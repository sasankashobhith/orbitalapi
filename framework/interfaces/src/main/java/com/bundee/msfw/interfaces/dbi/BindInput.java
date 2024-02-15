package com.bundee.msfw.interfaces.dbi;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.bundee.msfw.interfaces.logi.BLogger;

@FunctionalInterface
public interface BindInput {
    void bindInput(BLogger BLogger, PreparedStatement preparedStatement) throws SQLException;
}
