package com.bundee.ums.pojo;

public class APIKeyRoleDTO {
	Integer roleID;

	Boolean isActive;
	Integer createdBy;
	Integer updatedBy;
	
	public Integer getRoleID() {
		return roleID;
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
	
	public void setRoleID(Integer roleID) {
		this.roleID = roleID;
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
