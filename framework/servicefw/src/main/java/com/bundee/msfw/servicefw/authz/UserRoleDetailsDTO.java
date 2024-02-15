package com.bundee.msfw.servicefw.authz;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bundee.msfw.interfaces.blmodi.TokenDetails;

class UserRoleDetailsDTO implements TokenDetails {
	private static final Set<String> emptySet = new HashSet<String>();
	
	Integer userID = null;
	Boolean bUserToken = false;
	
	Map<Integer, Set<String>> rolePermissions;

	public boolean isValid() {
		return (userID != null && rolePermissions != null);
	}
	public Integer getUserID() {
		return userID;
	}

	public Integer getRoleID() {
		if(rolePermissions != null) {
			for(Map.Entry<Integer, Set<String>> pair : rolePermissions.entrySet()) {
				return pair.getKey();
			}
		}
		return -1;
	}

	public Set<String> getPermissions() {
		if(rolePermissions != null) {
			for(Map.Entry<Integer, Set<String>> pair : rolePermissions.entrySet()) {
				return pair.getValue();
			}
		}
		
		return emptySet; 
	}

	@Override
	public Boolean isUserToken() {
		return bUserToken;
	}
}
