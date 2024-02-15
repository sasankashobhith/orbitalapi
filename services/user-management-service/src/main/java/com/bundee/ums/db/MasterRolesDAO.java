package com.bundee.ums.db;


import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.dbi.DBQueryBuilder;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.ums.pojo.MasterRoles;
import com.bundee.ums.pojo.MasterRolesRequest;
import com.bundee.ums.pojo.UserList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MasterRolesDAO {

    private static final String ADD_MASTER_ROLES = "insert into masterroles (rolename,createddate,isactive) values (?,?,?)";
    private static final String GET_MASTER_ROLES_BYID = "select * from masterroles where roleid=? and isactive=true";
    private static final String GET_ALL_MASTER_ROLES = "select * from masterroles where isactive=true";
    private static final String GET_MASTER_ROLES = "select * from masterroles where rolename=? and isactive=true";
    private static final String UPDATE_MASTER_ROLES = "UPDATE masterroles set rolename=?,isactive=?,createddate=? WHERE roleid=?";
    private static final String UPDATE_MASTER_ROLES_ISACTIVE = "UPDATE masterroles set isactive=? WHERE roleid=?";


    public static UserList allMasterRoles(BLogger logger, DBManager dbm, List<MasterRoles> masterlist) throws DBException {
        UserList reviewResponse = new UserList();

        if (dbm == null) {
            reviewResponse.setErrorCode("1");
            reviewResponse.setErrorMessage("Error in MasterRole Request");
            return reviewResponse;
        }

        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();

            DBQuery sq = dbQB.setQueryString(GET_ALL_MASTER_ROLES).setFetchDataFunction((dbFLogger, rs) -> {

                reviewResponse.getMasterroles().add(getAllMasterRoleDateData(logger, rs));

            }).logQuery(true).throwOnNoData(false).build();

            dbm.select(logger, sq);
            reviewResponse.setErrorCode("0");
            reviewResponse.setErrorMessage("Data Fetched Successfully");
            return reviewResponse;
        } catch (Exception e) {
            reviewResponse.setErrorCode("1");
            reviewResponse.setErrorMessage("Error in MasterRole Request exception" + e.getMessage());
            return reviewResponse;
        }

    }


    public static UserList masterRoleById(BLogger logger, DBManager dbm, int roleid, List<MasterRoles> masterrolesList) throws DBException {
        UserList response = new UserList();

        if (dbm == null || roleid == 0) {
            response.setErrorCode("1");
            response.setErrorMessage("Error in MasterRole Request");
            return response;
        }
        MasterRoles masterroles = new MasterRoles();

        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();

            DBQuery sq = dbQB.setQueryString(GET_MASTER_ROLES_BYID).setBindInputFunction((dbBLogger, ps) -> {
                ps.setInt(1, roleid);
            }).setFetchDataFunction((dbFLogger, rs) -> {
                response.getMasterroles().add(getAllMasterRoleDateData(logger, rs));

            }).logQuery(true).throwOnNoData(false).build();

            dbm.select(logger, sq);
            response.setErrorCode("0");
            response.setErrorMessage("Data retrieved Successfully");
            return response;
        } catch (Exception e) {
            response.setErrorCode("1");
            response.setErrorMessage("Error in MasterRole Request exception" + e.getMessage());
            return response;
        }

    }

    public static UserList getMasterRoles(BLogger logger, DBManager dbm, String roleName) throws DBException {
        UserList response = new UserList();

        if (dbm == null || roleName == null) {
            response.setErrorCode("1");
            response.setErrorMessage("Error in MasterRole Request");
            return response;
        }
        MasterRoles masterroles = new MasterRoles();

        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();

            DBQuery sq = dbQB.setQueryString(GET_MASTER_ROLES).setBindInputFunction((dbBLogger, ps) -> {
                ps.setString(1, roleName);
            }).setFetchDataFunction((dbFLogger, rs) -> {
                response.getMasterroles().add(getAllMasterRoleDateData(logger, rs));

            }).logQuery(true).throwOnNoData(false).build();

            dbm.select(logger, sq);
            response.setErrorCode("0");
            response.setErrorMessage("Data retrieved Successfully");
            return response;
        } catch (Exception e) {
            response.setErrorCode("1");
            response.setErrorMessage("Error in MasterRole Request exception" + e.getMessage());
            return response;
        }

    }
    public static UserList insertMasteRoles(BLogger logger, DBManager dbm, MasterRolesRequest masterrolesobj) throws DBException {
        UserList roleResponse = new UserList();
        if (dbm == null || masterrolesobj == null || masterrolesobj.getRolename() == null) {
            roleResponse.setErrorCode("1");
            roleResponse.setErrorMessage("Error in MasterRole Request");
            return roleResponse;
        }
        List<MasterRoles> roleslist = new ArrayList<MasterRoles>();
        MasterRoles roles = new MasterRoles();
        try {
            Date createdDate = new Date();

            java.sql.Date sqlStartDate = new java.sql.Date(createdDate.getTime());

            java.sql.Timestamp timestampcreatedDate = new java.sql.Timestamp(sqlStartDate.getTime());

            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();

            DBQuery sq = dbQB.setBatch().setQueryString(ADD_MASTER_ROLES).setBindInputFunction((dbLogger, ps) -> {
                {

                    ps.setString(1, masterrolesobj.getRolename());
                    ps.setTimestamp(2, timestampcreatedDate);
                    ps.setBoolean(3, true);
                    ps.addBatch();


                }
            }).setReturnKeys().setFetchDataFunction((dbFlogger, rs) -> {

                roleResponse.getMasterroles().add(getAllMasterRoleDateData(logger, rs));


            }).logQuery(true).build();

            dbm.update(logger, sq);
            roleResponse.setErrorCode("0");
            roleResponse.setErrorMessage("Data inserted Successfully");
            return roleResponse;
        } catch (Exception e) {
            roleResponse.setErrorCode("1");
            roleResponse.setErrorMessage("Error in MasterRole Request exception" + e.getMessage());
            return roleResponse;
        }

    }


    public static UserList updateMasterRole(BLogger logger, DBManager dbm, MasterRolesRequest requestobj) throws DBException {
        UserList roleResponse = new UserList();
        if (dbm == null || requestobj == null||requestobj.getRoleid() == 0 ) {
            roleResponse.setErrorCode("1");
            roleResponse.setErrorMessage("Error in MasterRole Request");
            return roleResponse;
        }
        List<MasterRoles> masterroleslist = new ArrayList<MasterRoles>();

        if (requestobj.getRolename() != null) {
            try {

                Date createdDate = new Date();

                java.sql.Date sqlendDate = new java.sql.Date(createdDate.getTime());

                java.sql.Timestamp timestampcreatedDate = new java.sql.Timestamp(sqlendDate.getTime());

                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();

                DBQuery sq = dbQB.setBatch().setQueryString(UPDATE_MASTER_ROLES).setBindInputFunction((dbLogger, ps) -> {


                    {
                        ps.setString(1, requestobj.getRolename());

                        ps.setBoolean(2, requestobj.getIsactive());

                        ps.setTimestamp(3, timestampcreatedDate);
                        ps.setInt(4, requestobj.getRoleid());
                        ps.addBatch();
                    }
                }).setReturnKeys().setFetchDataFunction((dbFlogger, rs) -> {
                    masterroleslist.add(getAllMasterRoleDateData(logger, rs));
                }).logQuery(true).build();

                dbm.update(logger, sq);
                roleResponse.setErrorCode("0");
                roleResponse.setErrorMessage("Data Updated Successfully");
                roleResponse.setMasterroles(masterroleslist);
                return roleResponse;
            } catch (Exception e) {
                roleResponse.setErrorCode("1");
                roleResponse.setErrorMessage("Error in MasterRole Request exception" + e.getMessage());
                return roleResponse;
            }

        } else {

            try {

                Date updatedDate = new Date();
                java.sql.Date sqlendDate = new java.sql.Date(updatedDate.getTime());
                java.sql.Timestamp timestampupdatedDate = new java.sql.Timestamp(sqlendDate.getTime());
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setBatch().setQueryString(UPDATE_MASTER_ROLES_ISACTIVE).setBindInputFunction((dbLogger, ps) -> {
                    {

                        ps.setBoolean(1, requestobj.getIsactive());

                        ps.setInt(2, requestobj.getRoleid());

                        ps.addBatch();
                    }
                }).setReturnKeys().setFetchDataFunction((dbFlogger, rs) -> {
                    roleResponse.getMasterroles().add(getAllMasterRoleDateData(logger, rs));
                }).logQuery(true).build();

                dbm.update(logger, sq);
                roleResponse.setErrorCode("0");
                roleResponse.setErrorMessage("Data is  Inactived Successfully");
                return roleResponse;
            } catch (Exception e) {
                roleResponse.setErrorCode("1");
                roleResponse.setErrorMessage("Error in roleResponse Request exception" + e.getMessage());
                return roleResponse;
            }
        }

    }


    private static MasterRoles getAllMasterRoleDateData(BLogger logger, ResultSet rs) throws SQLException {
        MasterRoles roles = new MasterRoles();

        roles.setRoleid(rs.getInt("roleid"));
        roles.setRolename(rs.getString("rolename"));
        roles.setCreateddate(rs.getString("createddate"));
        roles.setIsactive(rs.getBoolean("isactive"));


        return roles;
    }


}

		
	  



