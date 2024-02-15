package com.bundee.ums.db;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.blmodi.*;
import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.ums.defs.*;
import com.bundee.ums.pojo.*;

import java.sql.*;
import java.util.Date;
import java.util.*;

public class APIKeyDAO {
    private static final String ADD_NEW_API_KEY = "insert into masterapikeys (apikeyname, isactive, validityts, createdby, updatedby) values (?,?,?,?,?)";
    private static final String GET_API_KEY_BY_ID = "select apikeyid, apikeyname, isactive, validityts, createdby, updatedby from masterapikeys where apikeyid=?";
    private static final String LIST_ALL_API_KEYS = "select apikeyid, apikeyname, isactive, validityts, createdby, updatedby from masterapikeys";
    private static final String UPDATE_API_KEY_STATUS = "update masterapikeys set isactive=?, updatedby=?, updtd_ts=? where apikeyid=?";
    private static final String ADD_NEW_APIKEY_ROLE_MAPPING_BYID = "insert into apikeyrolemapping (apikeyid, roleid, isactive, createdby, updatedby) values (?,?,?,?,?)";
    private static final String GET_ALL_APIKEY_ROLE_MAPPING_BYID = "select apikeyid, roleid, isactive, createdby, updatedby from apikeyrolemapping where apikeyid=?";
    private static final String GET_ACTIBE_APIKEY_ROLE_MAPPING_BYID = "select apikeyid, roleid, isactive, createdby, updatedby from apikeyrolemapping where apikeyid=? and isactive=true";
    private static final String DELETE_NEW_APIKEY_ROLE_MAPPING_BYID = "delete from apikeyrolemapping where apikeyid=?";

    public static void createNewAPIKey(BLogger logger, DBManager dbm, APIKeyDTO apiKeyDTO, TokenDetails td)
            throws BExceptions {
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery uq = dbQB.setQueryString(ADD_NEW_API_KEY).setBindInputFunction((dbLogger, ps) -> {
                ps.setString(1, apiKeyDTO.getAPIKeyName());
                ps.setBoolean(2, apiKeyDTO.getIsActive());
                ps.setLong(3, apiKeyDTO.getValidityTS());
                ps.setInt(4, td.getUserID());
                ps.setInt(5, 0);
            }).setFetchDataFunction((dbLogger, rs) -> {
                apiKeyDTO.setAPIKeyID(rs.getInt("apikeyid"));
            }).logQuery(true).throwOnNoData(true).setReturnKeys().build();
            dbm.update(logger, uq);
        } catch (DBException e) {
            logger.error(e);
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, apiKeyDTO.getAPIKeyName() + " already exists!");
        }
    }

    public static APIKeyDTO getAPIKeyByID(BLogger logger, DBManager dbm, int apiKeyID) throws BExceptions {
        List<APIKeyDTO> apiKeyDTOs = new ArrayList<APIKeyDTO>();
        try {
        	logger.debug("getAPIKeyByID building query");
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(GET_API_KEY_BY_ID).setBindInputFunction((dbLogger, ps) -> {
                ps.setInt(1, apiKeyID);
            }).setFetchDataFunction((dbLogger, rs) -> {
                apiKeyDTOs.add(getAPIKeyFromRS(rs));
            }).logQuery(true).throwOnNoData(true).logTrace(true).build();
            dbm.select(logger, sq);
        } catch (DBException e) {
            logger.error(e);
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKeyID " + apiKeyID + " does not exist!");
        }
        if (!apiKeyDTOs.isEmpty())
            return apiKeyDTOs.get(0);
        return null;
    }

    public static List<APIKeyDTO> listAllAPIKeys(BLogger logger, DBManager dbm) throws BExceptions {
        List<APIKeyDTO> apiKeyDTOs = new ArrayList<APIKeyDTO>();
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(LIST_ALL_API_KEYS).setFetchDataFunction((dbLogger, rs) -> {
                apiKeyDTOs.add(getAPIKeyFromRS(rs));
            }).logQuery(true).throwOnNoData(false).build();
            dbm.select(logger, sq);
        } catch (DBException e) {
        }
        return apiKeyDTOs;
    }

    public static void enableAPIKey(BLogger logger, DBManager dbm, int apiKeyID, TokenDetails td) throws BExceptions {
        updateAPIKeyStatus(logger, dbm, apiKeyID, td, true);
    }

    public static void disableAPIKey(BLogger logger, DBManager dbm, int apiKeyID, TokenDetails td) throws BExceptions {
        updateAPIKeyStatus(logger, dbm, apiKeyID, td, false);
    }

    public static void assignAPIKeyRoleMapping(BLogger logger, DBManager dbm, int apiKeyID,
                                               Set<APIKeyRoleDTO> ipRoleIDMapping, TokenDetails td) throws BExceptions {
        if (ipRoleIDMapping.isEmpty())
            return;
        List<DBQuery> queries = new ArrayList<DBQuery>();
        Set<APIKeyRoleDTO> roleMapping = listAllAPIKeyRoleMappingByID(logger, dbm, apiKeyID, false);
        if (!roleMapping.isEmpty()) {
            queries.add(deleteAPIKeyMapping(logger, dbm, apiKeyID));
        }
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery uq = dbQB.setQueryString(ADD_NEW_APIKEY_ROLE_MAPPING_BYID).setBindInputFunction((dbLogger, ps) -> {
                for (APIKeyRoleDTO apiKeyRole : ipRoleIDMapping) {
                    ps.setInt(1, apiKeyID);
                    ps.setInt(2, apiKeyRole.getRoleID());
                    ps.setBoolean(3, (apiKeyRole.getIsActive() == null ? true : apiKeyRole.getIsActive()));
                    ps.setInt(4, td.getUserID());
                    ps.setInt(5, td.getUserID());
                    ps.addBatch();
                }
            }).setBatch().logQuery(true).throwOnNoData(true).build();
            queries.add(uq);
            dbm.update(logger, queries);
        } catch (DBException e) {
            logger.error(e);
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKeyID " + apiKeyID + " does not exist!");
        }
    }

    public static Set<APIKeyRoleDTO> listAllAPIKeyRoleMappingByID(BLogger logger, DBManager dbm, int apiKeyID, boolean bActiveOnly)
            throws BExceptions {
        Set<APIKeyRoleDTO> roleMapping = new HashSet<APIKeyRoleDTO>();
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            String query = (bActiveOnly ? GET_ACTIBE_APIKEY_ROLE_MAPPING_BYID : GET_ALL_APIKEY_ROLE_MAPPING_BYID);
            DBQuery sq = dbQB.setQueryString(query).setBindInputFunction((dbLogger, ps) -> {
                ps.setInt(1, apiKeyID);
            }).setFetchDataFunction((dbLogger, rs) -> {
                roleMapping.add(getAPIKeyRoleFromRS(rs));
            }).logQuery(true).throwOnNoData(false).logTrace(true).build();
            dbm.select(logger, sq);
        } catch (DBException e) {
        }
        return roleMapping;
    }

    private static DBQuery deleteAPIKeyMapping(BLogger logger, DBManager dbm, int apiKeyID) throws BExceptions {
        DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
        DBQuery uq = dbQB.setQueryString(DELETE_NEW_APIKEY_ROLE_MAPPING_BYID).setBindInputFunction((dbLogger, ps) -> {
            ps.setInt(1, apiKeyID);
        }).logQuery(true).throwOnNoData(true).build();
        return uq;
    }

    // ---------------------------------------------------------------------------------
    private static void updateAPIKeyStatus(BLogger logger, DBManager dbm, int apiKeyID, TokenDetails td,
                                           boolean bEnable) throws BExceptions {
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            Date d = new Date();
            Timestamp ts = new Timestamp(d.getTime());
            DBQuery uq = dbQB.setQueryString(UPDATE_API_KEY_STATUS).setBindInputFunction((dbLogger, ps) -> {
                ps.setBoolean(1, bEnable);
                ps.setInt(2, td.getUserID());
                ps.setTimestamp(3, ts);
                ps.setInt(4, apiKeyID);
            }).logQuery(true).throwOnNoData(true).build();
            dbm.update(logger, uq);
        } catch (DBException e) {
            logger.error(e);
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKeyID " + apiKeyID + " does not exist!");
        }
    }

    private static APIKeyDTO getAPIKeyFromRS(ResultSet rs) throws SQLException {
        APIKeyDTO apiKeyDTO = new APIKeyDTO();
        apiKeyDTO.setAPIKeyID(rs.getInt("apikeyid"));
        apiKeyDTO.setAPIKeyName(rs.getString("apikeyname"));
        apiKeyDTO.setIsActive(rs.getBoolean("isactive"));
        apiKeyDTO.setValidityTS(rs.getLong("validityts"));
        apiKeyDTO.setCreatedBy(rs.getInt("createdby"));
        apiKeyDTO.setUpdatedBy(rs.getInt("updatedby"));
        return apiKeyDTO;
    }

    private static APIKeyRoleDTO getAPIKeyRoleFromRS(ResultSet rs) throws SQLException {
        APIKeyRoleDTO apiKeyRoleDTO = new APIKeyRoleDTO();
        apiKeyRoleDTO.setRoleID(rs.getInt("roleid"));
        apiKeyRoleDTO.setIsActive(rs.getBoolean("isactive"));
        apiKeyRoleDTO.setCreatedBy(rs.getInt("createdby"));
        apiKeyRoleDTO.setUpdatedBy(rs.getInt("updatedby"));
        return apiKeyRoleDTO;
    }
}
