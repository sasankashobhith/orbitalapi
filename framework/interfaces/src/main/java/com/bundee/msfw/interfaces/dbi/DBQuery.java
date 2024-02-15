package com.bundee.msfw.interfaces.dbi;


import java.sql.CallableStatement;
import java.sql.SQLException;

import com.bundee.msfw.interfaces.logi.BLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface DBQuery {
	boolean logQuery();
	boolean logTrace();
	String getQS();
	void bindInput(BLogger logger, PreparedStatement stmt) throws SQLException;
	void fetchData(BLogger logger, ResultSet rs) throws SQLException;
	void setRowsUpdated(int rowsUpdated);
	int getRowsUpdated();
	boolean returnKeys();
	Boolean throwOnNoData();
	boolean isBatch();
	void registerOutParameterTypes(BLogger logger, CallableStatement stmt) throws SQLException;
}
