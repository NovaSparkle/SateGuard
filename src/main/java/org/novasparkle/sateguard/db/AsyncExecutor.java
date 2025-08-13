package org.novasparkle.sateguard.db;

import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncExecutor {
    private final ExecutorService executor;
    private final ConnectionPool connectionPool;

    public AsyncExecutor(ConfigurationSection section) {
        this.executor = Executors.newFixedThreadPool(section.getInt("asyncThreadPoolSize"));
        this.connectionPool = new ConnectionPool(section);
    }


    public void executeAsync(String query, Object... params) {
        executor.submit(() -> {
            try (Connection conn = connectionPool.getConnection()) {
                connectionPool.executeUpdate(conn, query, 1, params);
            } catch (SQLException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void executeSync(String query, Object... params) {
        try (Connection conn = connectionPool.getConnection()) {
            connectionPool.executeUpdate(conn, query, 1, params);
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public <T> List<T> executeQuery(String query, ResultSetHandler<T> handler, Object... params) {
        try (Connection conn = connectionPool.getConnection()) {
            return connectionPool.executeQuery(conn, query, handler, params);
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public void shutdown() {
        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        connectionPool.closeAll();
    }
}
