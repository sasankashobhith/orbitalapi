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
import com.bundee.ums.db.PushNotificationsDAO;
import com.bundee.ums.defs.UMSDefs;
import com.bundee.ums.pojo.LoginRequest;
import com.bundee.ums.pojo.PushNotificationsResponse;
import com.bundee.ums.pojo.UserList;
import com.bundee.ums.utils.PushNotificationsUtill;

public class PushNotificationModule implements BLModule {

	@Override
	public void init(BLogger logger, BLModServices blModServices){

	}
//usehost
	@BEndpoint(uri = UMSDefs.Endpoints.INSERT_INTO_PUSHNOTIFICATIONS, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = PushNotificationsResponse.class)
	public BaseResponse createPushNotifications(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			PushNotificationsResponse resObject){

		PushNotificationsResponse reservationCreateObj = PushNotificationsUtill.createSinglePushNotification(resObject);

		UserList userresponse = new UserList();
		try {
			userresponse = PushNotificationsDAO.insertintopushnotifications(logger, blModServices.getDBManager(),
					reservationCreateObj);
		} catch (DBException e) {
			e.printStackTrace();
		} catch (BExceptions e) {
			e.printStackTrace();
		}
		return userresponse;

	}
//admin
	@BEndpoint(uri = UMSDefs.Endpoints.GET_ALL_PUSHNOTIFICXATIONS, httpMethod = UniversalConstants.GET, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY)
	public BaseResponse getAllPushnotifications(BLogger logger, BLModServices blModServices, RequestContext reqCtx)
			throws BExceptions {

		UserList vList = new UserList();
		try {
			vList = PushNotificationsDAO.getAllPushNotifications(logger, blModServices.getDBManager(), vList);
			return vList;
		} catch (DBException e) {
			vList.setErrorCode("1");
			vList.setErrorMessage("Error in CustomerActivity Request");
			return vList;
		}

	}
//unh
	@BEndpoint(uri = UMSDefs.Endpoints.GET_PUSHNOTIFICATIONS_BY_ID, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = LoginRequest.class)
	public BaseResponse getPushnotificationsbyid(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
			LoginRequest requestObject){
		UserList vList = new UserList();

		try {
			vList = PushNotificationsDAO.getpushnotificationsbyid(logger, blModServices.getDBManager(), requestObject,
					vList.getPushnotifications());
		} catch (DBException | BExceptions e) {
			UserList bookResponse = new UserList();
			bookResponse.setErrorCode("1");
			bookResponse.setErrorMessage("Error in PushNotifications Request");
			return bookResponse;
		}

		return vList;
	}
	
//uhost
		@BEndpoint(uri = UMSDefs.Endpoints.UPDATE_PUSHnOTIFICATION_DEVICETOKEN_BYUSERID, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = PushNotificationsResponse.class)
		public BaseResponse updatePushNotifications(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
				PushNotificationsResponse featureObject){

			try {
				UserList genericresponse = PushNotificationsDAO.updatedevicetokenbyuserid(logger, blModServices.getDBManager(),
						featureObject);
				return genericresponse;
			} catch (DBException | BExceptions e) {
				UserList bookResponse = new UserList();
				bookResponse.setErrorCode("1");
				bookResponse.setErrorMessage("Error in PushNotification Request");
				return bookResponse;
			}

		}
		//hostuser
	@BEndpoint(uri = UMSDefs.Endpoints.UPDATE_PUSHNOTIFICATIONS, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = PushNotificationsResponse.class)
	public BaseResponse updatePushNotification(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
												PushNotificationsResponse featureObject){

		try {
			UserList genericresponse = PushNotificationsDAO.updateDeviceToken(logger, blModServices.getDBManager(),featureObject);
			return genericresponse;
		} catch (DBException | BExceptions e) {
			UserList bookResponse = new UserList();
			bookResponse.setErrorCode("1");
			bookResponse.setErrorMessage("Error in PushNotification Request");
			return bookResponse;
		}

	}

}
