package com.bundee.ums.pojo;

public class UserRoleMap {
  
	private int userroleid;
	private int userid;
	private int roleid;
	private int createdby;
	private int updatedby;
	private String createddate;

	private String updatedate;
	private Boolean isactive;
	
	
	public int getId() {
		return userroleid;
	}
	public void setId(int id) {
		this.userroleid = id;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public int getRoleid() {
		return roleid;
	}
	public void setRoleid(int roleid) {
		this.roleid = roleid;
	}
	public int getCreatedby() {
		return createdby;
	}
	public void setCreatedby(int createdby) {
		this.createdby = createdby;
	}
	public int getUpdatedby() {
		return updatedby;
	}
	public void setUpdatedby(int updatedby) {
		this.updatedby = updatedby;
	}
	public String getCreateddate() {
		return createddate;
	}
	public void setCreateddate(String createddate) {
		this.createddate = createddate;
	}
	public String getUpdateddate() {
		return updatedate;
	}
	public void setUpdateddate(String updateddate) {
		this.updatedate  = updateddate;
	}
	public Boolean getIsactive() {
		return isactive;
	}
	public void setIsactive(Boolean isactive) {
		this.isactive = isactive;
	}
	
	
	
}
