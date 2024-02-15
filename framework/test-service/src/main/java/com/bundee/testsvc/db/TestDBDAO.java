package com.bundee.testsvc.db;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.logi.*;

import java.util.*;

public class TestDBDAO {

	public static void select(BLogger logger, DBManager dbm, List<String> queryStrs, BExceptions execs) throws BExceptions {
		if (queryStrs == null || queryStrs.isEmpty())
			return;
		
		DBQueryBuilder dbQB = dbm.getDBQueryBuilder();

		for (String q : queryStrs) {
			DBQuery sq = dbQB.setQueryString(q).logQuery(true).throwOnNoData(true).setReturnKeys().build();
			try {
				dbm.select(logger, sq);
			} catch (DBException e) {
				logger.error(e);
				execs.add(new BExceptions(e, UniversalConstants.PCodes.INTERNAL_ERROR));
			}
		}		
	}
	
	public static void update(BLogger logger, DBManager dbm, List<String> queryStrs, BExceptions execs) throws BExceptions {
		if (queryStrs == null || queryStrs.isEmpty())
			return;
		List<DBQuery> queries = new ArrayList<DBQuery>();

		DBQueryBuilder dbQB = dbm.getDBQueryBuilder();

		for (String q : queryStrs) {
			DBQuery uq = dbQB.setQueryString(q).logQuery(true).throwOnNoData(true).setReturnKeys().build();
			queries.add(uq);
		}
		try {
			dbm.update(logger, queries);
		} catch (DBException e) {
			logger.error(e);
			execs.add(new BExceptions(e, UniversalConstants.PCodes.INTERNAL_ERROR));
		}
	}
}
