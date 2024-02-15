package com.bundee.ums.blmodule;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.blmodi.*;
import com.bundee.msfw.interfaces.endpoint.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.msfw.interfaces.reqrespi.*;
import com.bundee.ums.db.*;
import com.bundee.ums.defs.*;
import com.bundee.ums.pojo.*;
import com.bundee.ums.utils.*;
import com.bundee.ums.utils.TokenSerializer.AuthToken.*;

import java.util.*;

public class APIKeyModule implements BLModule {
    @Override
    public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
    }

    @BEndpoint(uri = UMSDefs.Endpoints.CREATE_NEW_API_KEY, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.CREATE_NEW_API_KEY, reqDTOClass = APIKeyDTO.class)
    public BaseResponse createNewAPIKey(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                        APIKeyDTO apiKeyDTO) throws BExceptions {
        if (apiKeyDTO == null) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKeyDTO is null!");
        }
        if (apiKeyDTO.getAPIKeyName() == null || apiKeyDTO.getAPIKeyName().isBlank()) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKey Name is null!");
        }
        if (apiKeyDTO.getValidityTS() == null) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKey validaty timestamp is null!");
        }
        APIKeyDAO.createNewAPIKey(logger, blModServices.getDBManager(), apiKeyDTO, reqCtx.getTokenDetails());
        return apiKeyDTO;
    }

    @BEndpoint(uri = UMSDefs.Endpoints.LIST_ALL_API_KEY, httpMethod = UniversalConstants.GET, permission = UMSDefs.Permissions.LIST_ALL_API_KEY)
    public BaseResponse listAllAPIKeyDetails(BLogger logger, BLModServices blModServices, RequestContext reqCtx) throws BExceptions {
        APIKeyToRoleMappingDTOList apiKeyDTOList = new APIKeyToRoleMappingDTOList();
        List<APIKeyDTO> apiKeys = APIKeyDAO.listAllAPIKeys(logger, blModServices.getDBManager());
        if (apiKeys != null && !apiKeys.isEmpty()) {
            for (APIKeyDTO apik : apiKeys) {
                Set<APIKeyRoleDTO> apikeyRoleMapping = APIKeyDAO.listAllAPIKeyRoleMappingByID(logger, blModServices.getDBManager(), apik.getAPIKeyID(), false);
                APIKeyToRoleMappingDTO apiKeyrmd = new APIKeyToRoleMappingDTO();
                apiKeyrmd.setAPIKeyID(apik.getAPIKeyID());
                apiKeyrmd.setAPIKeyName(apik.getAPIKeyName());
                apiKeyrmd.setRoleMapping(apikeyRoleMapping);
                apiKeyDTOList.apikeys.add(apiKeyrmd);
            }
        }
        return apiKeyDTOList;
    }

    @BEndpoint(uri = UMSDefs.Endpoints.ENABLE_API_KEY, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.ENABLE_API_KEY, reqDTOClass = APIKeyDTO.class)
    public BaseResponse enableAPIKey(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                     APIKeyDTO apiKeyDTO) throws BExceptions {
        if (apiKeyDTO == null) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKeyDTO is null!");
        }
        if (apiKeyDTO.getAPIKeyID() == null) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKey ID is null!");
        }
        APIKeyDAO.enableAPIKey(logger, blModServices.getDBManager(), apiKeyDTO.getAPIKeyID(), reqCtx.getTokenDetails());
        return null;
    }

    @BEndpoint(uri = UMSDefs.Endpoints.DISABLE_API_KEY, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.DISABLE_API_KEY, reqDTOClass = APIKeyDTO.class)
    public BaseResponse disableAPIKey(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                      APIKeyDTO apiKeyDTO) throws BExceptions {
        if (apiKeyDTO == null) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKeyDTO is null!");
        }
        if (apiKeyDTO.getAPIKeyID() == null) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKey ID is null!");
        }
        APIKeyDAO.disableAPIKey(logger, blModServices.getDBManager(), apiKeyDTO.getAPIKeyID(), reqCtx.getTokenDetails());
        return null;
    }

    @BEndpoint(uri = UMSDefs.Endpoints.ASSIGN_ROLES_TO_API_KEY, httpMethod = UniversalConstants.POST, permission = UMSDefs.Permissions.ASSIGN_ROLES_TO_API_KEY, reqDTOClass = APIKeyToRoleMappingDTO.class)
    public BaseResponse assignRolesAPIKey(BLogger logger, BLModServices blModServices, RequestContext reqCtx,
                                          APIKeyToRoleMappingDTO apiKeyRoleMappingDTO) throws BExceptions {
        if (apiKeyRoleMappingDTO == null) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKeyToRoleMappingDTO is null!");
        }
        if (apiKeyRoleMappingDTO.getAPIKeyID() == null) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKeyToRoleMappingDTO ID is null!");
        }
        if (apiKeyRoleMappingDTO.getRoleMapping() == null || apiKeyRoleMappingDTO.getRoleMapping().isEmpty()) {
            throw new BExceptions(UMSProcessingCode.INVALID_INPUT, "APIKeyToRoleMappingDTO roleMapping is null!");
        }
        APIKeyDAO.assignAPIKeyRoleMapping(logger, blModServices.getDBManager(), apiKeyRoleMappingDTO.getAPIKeyID(),
                apiKeyRoleMappingDTO.getRoleMapping(), reqCtx.getTokenDetails());
        return null;
    }

    @BEndpoint(uri = UMSDefs.Endpoints.GEN_API_KEY_TOKEN, httpMethod = UniversalConstants.GET, permission = UMSDefs.Permissions.GEN_API_KEY_TOKEN, reqDTOClass = APIKeyDTO.class)
    public BaseResponse genTokenForAPIKey(BLogger logger, BLModServices blModServices, RequestContext reqCtx, APIKeyDTO apiKey) throws BExceptions {
        APIKeyTokenDTO apiKeyToken = new APIKeyTokenDTO();
        APIKeyDTO apiKeyDTO = APIKeyDAO.getAPIKeyByID(logger, blModServices.getDBManager(), apiKey.getAPIKeyID());
        apiKeyToken.apiKeyID = apiKeyDTO.getAPIKeyID();
        apiKeyToken.token = TokenSerializer.serializeToken(logger, blModServices, TOKEN_TYPE.TT_API, apiKeyToken.apiKeyID);
        return apiKeyToken;
    }

    class APIKeyToRoleMappingDTOList extends BaseResponse {
        public List<APIKeyToRoleMappingDTO> apikeys = new ArrayList<APIKeyToRoleMappingDTO>();
    }

    class APIKeyTokenDTO extends BaseResponse {
        public Integer apiKeyID;
        public String token;
    }
}
