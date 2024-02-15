package com.bundee.ums.pojo;

import java.util.Set;

public class APIKeyToRoleMappingDTO {
	public Integer getAPIKeyID() {
		return apiKeyID;
	}
	public Set<APIKeyRoleDTO> getRoleMapping() {
		return roleMapping;
	}
	
	public void setAPIKeyID(Integer apiKeyID) {
		this.apiKeyID = apiKeyID;
	}
	public void setAPIKeyName(String apiKeyName) {
		this.apiKeyName = apiKeyName;
	}
	public void setRoleMapping(Set<APIKeyRoleDTO> roleMapping) {
		this.roleMapping = roleMapping;
	}

	Integer apiKeyID;
	String apiKeyName;
	Set<APIKeyRoleDTO> roleMapping;
}
