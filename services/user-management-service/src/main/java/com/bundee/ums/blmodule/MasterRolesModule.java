package com.bundee.ums.blmodule;




import com.bundee.ums.db.MasterRolesDAO;
import com.bundee.ums.defs.UMSDefs;
import com.bundee.ums.defs.UMSProcessingCode;
//import com.bundee.ums.pojo.BookingResponse;
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

public class MasterRolesModule implements BLModule {
   
	@Override
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
	}
	//admin
	@BEndpoint(uri = UMSDefs.Endpoints.GET_ALL_MASTER_ROLES, httpMethod = UniversalConstants.GET, permission = UMSDefs.Permissions.ADMIN_API_KEY, reqDTOClass = MasterRolesRequest.class)
	public BaseResponse allUsers(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			MasterRolesRequest obj) throws BExceptions {
		UserList uList = new UserList();
		try {
			uList= MasterRolesDAO.allMasterRoles(logger, blModServices.getDBManager(),uList.getMasterroles());
			return uList;
		} catch (DBException e) {
			throw new BExceptions(e, UMSProcessingCode.INVALID_USER);
		}
	}
	//admin
	@BEndpoint(uri = UMSDefs.Endpoints.ADD_MASTER_ROLES, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.ADMIN_API_KEY, reqDTOClass = MasterRolesRequest.class)
	public BaseResponse createSingleMaster(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			MasterRolesRequest  rolesRequest) throws BExceptions {
		
		//MasterRoles m = MasterRolesUtil.createSingleMasterroles(rolesRequest);
		try {
			UserList response = MasterRolesDAO.insertMasteRoles(logger, blModServices.getDBManager(), rolesRequest);
			return response;
		} catch (DBException e) {
			UserList response=new UserList();
			response.setErrorCode("1");
			response.setErrorMessage("Error in MasterRole Request");
			return response;
			
		}
		}


	//admin
	@BEndpoint(uri = UMSDefs.Endpoints.UPDATE_MASTER_ROLES, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.ADMIN_API_KEY, reqDTOClass = MasterRolesRequest.class)
	public BaseResponse updateMasterRoles(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			MasterRolesRequest  rolesRequestObject) throws BExceptions {

		try {
			UserList uList = MasterRolesDAO.updateMasterRole(logger,blModServices.getDBManager(),rolesRequestObject);
			//GenericReservationResponse statusResponse =BookingStatusDAO.updateBookingStatus(logger, blModServices.getDBManager(), featureObject);
			return uList;
		} catch (DBException e) {
			UserList uList = new UserList();
			uList.setErrorCode("1");
			uList.setErrorMessage("Error in MasterRole Request");
			return uList;
		}

	}
	
//ush
	@BEndpoint(uri = UMSDefs.Endpoints.GET_MASTER_ROLES_BYID, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = MasterRolesRequest.class)
	public BaseResponse getMasterRoleByIds(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			MasterRolesRequest rolesRequest) throws BExceptions {
		UserList uList = new UserList();  
		try {
			uList= MasterRolesDAO.masterRoleById(logger, blModServices.getDBManager(), rolesRequest.getRoleid(),uList.getMasterroles());
			return uList;
		} catch (DBException e) {
			throw new BExceptions(e, UMSProcessingCode.INVALID_USER);
		}
	}
	//unh
	@BEndpoint(uri = UMSDefs.Endpoints.GET_MASTER_ROLES_BY_NAME, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = MasterRolesRequest.class)
	public BaseResponse GetMasterRole(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
										   MasterRolesRequest rolesRequest) throws BExceptions {
		UserList uList = new UserList();
		try {
			uList= MasterRolesDAO.getMasterRoles(logger, blModServices.getDBManager(), rolesRequest.getRolename());
			return uList;
		} catch (DBException e) {
			throw new BExceptions(e, UMSProcessingCode.INVALID_USER);
		}
	}
}
