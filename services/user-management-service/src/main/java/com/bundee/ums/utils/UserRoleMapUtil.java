

package com.bundee.ums.utils;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.ums.pojo.MasterRoles;
import com.bundee.ums.pojo.MasterRolesRequest;
//import com.bundee.ums.pojo.User;
//import com.bundee.ums.pojo.UserRequest;

public class UserRoleMapUtil {

	public static MasterRoles createSingleMasterroles(MasterRolesRequest row) throws BExceptions {
		MasterRoles roles = new MasterRoles();
		roles.setRoleid(row.getRoleid());
		roles.setRolename(row.getRolename());
		roles.setCreateddate(row.getCreateddate());
		roles.setIsactive(row.getIsactive());
		
		
		return roles;
	}
	
}