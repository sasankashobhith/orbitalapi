package com.bundee.ums.utils;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.ums.pojo.MasterRoles;
import com.bundee.ums.pojo.MasterRolesRequest;
//import com.bundee.ums.pojo.User;
//import com.bundee.ums.pojo.UserRequest;
import com.bundee.ums.pojo.UserRoleMap;

public class MasterRolesUtil {

	public static UserRoleMap createSingleUserRoleMap(MasterRolesRequest row) throws BExceptions {
		UserRoleMap roles = new UserRoleMap();
		roles.setId(row.getUserroleid());
		roles.setRoleid(row.getRoleid());
		
		roles.setUserid(row.getUserid());
		roles.setCreateddate(row.getCreateddate());
		roles.setUpdateddate(row.getUpdatedate());
		roles.setCreatedby(row.getCreatedby());
		roles.setUpdatedby(row.getUpdatedby());
		roles.setIsactive(row.getIsactive());
		
		
		return roles;
	}
	
}
