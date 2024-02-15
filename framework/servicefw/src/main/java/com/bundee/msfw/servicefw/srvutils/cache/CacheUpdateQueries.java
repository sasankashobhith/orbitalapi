package com.bundee.msfw.servicefw.srvutils.cache;


import java.util.Collection;

import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.dc.UpdateCacheObj;

public class CacheUpdateQueries {
    private static final String INSERT_UC_ENTRY = "insert into update_cache (uc_id, uc_modid, targettenant_id, uc_data, create_ts, status) values (nextval('uc_id_seq'), ?, ?, ?, ?, ?)";
    private static final String UPDATE_UC_PICKUP = "update update_cache set status=101, pickup_ts=? where uc_id=?";
    private static final String UPDATE_UC_COMPLETION = "update update_cache set status=2, complete_ts=? where uc_id=?";
    private static final String LIST_UC_ENTRIES_BY_STATUS = "select uc_id, uc_modid, targettenant_id, uc_data, status, create_ts, pickup_ts, complete_ts from update_cache where status=?";

    public void getInsertUCEntriesQ(BLogger logger, DBManager dbManager, Collection<UpdateCacheObj> ucEntries) throws DBException {
        DBQuery dbQuery = dbManager.getDBQueryBuilder()
                .setQueryString(INSERT_UC_ENTRY)
                .setBindInputFunction(
                        (BLogger, stmt) -> {
                            for (UpdateCacheObj uc : ucEntries) {
                                stmt.setString(1, uc.getBusinessModID());
                                stmt.setInt(2, uc.getTargetTenantId());
                                stmt.setString(3, uc.getUCData().getUTF8String());
                                stmt.setLong(4, uc.getCreationTS());
                                stmt.setInt(5, uc.getStatus().getValue());
                                stmt.addBatch();
                            }
                        })
                .setBatch()
                .build();
        dbManager.update(logger, dbQuery);
    }

    public void getUpdateUCPickupTimeQ(BLogger logger, DBManager dbManager, Collection<Integer> ucIDs, long pickupTime) throws DBException {
        DBQuery dbQuery = dbManager.getDBQueryBuilder()
                .setQueryString(UPDATE_UC_PICKUP)
                .setBatch()
                .setBindInputFunction(
                        (BLogger, statement) -> {
                            for (Integer ucID : ucIDs) {
                                statement.setLong(1, pickupTime);
                                statement.setInt(2, ucID);
                                statement.addBatch();
                            }
                        }
                ).build();
        dbManager.update(logger, dbQuery);
    }

    public void getUpdateUCCompletionTimeQ(BLogger logger, DBManager dbManager, Collection<Integer> ucIDs, long completionTime) throws DBException {
        DBQuery dbQuery = dbManager.getDBQueryBuilder()
                .setBatch()
                .setQueryString(UPDATE_UC_COMPLETION)
                .setBindInputFunction(
                        (BLogger, stmt) -> {
                            for (Integer ucID : ucIDs) {
                                stmt.setLong(1, completionTime);
                                stmt.setInt(2, ucID);
                                stmt.addBatch();
                            }
                        }
                ).build();
        dbManager.update(logger, dbQuery);
    }

    public void getSelectUCEntriesByStatusQ(BLogger logger, DBManager dbManager, UpdateCacheObj.STATUS status, Collection<CacheUpdateObjImpl> ucEntries) throws DBException {
        DBQuery dbQuery = dbManager.getDBQueryBuilder()
                .setQueryString(LIST_UC_ENTRIES_BY_STATUS)
                .setBindInputFunction(
                        (BLogger, stmt) -> stmt.setInt(1, status.getValue())
                ).setFetchDataFunction(
                        (BLogger, resultSet) -> {
                            CacheUpdateObjImpl cuo = new CacheUpdateObjImpl(resultSet);
                            ucEntries.add(cuo);
                        }
                )
                .build();
        dbManager.select(logger, dbQuery);
    }


}
