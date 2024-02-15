package com.bundee.msfw.interfaces.dbi;

import java.sql.CallableStatement;
import java.sql.SQLException;

import com.bundee.msfw.interfaces.logi.BLogger;

@FunctionalInterface
public interface RegisterOutParameterTypes {
    void registerOutParameterTypes(BLogger logger, CallableStatement stmt) throws SQLException;
}
