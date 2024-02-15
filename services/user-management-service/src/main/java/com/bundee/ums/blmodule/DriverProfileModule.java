package com.bundee.ums.blmodule;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.blmodi.*;
import com.bundee.msfw.interfaces.dbi.*;
import com.bundee.msfw.interfaces.endpoint.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.msfw.interfaces.reqrespi.*;
import com.bundee.ums.db.*;
import com.bundee.ums.defs.*;
import com.bundee.ums.pojo.*;
import com.bundee.ums.utils.*;

import java.util.*;

public class DriverProfileModule implements BLModule {
    @Override
    public void init(BLogger logger, BLModServices blModServices) {
    }

    @BEndpoint(uri = UMSDefs.Endpoints.GET_DRIVER_PROFILE, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = DriverProfile.class)
    public BaseResponse listByChannelID(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                        DriverProfile requestObject) {
        UserList vList = new UserList();
        try {
            vList = DriverProfileCrudDao.getDriverProfileById(logger, blModServices.getDBManager(), requestObject);
        } catch (Exception | BExceptions e) {
            UserList reviewResponse = new UserList();
            reviewResponse.setErrorCode("1");
            reviewResponse.setErrorMessage("Error in Review Request");
            return reviewResponse;
        }
        return vList;
    }

    @BEndpoint(uri = UMSDefs.Endpoints.CREATE_DRIVER_PROFILE, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = DriverProfile.class)
    public BaseResponse createChannel(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                      DriverProfile resObject) {
        UserList bookResponse = new UserList();
        try {
            if (resObject.getDrivingLicenseUrl().equalsIgnoreCase("na")) {
                UserList reviewResponse = new UserList();
                reviewResponse.setErrorCode("1");
                reviewResponse.setErrorMessage("Error in driver profile Request");
                return reviewResponse;
            }
            List<Image> imageList=new ArrayList<Image>();
            if(resObject.getInsuranceUrl().equalsIgnoreCase("na")){
                Image image= ImageUploadUtil.uploadImage(resObject.getDrivingLicenseUrl(), resObject.getUserId());
                imageList.add(image);
            }
            else {
                Image image= ImageUploadUtil.uploadImage(resObject.getDrivingLicenseUrl(), resObject.getUserId());
                Image image1= ImageUploadUtil.uploadImage(resObject.getInsuranceUrl(), resObject.getUserId());
                imageList.add(image);
                imageList.add(image1);
            }
            bookResponse = DriverProfileCrudDao.insertDriverProfile(logger, blModServices.getDBManager(), resObject,imageList);
        }  catch (Exception | BExceptions e) {
            UserList reviewResponse = new UserList();
            reviewResponse.setErrorCode("1");
            reviewResponse.setErrorMessage("Error in Review Request");
            return reviewResponse;
        }
        return bookResponse;
    }

    @BEndpoint(uri = UMSDefs.Endpoints.UPDATE_DRIVER_PROFILE, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY, reqDTOClass = DriverProfile.class)
    public BaseResponse updateChannel(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                      DriverProfile resObject) {
        UserList bookResponse = new UserList();
        try {
            List<Image> imageList=new ArrayList<Image>();
            if (!resObject.getDrivingLicenseUrl().equalsIgnoreCase("na")) {
                Image image= ImageUploadUtil.uploadImage(resObject.getInsuranceUrl(), resObject.getUserId());
                imageList.add(image);
            } else if (!resObject.getInsuranceUrl().equalsIgnoreCase("na")){
                Image image= ImageUploadUtil.uploadImage(resObject.getInsuranceUrl(), resObject.getUserId());
                imageList.add(image);
            }
            else {
                UserList reviewResponse = new UserList();
                reviewResponse.setErrorCode("1");
                reviewResponse.setErrorMessage("Error in driver profile Request");
                return reviewResponse;
            }
            bookResponse = DriverProfileCrudDao.updateDriverProfile(logger, blModServices.getDBManager(), resObject,imageList);
        }  catch (Exception | BExceptions e) {
            UserList reviewResponse = new UserList();
            reviewResponse.setErrorCode("1");
            reviewResponse.setErrorMessage("Error in Review Request");
            return reviewResponse;
        }
        return bookResponse;
    }
}
