package com.bundee.msfw.servicefw.dbm;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bundee.msfw.interfaces.dbi.BindInput;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.dbi.DBQueryBuilder;
import com.bundee.msfw.interfaces.dbi.FetchData;
import com.bundee.msfw.interfaces.dbi.RegisterOutParameterTypes;
import com.bundee.msfw.interfaces.logi.BLogger;

public class DBQueryBuilderImpl implements DBQueryBuilder {
    private BindInput bindInputFunction;

    private FetchData fetchDataFunction;

    private RegisterOutParameterTypes registerOutParameterTypes;

    private String queryString;

    private boolean bBatch;

    private boolean bReturnKeys;
    
    private Boolean bThrowOnNoData = null;
    
    private boolean bLogQ;

    private boolean bLogTrace = true;
    
	@Override
	public DBQueryBuilder setBindInputFunction(BindInput bindInputFunction) {
		this.bindInputFunction = bindInputFunction;
		return this;
	}

	@Override
	public DBQueryBuilder setFetchDataFunction(FetchData fetchDataFunction) {
		this.fetchDataFunction = fetchDataFunction;
		return this;
	}

	@Override
	public DBQueryBuilder setQueryString(String queryString) {
		this.queryString = queryString;
		return this;
	}

	@Override
	public DBQueryBuilder setRegisterOutParameterTypes(RegisterOutParameterTypes registerOutParameterTypes) {
		this.registerOutParameterTypes = registerOutParameterTypes;
		return this;
	}

	@Override
	public DBQueryBuilder setBatch() {
		bBatch = true;
		return this;
	}

	@Override
	public DBQueryBuilder setReturnKeys() {
		bReturnKeys = true;
		return this;
	}

	@Override
	public DBQueryBuilder logQuery(boolean bLogQ) {
		this.bLogQ = bLogQ;
		return this;
	}
	
	@Override
	public DBQueryBuilder throwOnNoData(boolean bThrow) {
		bThrowOnNoData = bThrow;
		return this;
	}
	
	@Override
	public DBQueryBuilder logTrace(boolean bLogTrace) {
		this.bLogTrace = bLogTrace;
		return this;
	}
	
	@Override
	public DBQuery build() {
		return new DBQueryImpl(this);
	}

	private class DBQueryImpl implements DBQuery {
	    private final BindInput bindInputFunction;

	    private final FetchData fetchDataFunction;

	    private final RegisterOutParameterTypes registerOutParameterTypes;

	    private final String queryString;

	    private final boolean bBatch;

	    private final boolean bReturnKeys;
	    
	    private Boolean bThrowOnNoData = true;
	    
	    private boolean bLogQ = true;

	    private boolean bLogTrace = true;
	    
	    private int rowsUpdated = -1;

	    public DBQueryImpl(DBQueryBuilderImpl builder) {
	        this.bindInputFunction = builder.bindInputFunction;
	        this.fetchDataFunction = builder.fetchDataFunction;
	        this.queryString = builder.queryString;
	        this.registerOutParameterTypes = builder.registerOutParameterTypes;
	        this.bBatch = builder.bBatch;
	        this.bReturnKeys = builder.bReturnKeys;
	        this.bLogQ = builder.bLogQ;
	        this.bThrowOnNoData = builder.bThrowOnNoData;
	        this.bLogTrace = builder.bLogTrace;
	    }

		@Override
		public String getQS() {
			return queryString;
		}

		@Override
		public void bindInput(BLogger logger, PreparedStatement stmt) throws SQLException {
			if(bindInputFunction != null) {
				bindInputFunction.bindInput(logger, stmt);
			}
			
		}

		@Override
		public void fetchData(BLogger logger, ResultSet rs) throws SQLException {
			if(fetchDataFunction != null) {
				fetchDataFunction.fetchData(logger, rs);
			}
		}
		
		@Override
		public void registerOutParameterTypes(BLogger logger, CallableStatement stmt) throws SQLException {
			if(registerOutParameterTypes != null) {
				registerOutParameterTypes.registerOutParameterTypes(logger, stmt);
			}
		}

		@Override
		public void setRowsUpdated(int rowsUpdated){
	    	this.rowsUpdated = rowsUpdated;
		}

		@Override
		public int getRowsUpdated(){
	    	return this.rowsUpdated;
	    }

	    @Override
		public boolean returnKeys(){
	    	return this.bReturnKeys;
		}

	    @Override
		public Boolean throwOnNoData() {
	    	return this.bThrowOnNoData;
		}
	    
		@Override
		public boolean isBatch(){
	    	return this.bBatch;
	    }

		@Override
		public boolean logQuery() {
	    	return this.bLogQ;
	    }
		
		@Override
		public boolean logTrace() {
			return this.bLogTrace;
		}
	}
}
