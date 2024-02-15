package com.bundee.ums.blmodule;

import com.bundee.ums.db.UserRoleMapDAO;
import com.bundee.ums.defs.UMSDefs;
import com.bundee.ums.defs.UMSProcessingCode;
import com.bundee.ums.pojo.MasterRolesRequest;
import com.bundee.ums.pojo.UserList;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.defs.UniversalConstants;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.BLModule;
import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.endpoint.BEndpoint;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.reqrespi.RequestContext;

public class UserRoleMapModule implements BLModule {
   
	@Override
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
	}
	//admin
	@BEndpoint(uri = UMSDefs.Endpoints.GET_ALL_USER_ROLE_MAPPING, httpMethod = UniversalConstants.GET, permission = UMSDefs.Permissions.ADMIN_API_KEY, reqDTOClass = MasterRolesRequest.class)
	public BaseResponse allUsers(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			MasterRolesRequest obj) throws BExceptions {
		UserList uList = new UserList();
		try {
			uList= UserRoleMapDAO.allUserroleMapping(logger, blModServices.getDBManager());
			return uList;
		} catch (DBException e) {
			throw new BExceptions(e, UMSProcessingCode.INVALID_USER);
		}
	}
	//
	@BEndpoint(uri = UMSDefs.Endpoints.ADD_USER_ROLE_MAPPING, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = MasterRolesRequest.class)
	public BaseResponse createSingleMaster(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			MasterRolesRequest  rolerequestObject) throws BExceptions {
		//MasterRoles m = MasterRolesUtil.createSingleMasterroles(rolerequestObject);
		try {
			UserList response = UserRoleMapDAO.insertuserRole(logger, blModServices.getDBManager(), rolerequestObject);
			return response;
		} catch (DBException e) {
			UserList response=new UserList();
			response.setErrorCode("1");
			response.setErrorMessage("Error in Reservaton Request");
			return response;
			
		}
		}
		//
	@BEndpoint(uri = UMSDefs.Endpoints.GET_ALL_USER_ROLE_MAPPING_BYID, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = MasterRolesRequest.class)
	public BaseResponse GetMasterRoleByIds(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			MasterRolesRequest rolesRequest) throws BExceptions {
		UserList uList = new UserList();  
		try {
			uList= UserRoleMapDAO.mapuserRolebyid(logger, blModServices.getDBManager(), rolesRequest.getUserroleid(),uList.getUserrolemap());
			return uList;
		} catch (DBException e) {
			throw new BExceptions(e, UMSProcessingCode.INVALID_USER);
		}
	}
	
	@BEndpoint(uri = UMSDefs.Endpoints.UPDATE_USER_ROLE_MAPPING, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = MasterRolesRequest.class)
	public BaseResponse updaterolemapping(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			MasterRolesRequest  requestObject) throws BExceptions {
		UserList uList = new UserList(); 
		try {
			 uList =UserRoleMapDAO.updateUserRole(logger, blModServices.getDBManager(), requestObject);
			return uList;
		} catch (DBException e) {
			//UserList uList = new UserList();
			uList.setErrorCode("1");
			uList.setErrorMessage("Error in Reservaton Request");
			return uList;
		}

	}
	
}
