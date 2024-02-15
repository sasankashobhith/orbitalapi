package com.bundee.ums.blmodule;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.defs.UniversalConstants;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.BLModule;
import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.endpoint.BEndpoint;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.reqrespi.RequestContext;
import com.bundee.ums.db.OwnerEmployeeDAO;
import com.bundee.ums.db.PushNotificationsDAO;
import com.bundee.ums.defs.UMSDefs;
import com.bundee.ums.pojo.LoginRequest;
import com.bundee.ums.pojo.OwnerEmployeeMappingResponse;
import com.bundee.ums.pojo.PushNotificationsResponse;
import com.bundee.ums.pojo.UserList;
import com.bundee.ums.utils.OwnerUtill;

public class OwnerEmployeeModule implements BLModule {

	@Override
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions {

	}
	
	

	@BEndpoint(uri = UMSDefs.Endpoints.INSERT_INTO_OWNEREMPLOYEE, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = OwnerEmployeeMappingResponse.class)
	public BaseResponse createOwnerEmployee(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			OwnerEmployeeMappingResponse resObject) throws BExceptions {

		OwnerEmployeeMappingResponse reservationCreateObj = OwnerUtill.createsingleowner(resObject);

		UserList userresponse = new UserList();
		try {
			userresponse = OwnerEmployeeDAO.insertOwnerEmployeeMapping(logger, blModServices.getDBManager(),
					reservationCreateObj);
		} catch (DBException e) {
			e.printStackTrace();
		} catch (BExceptions e) {
			e.printStackTrace();
		}
		return userresponse;

	}
	
	
	
	@BEndpoint(uri = UMSDefs.Endpoints.GET_ALL_OWNEREMPLOYEEMAPPING, httpMethod = UniversalConstants.GET, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY)
	public BaseResponse getAllOwnerEmployeeMapping(BLogger logger, BLModServices blModServices, RequestContext reqCtx)

			throws BExceptions {

		UserList vList = new UserList();
		try {
			vList = OwnerEmployeeDAO.getAllOwnerEmployee(logger, blModServices.getDBManager(), vList);
			return vList;
		} catch (DBException e) {
			vList.setErrorCode("1");
			vList.setErrorMessage("Error in OwnerEmployee Request");
			return vList;
		}

	}
	
	
	
	
	@BEndpoint(uri = UMSDefs.Endpoints.GET_OWNEREMPLOYEE_BY_ID, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = LoginRequest.class)
	public BaseResponse getOwnerEmployeebyId(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			LoginRequest requestObject) throws BExceptions {
		UserList vList = new UserList();

		try {
			vList = OwnerEmployeeDAO.getOwnerEmployeebyid(logger, blModServices.getDBManager(), requestObject,
					vList.getOwneremployeemapping());
		} catch (DBException e) {
			UserList bookResponse = new UserList();
			bookResponse.setErrorCode("1");
			bookResponse.setErrorMessage("Error in OwnerEmployee Request");
			return bookResponse;
		}

		return vList;
	}
	
	
	@BEndpoint(uri = UMSDefs.Endpoints.UPDATE_OWNER_EMPLOYEE, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = OwnerEmployeeMappingResponse.class)
	public BaseResponse updateOwnerEmployeee(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			OwnerEmployeeMappingResponse featureObject) throws BExceptions {

		try {
			UserList genericresponse = OwnerEmployeeDAO.upadteOwnerEmployee(logger, blModServices.getDBManager(),
					featureObject);
			return genericresponse;
		} catch (DBException e) {
			UserList bookResponse = new UserList();
			bookResponse.setErrorCode("1");
			bookResponse.setErrorMessage("Error in OwnerEmployee Request");
			return bookResponse;
		}

	}
	
	
	

}
