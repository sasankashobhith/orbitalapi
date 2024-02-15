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

public class HostProfileModule implements BLModule {
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

    @BEndpoint(uri = UMSDefs.Endpoints.INSERT_HOST_PROFILE, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = HostProfile.class)
    public BaseResponse createHostProfile(BLogger logger, BLModServices blModServices, RequestContext reqCtx, HostProfile loginObject) throws BExceptions {
        UserList uList = new UserList();
        token = reqCtx.getInHeaders().get(tokenName).get(0);
        try {
            uList = HostProfileDAO.insertSingleUser(logger, blModServices.getDBManager(), loginObject);
            uList.setErrorCode("0");
            uList.setErrorMessage("Data Inserted");
            return uList;
        } catch (DBException e) {
            uList.setErrorCode("1");
            uList.setErrorMessage("No database connection " + e.getMessage());
            return uList;
        }

    }



}
