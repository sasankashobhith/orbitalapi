package com.bundee.ums.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.dbi.DBQueryBuilder;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.ums.pojo.LoginRequest;
import com.bundee.ums.pojo.OwnerEmployeeMappingResponse;
import com.bundee.ums.pojo.PushNotificationsResponse;
import com.bundee.ums.pojo.UserList;

public class OwnerEmployeeDAO {

	private static final String INSERT_INTO_OWNER_EMPLOYEE_MAPPING = "insert into owneremployeemapping (userid,roleid,createdby,updatedby,isactive) values (?,?,?,?,?)";

	private static final String GET_ALL_OWNER_EMPLOYEE = "select * from owneremployeemapping where isactive=true";

	private static final String GET_OWNEREMPLOYEEMAPPING_BYID = "SELECT * FROM owneremployeemapping WHERE id=? and isactive=true";

	private static final String GET_OWNEREMPLOYEEMAPPING_BY_USERID = "SELECT * FROM owneremployeemapping WHERE userid=? and isactive=true";

	private static final String UPDATE_OWNEREMPLOYEE_BYID = "UPDATE owneremployeemapping  set roleid=?,updatedate=?,isactive=? WHERE id=? and isactive=true";

	public static UserList insertOwnerEmployeeMapping(BLogger logger, DBManager dbm,

			OwnerEmployeeMappingResponse customerobj) throws DBException {
		UserList bookResponse = new UserList();
		try {
			if (dbm == null || customerobj == null||customerobj.getUserid()==0||customerobj.getRoleid()==0||customerobj.getUpdatedby()==0) {
				bookResponse.setErrorCode("1");
				bookResponse.setErrorMessage("Error in OwnerEmployee  Request");
				return bookResponse;
			} else {
				DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
				DBQuery sq = dbQB.setBatch().setQueryString(INSERT_INTO_OWNER_EMPLOYEE_MAPPING)
						.setBindInputFunction((dbLogger, ps) -> {

							ps.setInt(1, customerobj.getUserid());
							ps.setInt(2, customerobj.getRoleid());
							ps.setInt(3, customerobj.getCreatedby());
							ps.setInt(4, customerobj.getUpdatedby());
							ps.setBoolean(5, customerobj.getisIsactive());

							ps.addBatch();
						}).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
							bookResponse.getOwneremployeemapping().add(createOwnerEmployee(dbFLogger, rs));
						}).logQuery(true).throwOnNoData(false).build();
				dbm.update(logger, sq);
				bookResponse.setErrorCode("0");
				bookResponse.setErrorMessage("Data inserted successfully");
				return bookResponse;

			}
		} catch (Exception e) {
			bookResponse.setErrorCode("1");
			bookResponse.setErrorMessage("Error in Master Request" + e.getMessage());
			return bookResponse;
		}
	}

	public static UserList getAllOwnerEmployee(BLogger logger, DBManager dbm, UserList vehiclesList)
			throws DBException {
		try {
			DBQueryBuilder dbQB = dbm.getDBQueryBuilder();

			DBQuery sq = dbQB.setQueryString(GET_ALL_OWNER_EMPLOYEE).setBindInputFunction((dbBLogger, ps) -> {
			}).setFetchDataFunction((dbFLogger, rs) -> {
				vehiclesList.getOwneremployeemapping().add(createOwnerEmployee(dbFLogger, rs));
			}).logQuery(true).throwOnNoData(false).build();
			dbm.select(logger, sq);
			vehiclesList.setErrorCode("0");
			vehiclesList.setErrorMessage("Data OwnerEmployee successfully");
			return vehiclesList;
		} catch (Exception e) {
			vehiclesList.setErrorMessage("Error retrieving all OwnerEmployee details - " + e.getMessage());
			vehiclesList.setErrorCode("1");
			return vehiclesList;
		}
	}

	private static OwnerEmployeeMappingResponse createOwnerEmployee(BLogger logger, ResultSet rs)

			throws SQLException {

		OwnerEmployeeMappingResponse customeractivity = new OwnerEmployeeMappingResponse();

		customeractivity.setId(rs.getInt("id"));
		customeractivity.setUserid(rs.getInt("userid"));
		customeractivity.setRoleid(rs.getInt("roleid"));
		customeractivity.setCreateddate(rs.getString("createddate"));
		customeractivity.setUpdateddate(rs.getString("updatedate"));
		customeractivity.setCreatedby(rs.getInt("createdby"));
		customeractivity.setUpdatedby(rs.getInt("updatedby"));

		customeractivity.setIsactive(rs.getBoolean("isactive"));

		return customeractivity;
	}

	public static UserList getOwnerEmployeebyid(BLogger logger, DBManager dbm, LoginRequest resObject,

			List<OwnerEmployeeMappingResponse> owneremployeemappingresponse) throws DBException {
		UserList bookResponse = new UserList();

		if (dbm == null || resObject == null || resObject.getFromvalue() == null || resObject.getId() == 0) {
			bookResponse.setErrorCode("1");
			bookResponse.setErrorMessage("Invalid Input Request");
			return bookResponse;
		}
		try {

			String queryString = "NA";

			if (resObject.getFromvalue().toLowerCase().equals("id")) {
				queryString = GET_OWNEREMPLOYEEMAPPING_BYID;
			} else if (resObject.getFromvalue().toLowerCase().equals("userid")) {
				queryString = GET_OWNEREMPLOYEEMAPPING_BY_USERID;

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

				if (resObject.getFromvalue().toLowerCase().equals("id")) {
					ps.setInt(1, resObject.getId());
				}

				else if (resObject.getFromvalue().toLowerCase().equals("userid")) {
					ps.setInt(1, resObject.getId());

				}

			}).setFetchDataFunction((dbFLogger, rs) -> {

				owneremployeemappingresponse.add(createOwnerEmployee(dbFLogger, rs));

			}).logQuery(true).throwOnNoData(false).build();

			dbm.select(logger, sq);
			bookResponse.setErrorCode("0");
			bookResponse.setErrorMessage("Data retrieved Succesfully");
			bookResponse.setOwneremployeemapping(owneremployeemappingresponse);
			return bookResponse;

		} catch (Exception e) {
			bookResponse.setErrorCode("1");
			bookResponse.setErrorMessage("Error in OwnerEmmployee Request exception" + e.getMessage());
			return bookResponse;
		}
	}

	public static UserList upadteOwnerEmployee(BLogger logger, DBManager dbm,
			OwnerEmployeeMappingResponse owneremployeemappingresponse) throws DBException {
		UserList bookResponse = new UserList();
		if (dbm == null || owneremployeemappingresponse == null||owneremployeemappingresponse.getRoleid()==0||owneremployeemappingresponse.getId()==0) {
			bookResponse.setErrorCode("1");
			bookResponse.setErrorMessage("Error in PushNotification  Request");
			return bookResponse;
		}

		DBQueryBuilder dbQB = dbm.getDBQueryBuilder();

		try {

			Date updateddeatail = new SimpleDateFormat("yyyy-MM-dd")
					.parse(owneremployeemappingresponse.getUpdateddate().toString());
			java.sql.Date sqlStartDate = new java.sql.Date(updateddeatail.getTime());
			java.sql.Timestamp updatedate = new java.sql.Timestamp(sqlStartDate.getTime());
			DBQuery sq = dbQB.setBatch().setQueryString(UPDATE_OWNEREMPLOYEE_BYID)
					.setBindInputFunction((dbLogger, ps) -> {
						{

							ps.setInt(1, owneremployeemappingresponse.getRoleid());
							ps.setTimestamp(2, updatedate);
							ps.setBoolean(3, owneremployeemappingresponse.getisIsactive());
							ps.setInt(4, owneremployeemappingresponse.getId());

							ps.addBatch();
						}
					}).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
						bookResponse.getOwneremployeemapping().add(createOwnerEmployee(dbFLogger, rs));
					}).logQuery(true).build();

			dbm.update(logger, sq);
			bookResponse.setErrorCode("0");
			bookResponse.setErrorMessage("Data updated Succesfully");

			return bookResponse;
		}

		catch (Exception e) {
			bookResponse.setErrorCode("1");
			bookResponse.setErrorMessage("Error in OwnerEmployee  Request exception" + e.getMessage());
			return bookResponse;
		}
	}

}
