package org.novasparkle.sateguard.db;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetHandler<R> {
    R handle(ResultSet resultSet) throws SQLException;
}
