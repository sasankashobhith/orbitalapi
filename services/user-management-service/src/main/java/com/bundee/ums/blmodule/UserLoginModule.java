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
import com.bundee.ums.utils.TokenSerializer.AuthToken.*;

import java.util.*;

public class UserLoginModule implements BLModule, BLAuthzModule {
    private FBClient fbClient = null;

    @Override
    public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
        fbClient = new FBClient();
        fbClient.init(logger, blModServices);
    }

    @BEndpoint(uri = UMSDefs.Endpoints.LOGIN_USER, httpMethod = UniversalConstants.POST, permission = UniversalConstants.SPECIAL_NO_VALIDATE_PERMISSION, reqDTOClass = AuthTokenDTO.class)
    public BaseResponse userLogin(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                  AuthTokenDTO tokenObject) throws BExceptions {
        if (tokenObject == null || tokenObject.getAuthToken() == null || tokenObject.getAuthToken().isBlank()) {
            throw new BExceptions(UMSProcessingCode.INVALID_AUTH_TOKEN, "AuthToken is not valid");
        }
        AuthTokenDTO responseToken = new AuthTokenDTO();
        UTF8String userEmail = fbClient.getUserEmail(logger, tokenObject.getAuthToken());
        try {
            UserList userList = UserDAO.getDataByEmail(logger, blModServices.getDBManager(), userEmail.getUTF8String());
            if (userList != null && userList.getUserResponse() != null) {
                int userID = userList.getUserResponse().getIduser();
                String authToken = TokenSerializer.serializeToken(logger, blModServices,
                        TokenSerializer.AuthToken.TOKEN_TYPE.TT_USER, userID);
                responseToken.setAuthToken(authToken);
            } else {
                throw new BExceptions(UMSProcessingCode.INVALID_AUTH_TOKEN, "user not found!");
            }
        } catch (DBException e) {
            throw new BExceptions(e, UMSProcessingCode.INVALID_AUTH_TOKEN);
        }
        return responseToken;
    }

    @BEndpoint(uri = UMSDefs.Endpoints.VALIDATE_USER_AUTH_TOK, httpMethod = UniversalConstants.POST, permission = UniversalConstants.SPECIAL_NO_VALIDATE_PERMISSION, reqDTOClass = AuthTokenDTO.class)
    public BaseResponse validateUserAuthToken(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                              AuthTokenDTO authzDTO) throws BExceptions {
        return getTokenDetails(logger, blModServices, authzDTO.getAuthToken());
    }

    @BEndpoint(uri = UMSDefs.Endpoints.USER_PERMISSION_TEST, httpMethod = UniversalConstants.GET, permission = UMSDefs.Permissions.USER_N_HOST_API_KEY)
    public BaseResponse permissionTest(BLogger logger, BLModServices blModServices, RequestContext reqCtx)
            throws BExceptions {
        logger.debug("permissionTest: yes, came in");
        return null;
    }

    @Override
    public TokenDetails validateToken(BLogger logger, BLModServices blModServices, Map<String, List<String>> headers)
            throws BExceptions {
        String token = null;
        UserRoleDetailsDTO urd = null;
        if (headers != null && !headers.isEmpty()) {
            List<String> vals = headers.get(UniversalConstants.AUTH_TOKEN_HEADER);
            if (vals != null && !vals.isEmpty()) {
                token = vals.get(0);
                urd = getTokenDetails(logger, blModServices, token);
            } else {
                throw new BExceptions(UMSProcessingCode.INVALID_AUTH_TOKEN, "Token not found in headers");
            }
        } else {
            throw new BExceptions(UMSProcessingCode.INVALID_AUTH_TOKEN, "Token not found in headers");
        }
        return urd;
    }

    public void validatePermission(BLogger logger, BLModServices blModServices, TokenDetails tokenDetails,
                                   String permission) throws BExceptions {
        if (tokenDetails == null || permission == null || permission.isBlank()) {
            throw new BExceptions(UniversalConstants.PCodes.ACCESS_DENIED,
                    "Permissions denied for user: Unable to extract token details!");
        }
        UserRoleDetailsDTO urd = (UserRoleDetailsDTO) tokenDetails;
        Set<String> allPermissions = urd.getAllPermissions();
        if (!allPermissions.contains(permission)) {
            throw new BExceptions(UniversalConstants.PCodes.ACCESS_DENIED,
                    "Permissions denied for user: " + urd.getUserID());
        }
    }

    private UserRoleDetailsDTO getTokenDetails(BLogger logger, BLModServices blModServices, String token)
            throws BExceptions {
        if (token == null || token.isBlank()) {
            throw new BExceptions(UMSProcessingCode.INVALID_AUTH_TOKEN, "Token is empty!");
        }
        TokenSerializer.AuthToken at = TokenSerializer.deserializeToken(logger, blModServices, token);
        Set<Integer> roleIDs = new HashSet<Integer>();
        UserRoleDetailsDTO urd = new UserRoleDetailsDTO();
        if (at.getTokType() == TOKEN_TYPE.TT_USER) {
        	logger.debug("validating user token");
            UserRoleMap urm = UserRoleMapDAO.getUserRoleMapping(logger, blModServices.getDBManager(), at.getUserID());
            if (urm == null) {
                throw new BExceptions(UMSProcessingCode.INVALID_CONFIGURATION,
                        "Role not found for user: " + at.getUserID());
            }
            urd.setUserID(urm.getUserid());
            roleIDs.add(urm.getRoleid());
        } else {
        	logger.debug("validating API token");
            APIKeyDTO apikd = APIKeyDAO.getAPIKeyByID(logger, blModServices.getDBManager(), at.getUserID());
            if (apikd == null || apikd.getAPIKeyID() == null) {
                throw new BExceptions(UMSProcessingCode.INVALID_CONFIGURATION,
                        "API key not found: " + at.getUserID());
            }
            Set<APIKeyRoleDTO> roles = APIKeyDAO.listAllAPIKeyRoleMappingByID(logger, blModServices.getDBManager(), apikd.getAPIKeyID(), true);
            if (roles == null || roles.isEmpty()) {
                throw new BExceptions(UMSProcessingCode.INVALID_CONFIGURATION,
                        "Role not found for API key: " + at.getUserID());
            }
            urd.setUserID(apikd.getAPIKeyID());
            urd.setUserToken(false);
            roles.forEach(rdto -> {
                roleIDs.add(rdto.getRoleID());
            });
        }
        Map<Integer, Set<String>> rolePermMap = UserRoleMapDAO.getRolePermissionsMapping(logger,
                blModServices.getDBManager(), roleIDs);
        for (Map.Entry<Integer, Set<String>> rp : rolePermMap.entrySet()) {
            urd.setRolePermissions(rp.getKey(), rp.getValue());
        }
        Set<String> allPerms = urd.getAllPermissions();
        if (allPerms == null || allPerms.isEmpty()) {
            throw new BExceptions(UMSProcessingCode.INVALID_CONFIGURATION,
                    "Permissions not found for Role: " + roleIDs.toString());
        }
        return urd;
    }
}
