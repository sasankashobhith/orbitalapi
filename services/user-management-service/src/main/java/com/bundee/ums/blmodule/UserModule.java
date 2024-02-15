package com.bundee.ums.blmodule;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.blmodi.*;
import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.endpoint.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.msfw.interfaces.reqrespi.*;
import com.bundee.msfw.interfaces.restclienti.*;
import com.bundee.ums.db.*;
import com.bundee.ums.defs.*;
import com.bundee.ums.pojo.*;
import com.bundee.ums.utils.*;

public class UserModule implements BLModule {
    String baseurlBooking;
    String baseurlAvailability;
    String baseurlHost;
    String baseurlUser;
    RESTClient reservationclient;
    String tokenName = "bundee_auth_token";
    String token;

    @Override
    public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
        baseurlBooking = blModServices.getFileCfgHandler().getCfgParamStr("reservationclient.baseurlBooking");
        baseurlAvailability = blModServices.getFileCfgHandler().getCfgParamStr("reservationclient.baseurlAvailability");
        baseurlHost = blModServices.getFileCfgHandler().getCfgParamStr("reservationclient.baseurlHost");
        baseurlUser = blModServices.getFileCfgHandler().getCfgParamStr("reservationclient.baseurlUser");
        reservationclient = blModServices.getRESTClientFactory().getNewRESTClient(logger, "reservationclientid", "reservationclient", blModServices);
    }


    @BEndpoint(uri = UMSDefs.Endpoints.CHECK_LOGIN, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = LoginRequest.class)
    public BaseResponse ValidateUser(BLogger logger, BLModServices blModServices, RequestContext reqCtx, LoginRequest loginObject) throws BExceptions {
        logger.debug(loginObject.getEmail().toString());
        UserList uList = new UserList();
        try {
            UserDAO.validateUser(logger, blModServices.getDBManager(), loginObject, uList.getUserdetail());
        } catch (DBException e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection ");
            return uList;
        }
        return uList;
    }

    @BEndpoint(uri = UMSDefs.Endpoints.COUNT_BY_ID, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = UserRequest.class)
    public BaseResponse CountById(BLogger logger, BLModServices blModServices, RequestContext reqCtx, UserRequest countobject) throws BExceptions {
        UserList uList = new UserList();
        try {
            try {
                uList = UserDAO.countbyid(logger, blModServices.getDBManager(), countobject.getId());
                return uList;
            } catch (DBException e) {
                throw new RuntimeException(e);
            }
        } catch (BExceptions e) {
            //uList.setErrorCode("1");
            //uList.setErrorMessage("No database connection ");
            //return uList;
            throw e;
        }
    }

    @BEndpoint(uri = UMSDefs.Endpoints.COUNT_BY_USERIDS, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = UserRequest.class)
    public BaseResponse GetUserByIds(BLogger logger, BLModServices blModServices, RequestContext reqCtx, UserRequest countobject) throws BExceptions {
        UserList uList = new UserList();
        try {
            uList = UserDAO.getbyuserIds(logger, blModServices.getDBManager(), countobject.getUserIds());
            return uList;
        } catch (DBException e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection ");
            return uList;
        }
    }



    @BEndpoint(uri = UMSDefs.Endpoints.UPDATE_USER, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = UserResponse.class)
    public BaseResponse updateUser(BLogger logger, BLModServices blModServices, RequestContext reqCtx, UserResponse userObject) throws BExceptions {
        UserList userResponse = new UserList();
        try {
            if (userObject.getFromValue().equals("basicProfile")) {
                userResponse = UserDAO.updateUserProfile(logger, blModServices.getDBManager(), userObject);
            } else if (userObject.getFromValue().equals("completeProfile")) {
                userResponse = UserDAO.updateUser(logger, blModServices.getDBManager(), userObject);
            }
            return userResponse;
        } catch (DBException e) {
            userResponse.setErrorCode("1");
            userResponse.setErrorMessage("No database connection ");
            return userResponse;
        }
    }

    @BEndpoint(uri = UMSDefs.Endpoints.UPDATE_USER_TOKEN, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = UserResponse.class)
    public BaseResponse updateUserToken(BLogger logger, BLModServices blModServices, RequestContext reqCtx, UserResponse userObject) throws BExceptions {
        UserList userResponse = new UserList();
        try {
            userResponse = UserDAO.updateUserToken(logger, blModServices.getDBManager(), userObject);
            return userResponse;
        } catch (DBException e) {
            userResponse.setErrorCode("1");
            userResponse.setErrorMessage("No database connection ");
            return userResponse;
        }
    }

    //admin
    @BEndpoint(uri = UMSDefs.Endpoints.ALL_USERS, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = UserRequest.class)
    public BaseResponse allUsers(BLogger logger, BLModServices blModServices, RequestContext reqCtx, UserRequest obj) throws BExceptions {
        UserList uList = new UserList();
        try {
            uList = UserDAO.allUsers(logger, blModServices.getDBManager());
            return uList;
        } catch (DBException e) {
            throw new BExceptions(e, UMSProcessingCode.INVALID_USER);
        }
    }

    //usernh
    @BEndpoint(uri = UMSDefs.Endpoints.GET_BY_FIREBASE_IDS, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = UserRequest.class)
    public BaseResponse GetByFirebaseIds(BLogger logger, BLModServices blModServices, RequestContext reqCtx, UserRequest countobject) throws BExceptions {
        UserList uList = new UserList();
        try {
            uList = UserDAO.getDataByFirebase(logger, blModServices.getDBManager(), countobject.getUserIds());
            return uList;
        } catch (DBException e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection ");
            return uList;
        }
    }

    @BEndpoint(uri = UMSDefs.Endpoints.ONBOARD_HOST, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = LoginRequest.class)
    public BaseResponse OnBoardHost(BLogger logger, BLModServices blModServices, RequestContext reqCtx, LoginRequest loginObject) throws BExceptions {
        token = reqCtx.getInHeaders().get(tokenName).get(0);
        UserList uList = new UserList();
        try {
            uList = UserDAO.onBoardHost(logger, blModServices.getDBManager(), loginObject.getUserIds());
            return uList;
        } catch (Exception e) {
            uList.setErrorCode("0");
            uList.setErrorMessage("Error in Request " + e.getMessage());
            return uList;
        }
    }

}
