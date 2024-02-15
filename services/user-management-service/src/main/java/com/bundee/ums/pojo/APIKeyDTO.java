package com.bundee.ums.pojo;

import com.bundee.msfw.defs.BaseResponse;

public class APIKeyDTO extends BaseResponse {
	Integer apiKeyID;
	String apiKeyName;
	Long validityTS;

	Boolean isActive;
	Integer createdBy;
	Integer updatedBy;
	
	public Integer getAPIKeyID() {
		return apiKeyID;
	}
	public String getAPIKeyName() {
		return apiKeyName;
	}
	public Long getValidityTS() {
		return validityTS;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public Integer getCreatedBy() {
		return createdBy;
	}
	public Integer getUpdatedBy() {
		return updatedBy;
	}
	
	public void setAPIKeyID(Integer apiKeyID) {
		this.apiKeyID = apiKeyID;
	}
	public void setAPIKeyName(String apiKeyName) {
		this.apiKeyName = apiKeyName;
	}
	public void setValidityTS(Long validityTS) {
		this.validityTS = validityTS;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}
	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}
}
