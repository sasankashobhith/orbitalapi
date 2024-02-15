package com.bundee.ums.db;

import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.ums.pojo.*;

import java.sql.*;
import java.util.*;

public class ErrorLogDao {
    private static final String INSERT_ERROR_LOG = "INSERT INTO errorlog(error) VALUES ( ?)";

    public static UserList insertErrorLog(BLogger logger, DBManager dbm, String discription) throws DBException {
        UserList response = new UserList();
        if (dbm == null || discription == null) {
            response.setErrorCode("1");
            response.setErrorMessage("Error in MasterRole Request");
            return response;
        }
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(INSERT_ERROR_LOG).setBindInputFunction((dbBLogger, ps) -> {
                ps.setString(1,   discription );
            }).setFetchDataFunction((dbFLogger, rs) -> {
                response.getErrorLogs().add(createErrorLog(logger, rs));
            }).logQuery(true).setReturnKeys().throwOnNoData(false).build();
            dbm.update(logger, sq);
            response.setErrorCode("0");
            response.setErrorMessage("Data retrieved Successfully");
            return response;
        } catch (Exception e) {
            response.setErrorCode("1");
            response.setErrorMessage("Error in MasterRole Request exception" + e.getMessage());
            return response;
        }

    }

    private static ErrorLog createErrorLog(BLogger logger, ResultSet rs) throws SQLException {
        ErrorLog roles = new ErrorLog();
        roles.setId(rs.getInt("id"));
        roles.setErrorLog(rs.getString("error"));
        return roles;
    }

}
