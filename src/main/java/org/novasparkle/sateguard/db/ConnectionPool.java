package org.novasparkle.sateguard.db;

import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionPool {
    private final BlockingQueue<Connection> connectionQueue;
    private final int maxRetries;
    private final String url;
    private final String user;
    private final String password;

    @SneakyThrows
    public ConnectionPool(ConfigurationSection section) {
        Class.forName(section.getString("driverClass"));
        this.url = section.getString("url");
        this.user = section.getString("username");
        this.password = section.getString("password");
        this.maxRetries = section.getInt("maxRetries");
        int poolSize = section.getInt("poolSize");
        this.connectionQueue = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            this.connectionQueue.add(this.createNewConnection());
        }
    }

    public Connection getConnection() throws SQLException, InterruptedException {
        Connection conn = connectionQueue.take();
        if (!isConnectionValid(conn)) {
            conn = createNewConnection();
        }
        return conn;
    }

    private boolean isConnectionValid(Connection conn) {
        try {
            if (conn == null || conn.isClosed()) return false;

            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT 1").close();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection createNewConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, user, password);

        conn.setAutoCommit(true);
        return conn;
    }

    @SneakyThrows
    public void releaseConnection(Connection conn) {
        try {
            if (!isConnectionValid(conn)) {
                conn = createNewConnection();
                SateGuard.getInstance().warning(ConfigManager.getString("messages.DBbadConnection"));
            }
            connectionQueue.put(conn);
        } catch (Exception e) {
            try {
                conn.close();

            } catch (SQLException exception) {
                throw e;
            }
        }
    }

    @SneakyThrows
    public void closeAll() {
        for (Connection connection : this.connectionQueue) {
            if (!connection.isClosed()) {
                connection.close();
            }
        }
    }

    @SneakyThrows
    public void executeUpdate(Connection connection, String query, int currentAttempt, Object... params) {
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();

            if (currentAttempt > maxRetries) {
                throw e;

            } else {
                currentAttempt++;
                SateGuard.getInstance().warning(Utils.applyReplacements(ConfigManager.getString("messages.DBnewAttempt"), "query-%-" + query, "attempt-%-" + currentAttempt));
                try {
                    Thread.sleep(1000L);
                    this.executeUpdate(connection, query, currentAttempt, params);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Поток прерван при новой попытке", ie);
                }
            }
        } finally {
            connection.setAutoCommit(true);
        }

        this.releaseConnection(connection);
    }

    public <T> List<T> executeQuery(Connection connection, String sql,
                                    ResultSetHandler<T> handler,
                                    Object... params) {
        Objects.requireNonNull(sql, "SQL не может быть null");
        Objects.requireNonNull(handler, "ResultSetHandler не может быть null");

        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<T> result = new ArrayList<>();

        try {
            if (!connection.isValid(2)) {
                throw new SQLException("Connection неисправен");
            }

            stmt = connection.prepareStatement(
                    sql,
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.HOLD_CURSORS_OVER_COMMIT
            );
            stmt.setQueryTimeout(30);

            setParameters(stmt, params);

            rs = stmt.executeQuery();

            int rowCount = 0;

            while (rs.next()) {
                try {
                    result.add(handler.handle(rs));
                    rowCount++;

                    if (rowCount % 1000 == 0) {
                        System.gc();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Error mapping row " + rowCount + " of result set", e);
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error executing query: " + StringUtils.abbreviate(sql, 200), e);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
            returnConnection(connection);
        }
    }

    @SneakyThrows
    private void setParameters(PreparedStatement stmt, Object[] params) {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int paramIndex = i + 1;

            try {
                if (param == null) {
                    stmt.setNull(paramIndex, Types.NULL);
                } else if (param instanceof Integer) {
                    stmt.setInt(paramIndex, (Integer) param);
                } else if (param instanceof Long) {
                    stmt.setLong(paramIndex, (Long) param);
                } else {
                    stmt.setObject(paramIndex, param);
                }
            } catch (SQLException e) {
                throw new SQLException(String.format("Ошибка вставки параметра %d (value: %s)", paramIndex, param), e);
            }
        }
    }

    /**
     * Безопасно закрывает ResultSet
     */
    @SneakyThrows
    private void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                if (!rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException e) {
                SateGuard.getInstance().warning("Ошибка при закрытии ResultSet");
                throw e;
            }
        }
    }

    /**
     * Безопасно закрывает Statement
     */
    private void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try {
                if (!stmt.isClosed()) {
                    stmt.close();
                }
            } catch (SQLException e) {
                SateGuard.getInstance().warning("Ошибка при закрытии Statement");
            }
        }
    }

    /**
     * Возвращает соединение в пул с обработкой ошибок
     */
    private void returnConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    this.releaseConnection(conn);
                }
            } catch (SQLException e) {
                SateGuard.getInstance().warning("Ошибка при освобождении соединения");
                try {
                    conn.close();
                } catch (SQLException ex) {
                    SateGuard.getInstance().warning("Ошибка при закрытии соединения");
                }
            }
        }
    }
}
