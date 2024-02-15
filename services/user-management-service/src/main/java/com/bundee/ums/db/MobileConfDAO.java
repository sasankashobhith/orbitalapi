package com.bundee.ums.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.dbi.DBQueryBuilder;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.ums.pojo.LoginRequest;
import com.bundee.ums.pojo.MobileConfResponse;
import com.bundee.ums.pojo.UserList;


	

public class MobileConfDAO {
	
	private static final String GET_ALL="select * from mobileconfjsonstring";
	private static final String GET_BY_ID="select * from mobileconfjsonstring where id=?";
	private static final String GET_BY_HOSTID="select * from mobileconfjsonstring where hostid=?";
	private static final String GET_BY_CHANNELID="select * from mobileconfjsonstring where channelid=?";
	private static final String INSERT_MOBILE_DETAILS="insert into mobileconfjsonstring (inputstr, hostid, channelid) values (CAST(? AS jsonb),?,?)";
	private static final String UPDATE_MOBILE_DETAILS="UPDATE mobileconfjsonstring SET inputstr = CAST(? AS jsonb), updateddate = ? WHERE id = ?";
	
	public static UserList geAllMobileConf(BLogger logger, DBManager dbm)
			throws DBException {
		UserList mobileconf = new UserList();
        try {

            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(GET_ALL).setBindInputFunction((dbBLogger, ps) -> {
            }).setFetchDataFunction((dbFLogger, rs) -> {
            	mobileconf.getMobileconfigration().add(getAllResponse(dbFLogger, rs));
            }).logQuery(true).throwOnNoData(false).build();
            dbm.select(logger, sq);
            
            
            mobileconf.setErrorCode("0");
            mobileconf.setErrorMessage("Data retrieved successfully");
            return mobileconf;

        } catch (Exception e) {
        	mobileconf.setErrorMessage("Error retrieving Mobile details - " + e.getMessage());
        	mobileconf.setErrorCode("1");
            return mobileconf;
        }

    }
	
	public static UserList getMobileDetailsById(BLogger logger, DBManager dbm,
			LoginRequest resObject) throws DBException {
		UserList mobileConfDetails = new UserList();
        List<MobileConfResponse> mobileconf = new ArrayList<MobileConfResponse>();
        if (dbm == null || resObject == null || resObject.getFromvalue() == null ) {
        	mobileConfDetails.setErrorCode("1");
        	mobileConfDetails.setErrorMessage("Invalid Input Request");
            return mobileConfDetails;
        }
        try {
            String queryString = "NA";	 
            if (resObject.getFromvalue().toLowerCase().equals("id")) {
                queryString = GET_BY_ID;
            } else if (resObject.getFromvalue().toLowerCase().equals("hostid")) {
                queryString = GET_BY_HOSTID;
            } else if (resObject.getFromvalue().toLowerCase().equals("channelid")) {
                queryString = GET_BY_CHANNELID;
            } else {
            	mobileConfDetails.setErrorCode("1");
            	mobileConfDetails.setErrorMessage("Invalid Input Request");
                return mobileConfDetails;
            }
            if (queryString.equals("NA")) {
            	mobileConfDetails.setErrorCode("1");
            	mobileConfDetails.setErrorMessage("Invalid Input Request");
               return mobileConfDetails;

            }
            DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
            DBQuery sq = dbQB.setQueryString(queryString).setBindInputFunction((dbLogger, ps) -> {
                if (resObject.getFromvalue().toLowerCase().equals("id")){
                	  ps.setInt(1, resObject.getId());
                } 
                else if (resObject.getFromvalue().toLowerCase().equals("hostid")) {
                    ps.setInt(1, resObject.getId());
                }
                else if (resObject.getFromvalue().toLowerCase().equals("channelid")) {
                    ps.setInt(1, resObject.getId());
                }
            }).setFetchDataFunction((dbFLogger, rs) -> {
            	mobileconf.add(getAllResponse(dbFLogger, rs));
            }).logQuery(true).throwOnNoData(false).build();
            dbm.select(logger, sq);
            mobileConfDetails.setErrorCode("0");
            mobileConfDetails.setErrorMessage("Data retrieved Succesfully");
            mobileConfDetails.setMobileconfigration(mobileconf);
            return mobileConfDetails;
        } catch (Exception e) {
        	mobileConfDetails.setErrorCode("1");
        	mobileConfDetails.setErrorMessage("Error in Mobile Details Request exception" + e.getMessage());
            return mobileConfDetails;
        }
    }
	
	private static MobileConfResponse getAllResponse(BLogger logger, ResultSet rs) throws SQLException {
		MobileConfResponse mobileconf = new MobileConfResponse();
		mobileconf.setId(rs.getInt("id"));
		mobileconf.setHostid(rs.getInt("hostid"));
		mobileconf.setChannelid(rs.getInt("channelid"));	
		mobileconf.setCreateddate(rs.getString("createddate"));
		mobileconf.setUpdateddate(rs.getString("updateddate"));
		mobileconf.setIsactive(rs.getBoolean("isactive"));
		mobileconf.setInputstr(rs.getString("inputstr"));
	
	return mobileconf;
	}

	
	public static UserList insertMobileDetails(BLogger logger, DBManager dbm, MobileConfResponse resobj)
			throws DBException {
		UserList mobileConfDetails = new UserList();
		List<MobileConfResponse> mobileconf = new ArrayList<MobileConfResponse>();
		try {
			if (dbm == null || resobj == null||resobj.getInputstr()== null||resobj.getHostid()==0||resobj.getChannelid()==0) {
				mobileConfDetails.setErrorCode("1");
				mobileConfDetails.setErrorMessage("Error in Mobile details Insertion Request");
				return mobileConfDetails;
			} else {
				DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
				DBQuery sq = dbQB.setBatch().setQueryString(INSERT_MOBILE_DETAILS)
						.setBindInputFunction((dbLogger, ps) -> {
							
							ps.setString(1, resobj.getInputstr());	
							ps.setInt(2, resobj.getHostid());
							ps.setInt(3, resobj.getChannelid());
							
									
							ps.addBatch();

						}).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
							mobileconf.add(getAllResponse(logger, rs));
						}).logQuery(true).build();
				dbm.update(logger, sq);
				mobileConfDetails.setErrorCode("0");
				mobileConfDetails.setErrorMessage("Successfully Mobile details Inserted");
				mobileConfDetails.setMobileconfigration(mobileconf);
				return mobileConfDetails;

			}
		} catch (DBException e) {
			// TODO Auto-generated catch block
			mobileConfDetails.setErrorCode("1");
			mobileConfDetails.setErrorMessage("Error in Inserting " + e.getMessage().toString());
			return mobileConfDetails;
		}
	}
	
	
	public static UserList updateMobileDetails(BLogger logger, DBManager dbm, MobileConfResponse resobj) {
		
		UserList mobileConfDetails = new UserList();
		List<MobileConfResponse> mobileconf = new ArrayList<MobileConfResponse>();
		if (dbm == null || resobj == null||resobj.getId()==0) {
			mobileConfDetails.setErrorCode("1");
			mobileConfDetails.setErrorMessage("Error in Update Mobile details Request");
			return mobileConfDetails;
		} else {
			
			Date updateDate = new Date();
			java.sql.Date sqlupdateDate = new java.sql.Date(updateDate.getTime());
			java.sql.Timestamp sqlupdateDateTime = new java.sql.Timestamp(sqlupdateDate.getTime());
			
			try {
					
					DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
					DBQuery sq = dbQB.setBatch().setQueryString(UPDATE_MOBILE_DETAILS)
							.setBindInputFunction((dbLogger, ps) -> {

								ps.setString(1, resobj.getInputstr());
								ps.setTimestamp(2, sqlupdateDateTime);
								ps.setInt(3, resobj.getId() );
								ps.addBatch();
								
							}).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
								mobileconf.add(getAllResponse(logger, rs));
							}).logQuery(true).throwOnNoData(false).build();
					dbm.update(logger, sq);
					
					mobileConfDetails.setErrorCode("0");
					mobileConfDetails.setErrorMessage("Successfully UPDATED");
					mobileConfDetails.setMobileconfigration(mobileconf);
					return mobileConfDetails;
					
					
			} catch (Exception e) {
				mobileConfDetails.setErrorCode("1");
				mobileConfDetails.setErrorMessage("Invalid Date format " + e.getMessage().toString());
				return mobileConfDetails;
			}

		}

	}
	
	
	
}
