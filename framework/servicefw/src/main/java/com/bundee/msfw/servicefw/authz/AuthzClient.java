package com.bundee.msfw.servicefw.authz;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UniversalConstants;
import com.bundee.msfw.interfaces.blmodi.BLAuthzModule;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.blmodi.TokenDetails;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.restclienti.RESTClient;
import com.bundee.msfw.interfaces.restclienti.ResponseCapsule;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;

public class AuthzClient implements BLAuthzModule {
	private static final String UM_AUTHZ_SERVICE_ENABLED = "um.authz.service.enabled";
	private static final String UM_AUTHZ_SERVICE_URL = "um.authz.service.url";
	private static final String UM_AUTHZ_SERVICE = "um.authz.service";

	private boolean bEnabled = false;
	private String authzURL;

	public AuthzClient() {
	}

	public boolean init(BLogger logger, BLModServices blModServices, BExceptions bex) {
		try {
			bEnabled = Boolean.parseBoolean(blModServices.getFileCfgHandler().getCfgParamStr(UM_AUTHZ_SERVICE_ENABLED));
		} catch (BExceptions e) {
			bEnabled = false;
		}

		if (bEnabled) {
			try {
				authzURL = blModServices.getFileCfgHandler().getCfgParamStr(UM_AUTHZ_SERVICE_URL);
				if (authzURL == null || authzURL.isBlank()) {
					bex.add(new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING,
							UM_AUTHZ_SERVICE_URL + " not configured!"));
				}
				authzURL += "api/v1/user/token/validate";

				RESTClient restClient = blModServices.getRESTClientFactory().getNewRESTClient(logger, UM_AUTHZ_SERVICE, UM_AUTHZ_SERVICE,
						blModServices);
				restClient.setStandardHealthCheck(authzURL);
			} catch (BExceptions e) {
				bex.add(e);
			}
		}

		return bEnabled;
	}

	@Override
	public TokenDetails validateToken(BLogger logger, BLModServices blModServices, Map<String, List<String>> headers)
			throws BExceptions {
		RESTClient authzClient = blModServices.getRESTClientFactory().getNewRESTClient(logger, UM_AUTHZ_SERVICE,
				UM_AUTHZ_SERVICE, blModServices);

		List<String> vals = headers.get(UniversalConstants.AUTH_TOKEN_HEADER);
		if (vals == null || vals.isEmpty() || vals.get(0) == null || vals.get(0).isBlank()) {
			throw new BExceptions(UniversalConstants.PCodes.ACCESS_DENIED, "Token is missing!");
		}
		String token = vals.get(0);
		AuthzDTO ai = new AuthzDTO();
		ai.setAuthToken(token);

		ResponseCapsule rc = authzClient.sendReceiveJSONData(logger, UniversalConstants.POST, authzURL, null, ai,
				UserRoleDetailsDTO.class, null);
		UserRoleDetailsDTO urd = (UserRoleDetailsDTO) rc.getResponseObject();
		if (urd == null || !urd.isValid()) {
			BExceptions err = (BExceptions) rc.getErrorObject(BExceptions.class);
			String errMsg = "Error in authz service " + authzURL;
			if (err != null) {
				errMsg = err.getMessage();
			}
			throw new BExceptions(UniversalConstants.PCodes.INTERNAL_ERROR, errMsg);
		}
		return urd;
	}

	@Override
	public void validatePermission(BLogger logger, BLModServices blModServices, TokenDetails tokenDetails,
			String permission) throws BExceptions {
		if (tokenDetails == null || permission == null || permission.isBlank()) {
			throw new BExceptions(UniversalConstants.PCodes.ACCESS_DENIED,
					"Permissions denied for user: Unable to extract token details!");
		}
		UserRoleDetailsDTO urd = (UserRoleDetailsDTO) tokenDetails;
		Set<String> perms = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		urd.getPermissions().forEach(p -> perms.add(p));
		if (!perms.contains(permission)) {
			throw new BExceptions(UniversalConstants.PCodes.ACCESS_DENIED, "user: " + urd.getUserID() + " role: "
					+ urd.getRoleID() + " does not have permission " + permission);
		}
	}
}
