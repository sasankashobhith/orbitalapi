package com.bundee.ums.utils;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.restclienti.RESTClient;
import com.bundee.msfw.interfaces.restclienti.ResponseCapsule;
import com.bundee.ums.defs.UMSProcessingCode;

public class FBClient {
	private static final FBClient fbc = new FBClient();
	
	private static final String TOKEN_SERVICE_NM = "token.service";
	private static final String TOKEN_SERVICE_URL = "token.service.url";
	
	RESTClient fbRESTClient;
	String tokenServiceURL;
	
	public FBClient() {
	}
	
    public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
    	FileCfgHandler fch = blModServices.getFileCfgHandler();
    	tokenServiceURL = fch.getCfgParamStr(TOKEN_SERVICE_URL);
    	if(tokenServiceURL == null || tokenServiceURL.isBlank()) {
    		throw new BExceptions(UMSProcessingCode.INVALID_CONFIGURATION, "Configuration missing for " + TOKEN_SERVICE_URL);
    	}
    	tokenServiceURL += "verifyUserToken";
    	
    	fbRESTClient = blModServices.getRESTClientFactory().getNewRESTClient(logger, TOKEN_SERVICE_NM, TOKEN_SERVICE_NM, blModServices);
    }
    
    public UTF8String getUserEmail(BLogger logger, String fbToken) throws BExceptions {
    	UTF8String userEmail = null;
    	FBTokenDTO fbt = fbc.new FBTokenDTO(fbToken);
    	ResponseCapsule rc = fbRESTClient.sendReceiveJSONData(logger, "POST", tokenServiceURL, null, fbt, FBUserDTO.class, null);
    	FBUserDTO user = (FBUserDTO)rc.getResponseObject();
    	if(user == null) {
    		FBErrorDTO err = (FBErrorDTO)rc.getErrorObject(FBErrorDTO.class);
    		String errMsg = "Unknown Error";
    		if(err != null) {
    			errMsg = err.getMessage();
    		} 
    		throw new BExceptions(UMSProcessingCode.FB_ERROR, errMsg);
    	}
    	userEmail = new UTF8String(user.getEmail());
    	return userEmail;
    }
    
    private class FBTokenDTO {
    	String token;
    	
    	FBTokenDTO(String token) {
    		this.token = token;
    	}
    }

    private class FBUserDTO {
    	String email;
    	public String getEmail() {
    		return email;
    	}
    }

    private class FBErrorDTO {
    	Integer statusCode;
    	String message;
    	
    	public String getMessage() {
    		return statusCode + ":" + message;
    	}
    }
}
