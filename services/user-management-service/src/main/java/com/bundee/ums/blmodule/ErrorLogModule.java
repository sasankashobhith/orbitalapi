package com.bundee.ums.blmodule;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.blmodi.*;
import com.bundee.msfw.interfaces.endpoint.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.msfw.interfaces.reqrespi.*;
import com.bundee.ums.db.*;
import com.bundee.ums.defs.*;
import com.bundee.ums.pojo.*;

public class ErrorLogModule implements BLModule {
    String tokenName = "bundee_auth_token";
    String token;

    @Override
    public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
    }

    @BEndpoint(uri = UMSDefs.Endpoints.INSERT_ERROR_LOG, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = ErrorLog.class)
    public BaseResponse getAppVersion(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                      ErrorLog rolesRequestObject) throws BExceptions {
        UserList uList = new UserList();
        try {
            uList = ErrorLogDao.insertErrorLog(logger, blModServices.getDBManager(), rolesRequestObject.getErrorLog());
            return uList;
        } catch (Exception e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("error in Application Request");
            return uList;
        }
    }
}
