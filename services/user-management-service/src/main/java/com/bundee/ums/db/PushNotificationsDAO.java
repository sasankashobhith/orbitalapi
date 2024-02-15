package com.bundee.ums.db;

import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.ums.pojo.*;

import java.sql.*;
import java.util.Date;
import java.util.*;

public class PushNotificationsDAO {
    private static final String INSERT_INTO_PUSHNOTIFICATIONS = "insert into pushnotification (userid,devicetoken) values (?,?)";
    private static final String GET_ALL_PUSHNOTIFICATIONS = "select * from pushnotification where isactive=true";
    private static final String GET_PUSH_NOTIFICATIONS_BYID = "SELECT * FROM pushnotification WHERE id=? and isactive=true";
    private static final String GET_PUSH_NOTIFICATIONS_BY_USERID = "SELECT * FROM pushnotification WHERE userid=? and isactive=true";
    private static final String GET_PUSH_NOTIFICATIONS_BY_USERIDS = "SELECT * FROM pushnotification WHERE userid in (&in) and isactive=true";
    private static final String UPDATE_DEVICETOKEN_BY_USERID = "UPDATE pushnotification  set devicetoken=?,updateddate=? WHERE userid=?";

    public static UserList insertintopushnotifications(BLogger logger, DBManager dbm,
                                                       PushNotificationsResponse customerobj) throws DBException {
        UserList bookResponse = new UserList();
        try {
            if (dbm == null || customerobj == null || customerobj.getUserid() == 0 || customerobj.getDevicetoken() == null) {
                bookResponse.setErrorCode("1");
                bookResponse.setErrorMessage("Error in PushNotifications  Request");
                return bookResponse;
            } else {
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setBatch().setQueryString(INSERT_INTO_PUSHNOTIFICATIONS)
                        .setBindInputFunction((dbLogger, ps) -> {
                            ps.setInt(1, customerobj.getUserid());
                            ps.setString(2, customerobj.getDevicetoken());
                            ps.addBatch();
                        }).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
                            bookResponse.getPushnotifications().add(createpushnotification(dbFLogger, rs));
                        }).logQuery(true).throwOnNoData(false).build();
                dbm.update(logger, sq);
                bookResponse.setErrorCode("0");
                bookResponse.setErrorMessage("Data inserted successfully");
                return bookResponse;
            }
        } catch (Exception e) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in PushNotifcations Request" + e.getMessage());
            return bookResponse;
        }
    }

    public static UserList getAllPushNotifications(BLogger logger, DBManager dbm, UserList vehiclesList)
            throws DBException {
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(GET_ALL_PUSHNOTIFICATIONS).setBindInputFunction((dbBLogger, ps) -> {
            }).setFetchDataFunction((dbFLogger, rs) -> {
                vehiclesList.getPushnotifications().add(createpushnotification(dbFLogger, rs));
            }).logQuery(true).throwOnNoData(false).build();
            dbm.select(logger, sq);
            vehiclesList.setErrorCode("0");
            vehiclesList.setErrorMessage("Data retrieved successfully");
            return vehiclesList;
        } catch (Exception e) {
            vehiclesList.setErrorMessage("Error retrieving all CustomerWishlist details - " + e.getMessage());
            vehiclesList.setErrorCode("1");
            return vehiclesList;
        }
    }

    private static PushNotificationsResponse createpushnotification(BLogger logger, ResultSet rs) throws SQLException {
        PushNotificationsResponse customeractivity = new PushNotificationsResponse();
        customeractivity.setId(rs.getInt("id"));
        customeractivity.setUserid(rs.getInt("userid"));
        customeractivity.setDevicetoken(rs.getString("devicetoken"));
        customeractivity.setCreateddate(rs.getString("createddate"));
        customeractivity.setUpdateddate(rs.getString("updateddate"));
        customeractivity.setIsactive(rs.getBoolean("isactive"));
        return customeractivity;
    }

    public static UserList getpushnotificationsbyid(BLogger logger, DBManager dbm, LoginRequest resObject,
                                                    List<PushNotificationsResponse> pushnotificationsresponse) throws DBException {
        UserList bookResponse = new UserList();
        if (dbm == null || resObject == null || resObject.getFromvalue() == null) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Invalid Input Request");
            return bookResponse;
        }
        try {
            String queryString = "NA";
            if (resObject.getFromvalue().toLowerCase().equals("id")) {
                queryString = GET_PUSH_NOTIFICATIONS_BYID;
            } else if (resObject.getFromvalue().toLowerCase().equals("userid")) {
                queryString = GET_PUSH_NOTIFICATIONS_BY_USERID;
            } else if (resObject.getFromvalue().toLowerCase().equals("userids")) {
                queryString = GET_PUSH_NOTIFICATIONS_BY_USERIDS.replace("&in",resObject.getUserids());

            } else {
                bookResponse.setErrorCode("1");
                bookResponse.setErrorMessage("Invalid from value Request");
                return bookResponse;
            }
            if (queryString.equals("NA")) {
                bookResponse.setErrorCode("1");
                bookResponse.setErrorMessage("Invalid Query Input Request");
                return bookResponse;
            }
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(queryString).setBindInputFunction((dbLogger, ps) -> {
                if (!resObject.getFromvalue().toLowerCase().equals("userids")) {
                    ps.setInt(1, resObject.getId());
                }
            }).setFetchDataFunction((dbFLogger, rs) -> {
                pushnotificationsresponse.add(createpushnotification(dbFLogger, rs));
            }).logQuery(true).throwOnNoData(false).build();
            dbm.select(logger, sq);
            bookResponse.setErrorCode("0");
            bookResponse.setErrorMessage("Data retrieved Succesfully");
            bookResponse.setPushnotifications(pushnotificationsresponse);
            return bookResponse;
        } catch (Exception e) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in PushNotifications Request exception" + e.getMessage());
            return bookResponse;
        }
    }

    public static UserList sendPushNotification(BLogger logger, DBManager dbm, LoginRequest resObject,
                                                List<PushNotificationsResponse> pushnotificationsresponse) throws DBException {
        UserList bookResponse = new UserList();
        if (dbm == null || resObject == null || resObject.getFromvalue() == null || resObject.getId() == 0) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Invalid Input Request");
            return bookResponse;
        }
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(GET_PUSH_NOTIFICATIONS_BY_USERID).setBindInputFunction((dbLogger, ps) -> {
                ps.setInt(1, resObject.getId());
            }).setFetchDataFunction((dbFLogger, rs) -> {
                pushnotificationsresponse.add(createpushnotification(dbFLogger, rs));
            }).logQuery(true).throwOnNoData(false).build();
            dbm.select(logger, sq);
            if(pushnotificationsresponse.isEmpty()){
                bookResponse.setErrorCode("1");
                bookResponse.setErrorMessage("Error PushNotifications Does not exist");
                return bookResponse;
            }

            bookResponse.setErrorCode("0");
            bookResponse.setErrorMessage("Data retrieved Succesfully");
            bookResponse.setPushnotifications(pushnotificationsresponse);
            return bookResponse;
        } catch (Exception e) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in PushNotifications Request exception" + e.getMessage());
            return bookResponse;
        }
    }

    public static UserList updatedevicetokenbyuserid(BLogger logger, DBManager dbm,
                                                     PushNotificationsResponse pushnotificationsresponse) throws DBException {
        UserList bookResponse = new UserList();
        if (dbm == null || pushnotificationsresponse == null || pushnotificationsresponse.getDevicetoken() == null || pushnotificationsresponse.getUserid() == 0) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in PushNotification  Request");
            return bookResponse;
        }
        DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
        try {
            Date date = new Date();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            java.sql.Timestamp updatedTime = new java.sql.Timestamp(sqlDate.getTime());
            DBQuery sq = dbQB.setBatch().setQueryString(UPDATE_DEVICETOKEN_BY_USERID)
                    .setBindInputFunction((dbLogger, ps) -> {
                        {
                            ps.setString(1, pushnotificationsresponse.getDevicetoken());
                            ps.setTimestamp(2, updatedTime);
                            ps.setInt(3, pushnotificationsresponse.getUserid());
                            ps.addBatch();
                        }
                    }).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
                        bookResponse.getPushnotifications().add(createpushnotification(dbFLogger, rs));
                    }).logQuery(true).build();
            dbm.update(logger, sq);
            bookResponse.setErrorCode("0");
            bookResponse.setErrorMessage("Data updated Succesfully");
            return bookResponse;
        } catch (Exception e) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in PushNotification  Request exception" + e.getMessage());
            return bookResponse;
        }
    }

    public static UserList updateDeviceToken(BLogger logger, DBManager dbm,
                                             PushNotificationsResponse pushnotificationsresponse) throws DBException {
        UserList bookResponse = new UserList();
        if (dbm == null || pushnotificationsresponse == null) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in PushNotification  Request");
            return bookResponse;
        }
        LoginRequest requestObject = new LoginRequest();
        requestObject.setId(pushnotificationsresponse.getUserid());
        requestObject.setFromvalue("userid");
        bookResponse = PushNotificationsDAO.getpushnotificationsbyid(logger, dbm, requestObject, new ArrayList<PushNotificationsResponse>());
        if (bookResponse.getPushnotifications().isEmpty()) {
            bookResponse = PushNotificationsDAO.insertintopushnotifications(logger, dbm, pushnotificationsresponse);
            return bookResponse;
        } else if (bookResponse.getPushnotifications().get(0).getDevicetoken().equals(pushnotificationsresponse.getDevicetoken())) {
            return bookResponse;
        }
        bookResponse = PushNotificationsDAO.updatedevicetokenbyuserid(logger, dbm, pushnotificationsresponse);
        return bookResponse;
    }
}
