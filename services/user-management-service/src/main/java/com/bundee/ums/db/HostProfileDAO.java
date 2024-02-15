package com.bundee.ums.db;

import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.ums.pojo.*;

import java.sql.*;

public class HostProfileDAO {
    private static final String GET_HOST_DATA = "SELECT hostprofile.*, mastercustomer.* FROM hostprofile INNER JOIN mastercustomer ON hostprofile.userid = mastercustomer.iduser where mastercustomer.iduser=? and mastercustomer.channelid=?";
    private static String INSERT_DRIVER_PROFILE = "INSERT INTO hostprofile (businessname, businesslicense, userid) VALUES (?, ?, ?)";

    public static UserList insertSingleUser(BLogger logger, DBManager dbm, HostProfile userobj) throws DBException {
        UserList uList = new UserList();
        if (dbm == null || userobj == null || userobj.getUserId() == 0 || userobj.getBusinessName() == null || userobj.getBusinessLicense() == null) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Error in Data null");
            return uList;
        }
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setBatch().setQueryString(INSERT_DRIVER_PROFILE).setBindInputFunction((dbLogger, ps) -> {
                ps.setString(1, userobj.getBusinessName());
                ps.setString(2, userobj.getBusinessLicense());
                ps.setInt(3, userobj.getUserId());
                ps.addBatch();
            }).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
                uList.getHostProfiles().add(createHostProfile(logger, rs));
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

    private static HostProfile createHostProfile(BLogger logger, ResultSet rs) throws SQLException {
        HostProfile user = new HostProfile();
        user.setId(rs.getInt("id"));
        user.setUserId(rs.getInt("userid"));
        user.setBusinessName(rs.getString("businessname"));
        user.setBusinessLicense(rs.getString("businesslicense"));
        user.setCreatedDate(rs.getString("createddate"));
        user.setUpdatedDate(rs.getString("updateddate"));
        user.setActive(rs.getBoolean("isactive"));
        return user;
    }

    public static UserList getHostProfile(BLogger logger, DBManager dbm, LoginRequest email) throws DBException {
        UserList uList = new UserList();
        if (dbm == null || email == null || email.getEmail() == null || email.getChannelid() == 0) {
            uList.setErrorCode("1");
            uList.setErrorMessage("Invalid User Id");
            return uList;
        } else {
            try {
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setQueryString(GET_HOST_DATA).setBindInputFunction((dbLogger, ps) -> {
                    ps.setString(1, email.getEmail());
                    ps.setInt(2, email.getChannelid());

                }).setFetchDataFunction((dbFLogger, rs) -> {
                    uList.getHostProfiles().add(hostProfileData(logger, rs));
                }).logQuery(true).throwOnNoData(false).build();
                logger.debug(sq.toString());
                dbm.select(logger, sq);
                if (uList.getHostProfiles() == null) {
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

    private static HostProfile hostProfileData(BLogger logger, ResultSet rs) throws SQLException {
        HostProfile user = new HostProfile();
        user.setId(rs.getInt("iduser"));
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
        user.setBusinessLicense(rs.getString("businnesslicense"));
        user.setBusinessName(rs.getString("businessname"));
        return user;
    }
}





