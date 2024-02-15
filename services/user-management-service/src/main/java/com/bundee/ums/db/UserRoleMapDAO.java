package com.bundee.ums.db;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.ums.defs.*;
import com.bundee.ums.pojo.*;

import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.atomic.*;

public class UserRoleMapDAO {
    private static final String ADD_USER_ROLE_MAPPING = "insert into userrolemapping (userid,roleid,createdby,updatedby,isactive) values (?,?,?,?,?)";
    private static final String GET_ALL_USER_ROLE_MAPPING_BYID = "select * from userrolemapping where id=? and isactive=true";
    private static final String GET_ALL_USER_ROLE_MAPPING = "select * from userrolemapping where isactive=true";
    private static final String UPDATE_USER_ROLE_MAPPING = "UPDATE userrolemapping set updatedby=?,updatedate=?,roleid=? WHERE id=?";
    // private static final String UPDATE_MASTER_ROLES_ISACTIVE = "UPDATE
    // masterroles set isactive=?,createddate=? WHERE roleid=?";
    private static final String GET_USER_ROLE_MAPPING = "select * from userrolemapping where userid=? and isactive=true";
    private static final String GET_ROLE_PERMISSION_MAPPING = "select roleid, permission_name from rolepermission where roleid=any(?)";

    public static UserList allUserroleMapping(BLogger logger, DBManager dbm) throws DBException {
        UserList uList = new UserList();
        if (dbm == null) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Invalid roleId");
            return uList;
        } else {
            try {
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setQueryString(GET_ALL_USER_ROLE_MAPPING).setBindInputFunction((dbLogger, ps) -> {
                }).setFetchDataFunction((dbFLogger, rs) -> {
                    uList.getUserrolemap().add(mapuserRoleResponse(logger, rs));
                }).logQuery(true).throwOnNoData(false).build();
                logger.debug(sq.toString());
                dbm.select(logger, sq);
                if (uList.getUserResponses() == null) {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("role Does Not Exist");
                    return uList;
                } else {
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data Fetched");
                    return uList;
                }

            } catch (DBException e) {
                uList.setErrorCode("1");
                uList.setErrorMessage("Error in Exception" + e.getMessage().toString());
                return uList;
            }

        }

    }

    private static UserRoleMap mapuserRoleResponse(BLogger logger, ResultSet rs) throws SQLException {
        UserRoleMap roles = new UserRoleMap();
        roles.setId(rs.getInt("id"));
        roles.setRoleid(rs.getInt("roleid"));
        roles.setUserid(rs.getInt("userid"));
        roles.setCreateddate(rs.getString("createddate"));
        roles.setUpdateddate(rs.getString("updatedate"));
        roles.setCreatedby(rs.getInt("createdby"));
        roles.setUpdatedby(rs.getInt("updatedby"));
        roles.setIsactive(rs.getBoolean("isactive"));
        return roles;

    }

    public static UserList mapuserRolebyid(BLogger logger, DBManager dbm, int userroleid,
                                           List<UserRoleMap> userrolemapList) throws DBException {
        UserList roleresponse = new UserList();
        if (dbm == null || userroleid == 0) {
            roleresponse.setErrorCode("1");
            roleresponse.setErrorMessage("Error in role Request");
            return roleresponse;
        }
        // UserRoleMap userrole = new UserRoleMap();
        try {
            UserRoleMap userrole = new UserRoleMap();
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(GET_ALL_USER_ROLE_MAPPING_BYID).setBindInputFunction((dbBLogger, ps) -> {
                ps.setInt(1, userroleid);
            }).setFetchDataFunction((dbFLogger, rs) -> {
                roleresponse.getUserrolemap().add(mapuserRoleResponse(logger, rs));

            }).logQuery(true).throwOnNoData(false).build();
            dbm.select(logger, sq);
            roleresponse.setErrorCode("0");
            roleresponse.setErrorMessage("Data inserted Succesfully");
            return roleresponse;
        } catch (Exception e) {
            roleresponse.setErrorCode("1");
            roleresponse.setErrorMessage("Error in Reservaton Request exception" + e.getMessage());
            return roleresponse;
        }

    }

    public static UserList insertuserRole(BLogger logger, DBManager dbm, MasterRolesRequest bookingreq)
            throws DBException {
        UserList bookResponse = new UserList();
        if (dbm == null || bookingreq == null || bookingreq.getRoleid() == 0 || bookingreq.getUserid() == 0
                || bookingreq.getCreatedby() == 0) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in Reservaton Request");
            return bookResponse;
        }
        List<UserRoleMap> maplist = new ArrayList<UserRoleMap>();
        UserRoleMap bookingStatus = new UserRoleMap();
        try {
            Date createdDate = new Date();
            Date updatedDate = new Date();
            java.sql.Date sqlStartDate = new java.sql.Date(createdDate.getTime());
            java.sql.Date sqlendDate = new java.sql.Date(updatedDate.getTime());
            java.sql.Timestamp timestampcreatedDate = new java.sql.Timestamp(sqlStartDate.getTime());
            java.sql.Timestamp timestampupdatedDate = new java.sql.Timestamp(sqlendDate.getTime());
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setBatch().setQueryString(ADD_USER_ROLE_MAPPING).setBindInputFunction((dbLogger, ps) -> {
                {
                    ps.setInt(2, bookingreq.getRoleid());
                    ps.setInt(1, bookingreq.getUserid());
                    // ps.setTimestamp(3, timestampcreatedDate);
                    // ps.setTimestamp(4, timestampupdatedDate);
                    ps.setInt(3, bookingreq.getCreatedby());
                    ps.setInt(4, bookingreq.getUpdatedby());
                    ps.setBoolean(5, true);
                    ps.addBatch();
                }
            }).setReturnKeys().setFetchDataFunction((dbFlogger, rs) -> {
                bookResponse.getUserrolemap().add(mapuserRoleResponse(logger, rs));

            }).logQuery(true).build();
            dbm.update(logger, sq);
            bookResponse.setErrorCode("0");
            bookResponse.setErrorMessage("Data inserted Succesfully");
            return bookResponse;
        } catch (Exception e) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in Reservaton Request exception" + e.getMessage());
            return bookResponse;
        }

    }

    public static UserList updateUserRole(BLogger logger, DBManager dbm, MasterRolesRequest request)
            throws DBException {
        UserList userresponse = new UserList();
        if (dbm == null || request == null) {
            userresponse.setErrorCode("1");
            userresponse.setErrorMessage("Error in Reservaton Request");
            return userresponse;
        }
        List<UserRoleMap> ulist = new ArrayList<UserRoleMap>();
        UserRoleMap bookingStatus = new UserRoleMap();
        if (request.getRoleid() != 0 && request.getUserroleid() != 0 || request.getUpdatedby() != 0) {
            try {
                Date updatedDate = new Date();
                java.sql.Date sqlendDate = new java.sql.Date(updatedDate.getTime());
                java.sql.Timestamp timestampupdatedDate = new java.sql.Timestamp(sqlendDate.getTime());
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setBatch().setQueryString(UPDATE_USER_ROLE_MAPPING)
                        .setBindInputFunction((dbLogger, ps) -> {
                            {
                                ps.setInt(1, request.getUpdatedby());
                                ps.setTimestamp(2, timestampupdatedDate);
                                ps.setInt(3, request.getRoleid());
                                ps.setInt(4, request.getUserroleid());
                                ps.addBatch();
                            }
                        }).setReturnKeys().setFetchDataFunction((dbFlogger, rs) -> {
                            userresponse.getUserrolemap().add(mapuserRoleResponse(logger, rs));

                        }).logQuery(true).build();
                dbm.update(logger, sq);
                userresponse.setErrorCode("0");
                userresponse.setErrorMessage("Data Updated Succesfully");
                return userresponse;
            } catch (Exception e) {
                userresponse.setErrorCode("1");
                userresponse.setErrorMessage("Error in user Request exception" + e.getMessage());
                return userresponse;
            }

        } else {
            userresponse.setErrorCode("1");
            userresponse.setErrorMessage("Error in user Request exception");
            return userresponse;
        }

    }

    public static UserRoleMap getUserRoleMapping(BLogger logger, DBManager dbm, int userID) throws BExceptions {
        AtomicReference<UserRoleMap> urm = new AtomicReference<UserRoleMap>();
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(GET_USER_ROLE_MAPPING).setBindInputFunction((dbBLogger, ps) -> {
                ps.setInt(1, userID);
            }).setFetchDataFunction((dbFLogger, rs) -> {
                urm.set(mapuserRoleResponse(logger, rs));
            }).logQuery(true).throwOnNoData(true).build();
            dbm.select(logger, sq);
        } catch (DBException e) {
            throw new BExceptions(UMSProcessingCode.INVALID_CONFIGURATION, "Role does not exist for user: " + userID);
        }
        return urm.get();
    }

    public static Map<Integer, Set<String>> getRolePermissionsMapping(BLogger logger, DBManager dbm, Set<Integer> roleIDs) throws BExceptions {
        Map<Integer, Set<String>> rpm = new HashMap<Integer, Set<String>>();
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(GET_ROLE_PERMISSION_MAPPING).setBindInputFunction((dbBLogger, ps) -> {
                ps.setArray(1, ps.getConnection().createArrayOf("int", roleIDs.toArray()));
            }).setFetchDataFunction((dbFLogger, rs) -> {
                int roleID = rs.getInt("roleid");
                Set<String> perms = null;
                if (!rpm.containsKey(roleID)) {
                    perms = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
                    rpm.put(roleID, perms);
                } else {
                    perms = rpm.get(roleID);
                }
                perms.add(rs.getString("permission_name"));
            }).logQuery(true).throwOnNoData(true).logTrace(true).build();
            dbm.select(logger, sq);
        } catch (DBException e) {
            throw new BExceptions(UMSProcessingCode.INVALID_CONFIGURATION, "Permissions do not exist for role: " + roleIDs.toString());
        }
        return rpm;
    }

}


