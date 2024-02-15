package com.bundee.msfw.interfaces.dbi;


import java.sql.SQLException;

import com.bundee.msfw.interfaces.logi.BLogger;

public class DBException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String msg;
	private int sqlError;
	private String sqlState;
	private boolean bUniqueKeyViolation = false;
	private boolean bForeignKeyViolation = false;
	private boolean bBatchQueryFailed = false;
	private boolean bRowNotFound = false;

	private void init(int sqlError, String sqlState, String msg) {
		this.sqlState= sqlState;
		this.sqlError = sqlError;
		this.msg = msg;
	}
	
	public DBException(boolean bBatchQueryFailed, String msg) {
		this.bBatchQueryFailed = bBatchQueryFailed;
		this.msg = msg;
	}
	
	public DBException(boolean bRowNotFound) {
		this.bRowNotFound = bRowNotFound;
		this.msg = "";
		if(bRowNotFound) {
			this.msg = "No Data Found";
		}
	}
	
	public DBException(BLogger logger, SQLException ex) {
		logger.error(ex);
		String msg = ex.getMessage();
		if (ex.getNextException() != null) {
			msg = ex.getNextException().getMessage();
			logger.error(ex.getNextException());
		}
		init(ex.getErrorCode(), ex.getSQLState(), msg);
		if(sqlState != null) {
			if(sqlState.equals("23505")) {
				bUniqueKeyViolation = true;
			} else if(sqlState.equals("23503")) {
				bForeignKeyViolation = true;
			}
		}
	}

	public int getSqlError() {
		return sqlError;
	}
	
	public String getSqlState() {
		return sqlState;
	}
	
	public String getMessage() {
		return msg;
	}
	
	public boolean isUniqueKeyViolation() {
		return bUniqueKeyViolation;
	}
	
	public boolean isForeignKeyViolation() {
		return bForeignKeyViolation;
	}

	public boolean isRowNotFound() {
		return bRowNotFound;
	}

	public boolean isBatchQueryFailed() {
		return bBatchQueryFailed;
	}
}