package com.bundee.msfw.interfaces.dbi;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bundee.msfw.interfaces.logi.BLogger;

@FunctionalInterface
public interface FetchData {
    void fetchData(BLogger BLogger, ResultSet resultSet) throws SQLException;
}
