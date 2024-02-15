package com.bundee.ums.blmodule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.defs.UniversalConstants;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.BLModule;
import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.dbi.DBQueryBuilder;
import com.bundee.msfw.interfaces.endpoint.BEndpoint;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.reqrespi.RequestContext;
import com.bundee.ums.db.MobileConfDAO;
import com.bundee.ums.defs.UMSDefs;
import com.bundee.ums.pojo.LoginRequest;
import com.bundee.ums.pojo.MobileConfResponse;
import com.bundee.ums.pojo.UserList;

public class MobileConfblModule implements BLModule {
	
	@Override
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
		// TODO Auto-generated method stub
		
	}
	//admin
	@BEndpoint(uri = UMSDefs.Endpoints.GET_ALL_MOBILECONF, httpMethod = UniversalConstants.GET, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY)
    public BaseResponse getAll(BLogger logger, BLModServices blModServices, RequestContext reqCtx) throws BExceptions {
		UserList uList = new UserList();
        try {
        	uList = MobileConfDAO.geAllMobileConf(logger, blModServices.getDBManager());
           return uList;
        } catch (DBException e) {

        	uList.setErrorCode("1");
        	uList.setErrorMessage("Error in CustomerActivity Request");
           return uList;
        }
    }
//

	@BEndpoint(uri = UMSDefs.Endpoints.GET_ALL_MOBILE_CONF_BY_ID, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = LoginRequest.class)

    public BaseResponse getAllMobileDetailsbyId(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
    		LoginRequest requestObject) throws BExceptions {
		UserList uList = new UserList();
        try {
            uList = MobileConfDAO.getMobileDetailsById(logger, blModServices.getDBManager()
                    , requestObject);
            return uList;
        } catch (DBException e) {
           
        	uList.setErrorCode("1");
        	uList.setErrorMessage("Error in Mobile conf Request");
            return uList;
        }
       
    }
	
	//
	@BEndpoint(uri = UMSDefs.Endpoints.INSERT_MOBILE_DETAILS, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = MobileConfResponse.class)

    public BaseResponse insertPrice(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
    		MobileConfResponse requestObject) throws BExceptions {
        UserList uList = new UserList();
        try {
            uList = MobileConfDAO.insertMobileDetails(logger, blModServices.getDBManager()
                    , requestObject);
            return uList;
        } catch (DBException e) {
           
        	uList.setErrorCode("1");
        	uList.setErrorMessage("Error in Image Request");
            return uList;
        }
       
    }
	//
	@BEndpoint(uri = UMSDefs.Endpoints.UPDATE_MOBILE_DETAILS, httpMethod = UniversalConstants.POST, permission = "", reqDTOClass = MobileConfResponse.class)

    public BaseResponse updateMobileDetails(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
    		MobileConfResponse requestObject) throws BExceptions {
        UserList uList = new UserList();
        try {
            uList = MobileConfDAO.updateMobileDetails(logger, blModServices.getDBManager()
                    , requestObject);
            return uList;
        } catch (Exception e) {
           
        	uList.setErrorCode("1");
        	uList.setErrorMessage("Error in update Request");
            return uList;
        }
       
    }
	
	

}
