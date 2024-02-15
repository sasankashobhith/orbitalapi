package com.bundee.ums.db;

import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.ums.pojo.*;

import java.sql.*;
import java.util.*;

public class ApiVersionDAO {
    private static final String GET_VERSION_BY_NAME = "SELECT * FROM appversion WHERE versionname LIKE ?";

    public static UserList getAppVersion(BLogger logger, DBManager dbm, String versionName, List<ApiVersion> masterrolesList) throws DBException {
        UserList response = new UserList();
        if (dbm == null || versionName == null) {
            response.setErrorCode("1");
            response.setErrorMessage("Error in MasterRole Request");
            return response;
        }
        try {
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(GET_VERSION_BY_NAME).setBindInputFunction((dbBLogger, ps) -> {
                ps.setString(1, "%" + versionName + "%");
            }).setFetchDataFunction((dbFLogger, rs) -> {
                response.getApiVersions().add(createApiVersion(logger, rs));

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

    private static ApiVersion createApiVersion(BLogger logger, ResultSet rs) throws SQLException {
        ApiVersion roles = new ApiVersion();
        roles.setId(rs.getInt("id"));
        roles.setVersionName(rs.getString("versionname"));
        roles.setReleaseDate(rs.getString("releasedate"));
        roles.setPurpose(rs.getString("purpose"));
        roles.setCreatedDate(rs.getString("createddate"));
        roles.setActive(rs.getBoolean("istrue"));
        roles.setDescription(rs.getString("description"));
        return roles;
    }

}
