package com.bundee.ums.utils;
import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.dbi.DBQueryBuilder;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.restclienti.*;
import com.bundee.ums.defs.*;
import com.bundee.ums.pojo.*;
import org.json.*;

import java.util.*;

public class Util {

	
	public static User createSingleUser(UserRequest row) throws BExceptions {
		User user = new User();
		
		user.setEmailid(row.getEmail());
		user.setfirstname(row.getFirstName());
		user.setlastname(row.getLastName());
		return user;
	}
	public static Boolean validateUserById(int userid, BLogger logger, DBManager dbm){
		try {
			String query = "select iduser, firstname from mastercustomer where iduser=?";
			User user=new User();
			DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
			DBQuery sq = dbQB.setBatch().setQueryString(query).setBindInputFunction((dbLogger, ps) -> {
				ps.setInt(1, userid);
				ps.addBatch();
			}).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
				user.setfirstname(new UTF8String(rs.getString("firstname")));
			}).logQuery(true).build();
			dbm.select(logger, sq);
			if(user.getfirstname()!= null) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (Exception e){
			return false;
		}
	}

}
