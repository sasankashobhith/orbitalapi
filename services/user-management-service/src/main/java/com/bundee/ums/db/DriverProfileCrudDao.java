package com.bundee.ums.db;

import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.ums.pojo.*;

import java.sql.*;
import java.util.Date;
import java.util.*;

public class DriverProfileCrudDao {
    private static final String GET_DRIVER_PROFILE_BY_USER_ID = "select * from  driverprofile where userid=? and isactive=true";
    private static final String INSERT_INTO_DRIVER_PROFILE = "INSERT INTO driverprofile(userid, drivinglicensenumber, drivinglicenseurl, insurancenumber, insuranceurl,insurancecompany) VALUES ( ?, ?, ?, ?, ?, ?);";
    private static final String GET_ALL_DRIVER_PROFILE = "select * from driverprofile where isactive=true";
    private static final String UPDATE_DRIVER_PROFILE = "UPDATE driverprofile SET  drivinglicensenumber=?, drivinglicenseurl=?,updatedtime=? WHERE userid=?";
    private static final String UPDATE_DRIVER_PROFILE_INSURANCE = "UPDATE driverprofile SET  insurancenumber=?,insuranceurl=?,insurancecompany=?,updatedtime=? WHERE userid=?";
    private static final String GET_BY_DRIVER_PROFILE_ID = "select * from  driverprofile where id=? and isactive=true";

    private static DriverProfile getallData(BLogger logger, ResultSet rs) throws SQLException {
        DriverProfile driverProfile = new DriverProfile();
        driverProfile.setCreatedTime(rs.getString("createdtime"));
        driverProfile.setId(rs.getInt("id"));
        driverProfile.setUserId(rs.getInt("userid"));
        driverProfile.setActive(rs.getBoolean("isactive"));
        driverProfile.setVerified(rs.getBoolean("isverified"));
        driverProfile.setUpdatedTime(rs.getString("updatedtime"));
        driverProfile.setDrivingLicenseNumber(rs.getString("drivinglicensenumber"));
        driverProfile.setDrivingLicenseUrl(rs.getString("drivinglicenseurl"));
        driverProfile.setInsuranceNumber(rs.getString("insurancenumber"));
        driverProfile.setInsuranceUrl(rs.getString("insuranceurl"));
        driverProfile.setInsuranceCompany(rs.getString("insurancecompany"));
        return driverProfile;
    }

    public static UserList insertDriverProfile(BLogger logger, DBManager dbm, DriverProfile customerobj, List<Image> imageList) {
        UserList bookResponse = new UserList();
        try {
            if (dbm == null || customerobj == null) {
                bookResponse.setErrorCode("1");
                bookResponse.setErrorMessage("Error in CustomerWishList  Request");
                return bookResponse;
            } else {
                customerobj.setDrivingLicenseUrl(imageList.get(0).getImageUrl());
                if (imageList.size() > 1) {
                    customerobj.setInsuranceUrl(imageList.get(1).getImageUrl());
                }
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setBatch().setQueryString(INSERT_INTO_DRIVER_PROFILE)
                        .setBindInputFunction((dbLogger, ps) -> {
                            ps.setInt(1, customerobj.getUserId());
                            ps.setString(2, customerobj.getDrivingLicenseNumber());
                            ps.setString(3, customerobj.getDrivingLicenseUrl());
                            ps.setString(4, customerobj.getInsuranceNumber());
                            ps.setString(5, customerobj.getInsuranceUrl());
                            ps.setString(6, customerobj.getInsuranceCompany());
                            ps.addBatch();
                        }).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
                            bookResponse.getDriverProfiles().add(getallData(dbFLogger, rs));
                        }).logQuery(true).throwOnNoData(false).build();
                dbm.update(logger, sq);
                bookResponse.setErrorCode("0");
                bookResponse.setErrorMessage("Data inserted successfully");
                return bookResponse;
            }
        } catch (Exception e) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in CustomerWishList " + e.getMessage());
            return bookResponse;
        }
    }

    public static UserList updateDriverProfile(BLogger logger, DBManager dbm, DriverProfile customerobj, List<Image> imageList) throws DBException {
        UserList bookResponse = new UserList();
        try {
            if (dbm == null || customerobj == null) {
                bookResponse.setErrorCode("1");
                bookResponse.setErrorMessage("Error in Channel  Request");
                return bookResponse;
            } else {
                String query = "";
                String number;
                if (!customerobj.getDrivingLicenseUrl().equalsIgnoreCase("na")) {
                    query = UPDATE_DRIVER_PROFILE;
                } else if (!customerobj.getInsuranceUrl().equalsIgnoreCase("na")) {
                    query = UPDATE_DRIVER_PROFILE_INSURANCE;
                } else {
                    number = "";
                }
                Date updateddate = new Date();
                java.sql.Date sqlupdateddate = new java.sql.Date(updateddate.getTime());
                Timestamp updateddatets = new Timestamp(sqlupdateddate.getTime());
                DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
                DBQuery sq = dbQB.setBatch().setQueryString(query).setBindInputFunction((dbLogger, ps) -> {
                    if (!customerobj.getDrivingLicenseUrl().equalsIgnoreCase("na")) {
                        ps.setString(1, customerobj.getDrivingLicenseNumber());
                        ps.setString(2, imageList.get(0).getImageUrl());
                        ps.setTimestamp(3, updateddatets);
                        ps.setInt(4, customerobj.getUserId());
                    } else {
                        ps.setString(1, customerobj.getInsuranceNumber());
                        ps.setString(2, imageList.get(0).getImageUrl());
                        ps.setString(3, customerobj.getInsuranceCompany());
                        ps.setTimestamp(4, updateddatets);
                        ps.setInt(5, customerobj.getUserId());
                        ps.addBatch();
                    }
                }).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
                    bookResponse.getDriverProfiles().add(getallData(dbFLogger, rs));
                }).logQuery(true).throwOnNoData(false).build();
                dbm.update(logger, sq);
                bookResponse.setErrorCode("0");
                bookResponse.setErrorMessage("Data inserted successfully");
                return bookResponse;
            }
        } catch (DBException e) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in Channel " + e.getMessage());
            return bookResponse;
        }
    }

    public static UserList getDriverProfileById(BLogger logger, DBManager dbm,
                                                DriverProfile resObject) throws DBException {
        UserList bookResponse = new UserList();
        if (dbm == null || resObject == null || resObject.getFromValue() == null) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Invalid Input Request");
            return bookResponse;
        }
        try {
            String queryString = "NA";
            if (resObject.getFromValue().toLowerCase().equals("id")) {
                queryString = GET_BY_DRIVER_PROFILE_ID;
            } else if (resObject.getFromValue().toLowerCase().equals("userid")) {
                queryString = GET_DRIVER_PROFILE_BY_USER_ID;
            } else {
                bookResponse.setErrorCode("1");
                bookResponse.setErrorMessage("Invalid Input Request");
                return bookResponse;
            }
            if (queryString.equals("NA")) {
                bookResponse.setErrorCode("1");
                bookResponse.setErrorMessage("Invalid Input Request");
                return bookResponse;
            }
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(queryString).setBindInputFunction((dbLogger, ps) -> {
                ps.setInt(1, resObject.getId());
            }).setFetchDataFunction((dbFLogger, rs) -> {
                bookResponse.getDriverProfiles().add(getallData(dbFLogger, rs));
            }).logQuery(true).throwOnNoData(false).build();
            dbm.select(logger, sq);
            bookResponse.setErrorCode("0");
            bookResponse.setErrorMessage("Data retrieved Succesfully");
            return bookResponse;
        } catch (Exception e) {
            bookResponse.setErrorCode("1");
            bookResponse.setErrorMessage("Error in Trip Payment  Details Request exception" + e.getMessage());
            return bookResponse;
        }
    }
}
