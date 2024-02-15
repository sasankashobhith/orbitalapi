package com.bundee.ums.pojo;

public class MasterRolesRequest {

	private int roleid;
	
	private String rolename;
	private String createddate;
	private String updatedate;
	private Boolean isactive;
	
	private int userroleid;
    public int getUserroleid() {
		return userroleid;
	}
	public void setUserroleid(int userroleid) {
		this.userroleid = userroleid;
	}



	private int userid;
	
	private int createdby;
	private int updatedby;
	
	
	
	public String getCreateddate() {
		return createddate;
	}
	public void setCreateddate(String createddate) {
		this.createddate = createddate;
	}
	public String getUpdatedate() {
		return updatedate;
	}
	public void setUpdatedate(String updatedate) {
		this.updatedate = updatedate;
	}
	
	
	
	private Boolean userisactive;
	
	
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
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
	
	public Boolean getUserisactive() {
		return userisactive;
	}
	public void setUserisactive(Boolean userisactive) {
		this.userisactive = userisactive;
	}
	
	
	
	
	
	
	public int getRoleid() {
		return roleid;
	}
	public void setRoleid(int roleid) {
		this.roleid = roleid;
	}
	public String getRolename() {
		return rolename;
	}
	public void setRolename(String rolename) {
		this.rolename = rolename;
	}
	
	public Boolean getIsactive() {
		return isactive;
	}
	public void setIsactive(Boolean isactive) {
		this.isactive = isactive;
	}
	
	
	
}
