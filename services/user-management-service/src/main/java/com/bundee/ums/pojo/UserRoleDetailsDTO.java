package com.bundee.ums.pojo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.interfaces.blmodi.TokenDetails;

public class UserRoleDetailsDTO extends BaseResponse implements TokenDetails{
	private static final Set<String> emptySet = new HashSet<String>();
	
	Integer userID;
	Boolean bUserToken = false;
	
	Map<Integer, Set<String>> rolePermissions = new HashMap<Integer, Set<String>>();

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

	public Boolean isUserToken() {
		return bUserToken;
	}

	public Set<String> getAllPermissions() {
		Set<String> allPermissions = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		for(Map.Entry<Integer, Set<String>> pair : rolePermissions.entrySet()) {
			allPermissions.addAll(pair.getValue());
		}
		
		return allPermissions;
	}
	
	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	public void setUserToken(Boolean bUserToken) {
		this.bUserToken = bUserToken;
	}

	public void setRolePermissions(Integer roleID, Set<String> permissions) {
		this.rolePermissions.put(roleID, permissions);
	}
}
