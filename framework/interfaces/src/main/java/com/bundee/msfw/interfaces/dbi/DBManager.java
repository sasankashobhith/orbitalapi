package com.bundee.msfw.interfaces.dbi;

import java.util.List;

import com.bundee.msfw.interfaces.blmodi.HealthCheck;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface DBManager extends HealthCheck {
	public DBQueryBuilder getDBQueryBuilder();
	
	public boolean update(BLogger logger, DBQuery query) throws DBException;
	public boolean update(BLogger logger, List<DBQuery> queries) throws DBException;
	
	public boolean select(BLogger logger, DBQuery query) throws DBException;

	public boolean executeSP(BLogger logger, DBQuery query) throws DBException;
}
