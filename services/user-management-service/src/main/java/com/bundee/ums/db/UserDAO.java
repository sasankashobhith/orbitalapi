package com.bundee.ums.db;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.ums.pojo.*;
import com.bundee.ums.utils.*;

import java.sql.*;
import java.util.Date;
import java.util.*;

public class UserDAO {
    private static final String INSERT_USER = "INSERT INTO mastercustomer( firstname, middlename, lastname, email, mobilephone, address_1, address_2, address_3, city, state, postcode, country, language, driverlicense, vehicleowner,userimage,firebaseuserid,channelname,channelid) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
    private static final String VALIDATE_USER = "select  email,iduser,firstname,lastname from masterCustomer where email=?  and isactive=true";
    private static final String COUNT_BY_ID = "select * from mastercustomer where iduser=? and isactive=true";
    private static final String GETALL_BY_EMAIL = "select * from mastercustomer where email=? and isactive=true";
    private static final String GET_BY_FIREBASE = "select * from mastercustomer where firebaseuserid=? and isactive=true";
    //    private static final String UPDATE_USER_BY_USER_ID = "UPDATE mastercustomer SET %s , updateddate=? WHERE iduser = ?";
    private static final String UPDATE_USER_BY_USER_ID = "UPDATE mastercustomer SET firstname=?, middlename=?, lastname=? ,mobilephone=?, address_1=?, address_2=?, address_3=?, city=?, state=?, postcode=?, country=?, language=?, driverlicense=?, vehicleowner=? ,updateddate=?, userimage=? WHERE iduser=?";
    private static final String UPDATE_USER_PROFILE_BY_USER_ID = "UPDATE mastercustomer SET firstname=?, lastname=?, address_1=?, mobilephone=?, address_2=?,  city=?, state=?, postcode=?,updateddate=? WHERE iduser=?";
    private static final String UPDATE_USER_TOKEN = "UPDATE mastercustomer SET updateddate=?, stripecustomertoken=? WHERE iduser=?";
    private static final String GET_ALL_USERS = "select * from mastercustomer where isactive=true";
    private static final String GET_USERS_BY_FIREBASEID = "select * from mastercustomer where isactive=true";
    private static final String GET_HOST_DATA = "select * from mastercustomer where email=?  and channelid=?  and isactive=true";
    private static final String SET_HOST_ROLE = "update mastercustomer set isverified=false where iduser=?";
    private static final String ON_BOARD_HOST = "update mastercustomer set isverified=true where iduser in(&in)";

    public static void validateUser(BLogger logger, DBManager dbm, LoginRequest userobj, List<User> obj) throws DBException {
        if (dbm == null || obj == null) return;
        DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
        DBQuery sq = dbQB.setQueryString(VALIDATE_USER).setBindInputFunction((dbLogger, ps) -> {
            ps.setString(1, userobj.getEmail().toString());
        }).setFetchDataFunction((dbFLogger, rs) -> {
            logger.debug(rs.toString());
            obj.add(createUser(dbFLogger, rs));
        }).logQuery(true).throwOnNoData(false).build();
        logger.debug(sq.toString());
        dbm.select(logger, sq);
    }

    private static User createUser(BLogger logger, ResultSet rs) throws SQLException {
        User usr = new User();
        usr.setId(rs.getInt("iduser"));
        usr.setEmailid(new UTF8String(rs.getString("email")));
        usr.setfirstname(new UTF8String(rs.getString("firstname")));
        usr.setlastname(new UTF8String(rs.getString("lastname")));
        return usr;
    }

    public static UserList insertSingleUser(BLogger logger, DBManager dbm, UserResponse userobj) throws DBException {
        UserList uList = new UserList();
        if (dbm == null || userobj == null || userobj.getEmail() == null || userobj.getFirstname() == null) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Error in Data null");
            return uList;
        }
        try {
//            if(UserDAO.vlaidateUser(userobj)){
//                uList.setErrorCode("1");
//                uList.setErrorMessage("Error in Data null");
//                return uList;
//            }
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setBatch().setQueryString(INSERT_USER).setBindInputFunction((dbLogger, ps) -> {
                ps.setString(1, userobj.getFirstname());
                ps.setString(2, userobj.getMiddlename());
                ps.setString(3, userobj.getLastname());
                ps.setString(4, userobj.getEmail());
                ps.setString(5, userobj.getMobilephone());
                ps.setString(6, userobj.getAddress_1());
                ps.setString(7, userobj.getAddress_2());
                ps.setString(8, userobj.getAddress_3());
                ps.setString(9, userobj.getCity());
                ps.setString(10, userobj.getState());
                ps.setString(11, userobj.getPostcode());
                ps.setString(12, userobj.getCountry());
                ps.setString(13, userobj.getLanguage());
                ps.setString(14, userobj.getDriverlisense());
                ps.setBoolean(15, userobj.isVehicleowner());
                ps.setString(16, userobj.getUserimage());
                ps.setString(17, userobj.getFirebaseId());
                ps.setString(18, userobj.getChannelName());
                ps.setInt(19, userobj.getChannelId());
                ps.addBatch();
            }).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
                uList.getUserResponses().add(mapUserResponse(logger, rs));
            }).logQuery(true).throwOnNoData(false).build();
            dbm.update(logger, sq);
            uList.setErrorCode("0");
            uList.setErrorMessage("Successully Data inserted");
            return uList;
        } catch (Exception e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Error in Exception" + e.getMessage());
            return uList;
        }
    }

    public static Boolean vlaidateUser(UserResponse userResponse) {
        return userResponse.getFirstname() == null || userResponse.getMiddlename() == null || userResponse.getLastname() == null || userResponse.getAddress_1() == null || userResponse.getCity() == null || userResponse.getState() == null || userResponse.getPostcode() == null || userResponse.getCountry() == null || userResponse.getLanguage() == null || userResponse.getDriverlisense() == null || userResponse.getUserimage() == null;
    }

    public static UserList getbyuserIds(BLogger logger, DBManager dbm, String iduser) throws DBException {
        UserList uList = new UserList();
        List<UserResponse> userResponse = new ArrayList<UserResponse>();
        if (dbm == null || iduser == null) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Invalid User Id");
            return uList;
        } else {
            String Get_By_USER = "select * from mastercustomer where iduser IN (&in) ";
            String csvId = iduser.replaceAll("^|$", "'").replaceAll(",", "','").replaceAll("\"", "'");
            String repSearcStr = Get_By_USER.replace("&in", csvId);
            try {
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setQueryString(repSearcStr).setBindInputFunction((dbLogger, ps) -> {
                }).setFetchDataFunction((dbFLogger, rs) -> {
                    userResponse.add(mapUserResponse(logger, rs));
                }).logQuery(true).throwOnNoData(false).build();
                logger.debug(sq.toString());
                dbm.select(logger, sq);
                if (userResponse.size() == 0) {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("User Does Not Exist");
                    return uList;
                } else {
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data Fetched");
                    uList.setUserResponses(userResponse);
                    return uList;
                }
            } catch (DBException e) {
                uList.setErrorCode("1");
                uList.setErrorMessage("Error in Exception" + e.getMessage());
                return uList;
            }
        }
    }

    public static UserList countbyid(BLogger logger, DBManager dbm, int iduser) throws DBException {
        UserList uList = new UserList();
        if (dbm == null || iduser == 0) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Invalid User Id");
            return uList;
        } else {
            try {
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setQueryString(COUNT_BY_ID).setBindInputFunction((dbLogger, ps) -> {
                    ps.setInt(1, iduser);
                }).setFetchDataFunction((dbFLogger, rs) -> {
                    //uList.setCount(rs.getInt("count"));
                    uList.setUserResponse(mapUserResponse(logger, rs));
                }).logQuery(true).throwOnNoData(false).build();
                logger.debug(sq.toString());
                dbm.select(logger, sq);
                if (uList.getUserResponse() == null) {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("User Does Not Exist");
                    return uList;
                } else {
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data Fetched");
                    return uList;
                }
            } catch (DBException e) {
                uList.setErrorCode("1");
                uList.setErrorMessage("Error in Exception" + e.getMessage());
                return uList;
            }
        }
    }

    private static UserResponse mapUserResponse(BLogger logger, ResultSet rs) throws SQLException {
        UserResponse user = new UserResponse();
        user.setIduser(rs.getInt("iduser"));
        user.setFirstname(rs.getString("firstname"));
        user.setMiddlename(rs.getString("middlename"));
        user.setLastname(rs.getString("lastname"));
        user.setEmail(rs.getString("email"));
        user.setMobilephone(rs.getString("mobilephone"));
        user.setAddress_1(rs.getString("address_1"));
        user.setAddress_2(rs.getString("address_2"));
        user.setAddress_3(rs.getString("address_3"));
        user.setCity(rs.getString("city"));
        user.setState(rs.getString("state"));
        user.setPostcode(rs.getString("postcode"));
        user.setCountry(rs.getString("country"));
        user.setLanguage(rs.getString("city"));
        user.setDriverlisense(rs.getString("driverlicense"));
        user.setIsactive(rs.getBoolean("isactive"));
        user.setVehicleowner(rs.getBoolean("vehicleowner"));
        user.setCreateddate(rs.getString("createddate"));
        user.setUpdateddate(rs.getString("updateddate"));
        user.setUserimage(rs.getString("userimage"));
        user.setFirebaseId(rs.getString("firebaseuserid"));
        user.setStripeCustomerToken(rs.getString("stripecustomertoken"));
        user.setChannelName(rs.getString("channelname"));
        user.setChannelId(rs.getInt("channelid"));
        user.setVerified(rs.getBoolean("isverified"));
        return user;
    }

    public static UserList getDataByEmail(BLogger logger, DBManager dbm, String email) throws DBException {
        UserList uList = new UserList();
        if (dbm == null || email == null || email.isEmpty()) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Invalid User Id");
            return uList;
        } else {
            try {
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setQueryString(GETALL_BY_EMAIL).setBindInputFunction((dbLogger, ps) -> {
                    ps.setString(1, email);
                }).setFetchDataFunction((dbFLogger, rs) -> {
                    uList.setUserResponse(mapUserResponse(logger, rs));
                }).logQuery(true).throwOnNoData(false).build();
                logger.debug(sq.toString());
                dbm.select(logger, sq);
                if (uList.getUserResponse() == null) {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("User Does Not Exist");
                    return uList;
                } else {
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data Fetched");
                    return uList;
                }
            } catch (DBException e) {
                uList.setErrorCode("1");
                uList.setErrorMessage("Error in Exception" + e.getMessage());
                return uList;
            }
        }
    }

    public static UserList getDataByFirebase(BLogger logger, DBManager dbm, String firebaseID) throws DBException {
        UserList uList = new UserList();
        if (dbm == null || firebaseID == null || firebaseID.isEmpty()) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Invalid User Id");
            return uList;
        } else {
            try {
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setQueryString(GET_BY_FIREBASE).setBindInputFunction((dbLogger, ps) -> {
                    ps.setString(1, firebaseID);
                }).setFetchDataFunction((dbFLogger, rs) -> {
                    uList.setUserResponse(mapUserResponse(logger, rs));
                }).logQuery(true).throwOnNoData(false).build();
                logger.debug(sq.toString());
                dbm.select(logger, sq);
                if (uList.getUserResponse() == null) {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("User Does Not Exist");
                    return uList;
                } else {
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data Fetched");
                    return uList;
                }
            } catch (DBException e) {
                uList.setErrorCode("1");
                uList.setErrorMessage("Error in Exception" + e.getMessage());
                return uList;
            }
        }
    }

    public static UserList updateUser(BLogger logger, DBManager dbm, UserResponse userobj) throws DBException {
        UserList uList = new UserList();
        UserResponse userResponse = new UserResponse();
        if (dbm == null || userobj == null) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection ");
            return uList;
        }
        try {
            if (userobj.getIduser() != 0) {
                if (Util.validateUserById(userobj.getIduser(), logger, dbm)) {
                    Date date = new Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    java.sql.Timestamp updatedTime = new java.sql.Timestamp(sqlDate.getTime());
                    List<String> setClauses = new ArrayList<>();
                    DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                    if (UserDAO.vlaidateUser(userobj)) {
                        uList.setErrorCode("1");
                        uList.setErrorMessage("Error in Data null");
                        return uList;
                    }
                    DBQuery sq = dbQB.setBatch().setQueryString(UPDATE_USER_BY_USER_ID).setBindInputFunction((dbLogger, ps) -> {
                        ps.setString(1, userobj.getFirstname());
                        ps.setString(2, userobj.getMiddlename());
                        ps.setString(3, userobj.getLastname());
                        ps.setString(4, userobj.getMobilePhone());
                        ps.setString(5, userobj.getAddress_1());
                        ps.setString(6, userobj.getAddress_2());
                        ps.setString(7, userobj.getAddress_3());
                        ps.setString(8, userobj.getCity());
                        ps.setString(9, userobj.getState());
                        ps.setString(10, userobj.getPostcode());
                        ps.setString(11, userobj.getCountry());
                        ps.setString(12, userobj.getLanguage());
                        ps.setString(13, userobj.getDriverlisense());
                        ps.setBoolean(14, userobj.isVehicleowner());
                        ps.setTimestamp(15, updatedTime);
                        ps.setString(16, userobj.getUserimage());
                        ps.setInt(17, userobj.getIduser());
                        ps.addBatch();
                    }).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
                        uList.getUserResponses().add(mapUserResponse(logger, rs));
                    }).logQuery(true).throwOnNoData(false).build();
                    dbm.update(logger, sq);
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data inserted");
                    return uList;
                } else {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("User not found");
                    return uList;
                }
            } else {
                uList.setErrorCode("1");
                uList.setErrorMessage("User not found");
                return uList;
            }
        } catch (Exception e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection ");
            return uList;
        }
    }

    public static UserList updateUserProfile(BLogger logger, DBManager dbm, UserResponse userobj) throws DBException {
        UserList uList = new UserList();
        UserResponse userResponse = new UserResponse();
        if (dbm == null || userobj == null) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection ");
            return uList;
        }
        try {
            if (userobj.getIduser() != 0) {
                if (Util.validateUserById(userobj.getIduser(), logger, dbm)) {
                    Date date = new Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    java.sql.Timestamp updatedTime = new java.sql.Timestamp(sqlDate.getTime());
                    List<String> setClauses = new ArrayList<>();
                    DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                    DBQuery sq = dbQB.setBatch().setQueryString(UPDATE_USER_PROFILE_BY_USER_ID).setBindInputFunction((dbLogger, ps) -> {
                        ps.setString(1, userobj.getFirstname());
                        ps.setString(2, userobj.getLastname());
                        ps.setString(3, userobj.getAddress_1());
                        ps.setString(4, userobj.getMobilePhone());
                        ps.setString(5, userobj.getAddress_2());
                        ps.setString(6, userobj.getCity());
                        ps.setString(7, userobj.getState());
                        ps.setString(8, userobj.getPostcode());
                        ps.setTimestamp(9, updatedTime);
                        ps.setInt(10, userobj.getIduser());
                        ps.addBatch();
                    }).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
                        uList.getUserResponses().add(mapUserResponse(logger, rs));
                    }).logQuery(true).throwOnNoData(false).build();
                    dbm.update(logger, sq);
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data inserted");
                    return uList;
                } else {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("User not found");
                    return uList;
                }
            } else {
                uList.setErrorCode("1");
                uList.setErrorMessage("User not found");
                return uList;
            }
        } catch (Exception e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection ");
            return uList;
        }
    }

    public static UserList updateUserToken(BLogger logger, DBManager dbm, UserResponse userobj) throws DBException {
        UserList uList = new UserList();
        UserResponse userResponse = new UserResponse();
        if (dbm == null || userobj == null) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection ");
            return uList;
        }
        try {
            if (userobj.getIduser() != 0 && userobj.getStripeCustomerToken() != null) {
                if (Util.validateUserById(userobj.getIduser(), logger, dbm)) {
                    Date date = new Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    java.sql.Timestamp updatedTime = new java.sql.Timestamp(sqlDate.getTime());
                    DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                    DBQuery sq = dbQB.setBatch().setQueryString(UPDATE_USER_TOKEN).setBindInputFunction((dbLogger, ps) -> {
                        ps.setTimestamp(1, updatedTime);
                        ps.setString(2, userobj.getStripeCustomerToken());
                        ps.setInt(3, userobj.getIduser());
                        ps.addBatch();
                    }).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
                        uList.getUserResponses().add(mapUserResponse(logger, rs));
                    }).logQuery(true).throwOnNoData(false).build();
                    dbm.update(logger, sq);
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data inserted");
                    return uList;
                } else {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("User not found");
                    return uList;
                }
            } else {
                uList.setErrorCode("1");
                uList.setErrorMessage("Error in data");
                return uList;
            }
        } catch (Exception e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection ");
            return uList;
        }
    }

    public static UserList allUsers(BLogger logger, DBManager dbm) throws DBException {
        UserList uList = new UserList();
        if (dbm == null) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Invalid User Id");
            return uList;
        } else {
            try {
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setQueryString(GET_ALL_USERS).setBindInputFunction((dbLogger, ps) -> {
                }).setFetchDataFunction((dbFLogger, rs) -> {
                    uList.getUserResponses().add(mapUserResponse(logger, rs));
                }).logQuery(true).throwOnNoData(false).build();
                logger.debug(sq.toString());
                dbm.select(logger, sq);
                if (uList.getUserResponses() == null) {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("User Does Not Exist");
                    return uList;
                } else {
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data Fetched");
                    return uList;
                }
            } catch (DBException e) {
                uList.setErrorCode("1");
                uList.setErrorMessage("Error in Exception" + e.getMessage());
                return uList;
            }
        }
    }

    public static UserList getHostByEmail(BLogger logger, DBManager dbm, String email, int channelId) throws DBException {
        UserList uList = new UserList();
        if (dbm == null || email == null || email.isEmpty()) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Invalid User Id");
            return uList;
        } else {
            try {
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setQueryString(GET_HOST_DATA).setBindInputFunction((dbLogger, ps) -> {
                    ps.setString(1, email);
                    ps.setInt(2, channelId);
                }).setFetchDataFunction((dbFLogger, rs) -> {
                    uList.getUserResponses().add(mapUserResponse(logger, rs));
                }).logQuery(true).throwOnNoData(false).build();
                logger.debug(sq.toString());
                dbm.select(logger, sq);
                if (uList.getUserResponse() == null) {
                    uList.setErrorCode("1");
                    uList.setErrorMessage("User Does Not Exist");
                    return uList;
                } else {
                    uList.setErrorCode("0");
                    uList.setErrorMessage("Successully Data Fetched");
                    return uList;
                }
            } catch (DBException e) {
                uList.setErrorCode("1");
                uList.setErrorMessage("Error in Exception" + e.getMessage());
                return uList;
            }
        }
    }

    public static UserList setHostRole(BLogger logger, DBManager dbm, int userId) throws DBException {
        UserList userresponse = new UserList();
        if (dbm == null || userId == 0) {
            userresponse.setErrorCode("1");
            userresponse.setErrorMessage("Error in Assigning Role Request");
            return userresponse;
        }
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setBatch().setQueryString(SET_HOST_ROLE).setBindInputFunction((dbLogger, ps) -> {
                {
                    ps.setInt(1, userId);
                    ps.addBatch();
                }
            }).setReturnKeys().setFetchDataFunction((dbFlogger, rs) -> {
                userresponse.setUserResponse(mapUserResponse(logger, rs));
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

    }

    public static UserList onBoardHost(BLogger logger, DBManager dbm, String userIds) {
        UserList userresponse = new UserList();
        try {
            String csvId = userIds.replaceAll("^|$", "'").replaceAll(",", "','").replaceAll("\"", "'");
            String repSearcStr = ON_BOARD_HOST.replace("&in", csvId);
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(repSearcStr).setBindInputFunction((dbBLogger, ps) -> {
            }).setFetchDataFunction((dbFLogger, rs) -> {
                userresponse.getUserResponses().add(mapUserResponse(logger, rs));
            }).setReturnKeys().logQuery(true).throwOnNoData(false).build();
            dbm.update(logger, sq);
            userresponse.setErrorCode("0");
            userresponse.setErrorMessage("Updated Successfully ");
            return userresponse;
        } catch (Exception e) {
            userresponse.setErrorCode("1");
            userresponse.setErrorMessage("Errorr in OnBoarding Host " + e.getMessage());
            return userresponse;
        }
    }

}
