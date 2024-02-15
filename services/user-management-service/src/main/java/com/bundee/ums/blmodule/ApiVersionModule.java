package com.bundee.ums.blmodule;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.blmodi.*;
import com.bundee.msfw.interfaces.endpoint.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.msfw.interfaces.reqrespi.*;
import com.bundee.ums.db.*;
import com.bundee.ums.defs.*;
import com.bundee.ums.pojo.*;

import java.util.*;

public class ApiVersionModule implements BLModule {
    String tokenName = "bundee_auth_token";
    String token;
    @Override
    public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
    }

    @BEndpoint(uri = UMSDefs.Endpoints.GET_API_VERSION, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = ApiVersion.class)
    public BaseResponse getAppVersion(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                      ApiVersion rolesRequestObject) throws BExceptions {
        token = reqCtx.getInHeaders().get(tokenName).get(0);
        UserList uList = new UserList();
        try {
            uList = ApiVersionDAO.getAppVersion(logger, blModServices.getDBManager(), rolesRequestObject.getVersionName(), uList.getApiVersions());
            if (uList.getApiVersions().size() == 0) {
                uList.setErrorCode("1");
                uList.setApiVersions(new ArrayList<ApiVersion>());
                uList.setErrorMessage("You need to update the application");
                return uList;

            } else {
                uList.setErrorCode("0");
                uList.setApiVersions(new ArrayList<ApiVersion>());
                uList.setErrorMessage("Your Application is upto Date");
                return uList;
            }

        } catch (Exception e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("error in Application Request");
            return uList;
        }

    }


}
