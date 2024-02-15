package com.bundee.ums.pojo;

public class MobileConfResponse {
	
	private int id;
	private String inputstr;
	private boolean isactive; 
	private String createddate; 
	private String updateddate;
	private int hostid; 
	private int channelid;
	
	
	
	public int getHostid() {
		return hostid;
	}
	public void setHostid(int hostid) {
		this.hostid = hostid;
	}
	public int getChannelid() {
		return channelid;
	}
	public void setChannelid(int channelid) {
		this.channelid = channelid;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getInputstr() {
		return inputstr;
	}
	public void setInputstr(String inputstr) {
		this.inputstr = inputstr;
	}
	public boolean getisIsactive() {
		return isactive;
	}
	public void setIsactive(boolean isactive) {
		this.isactive = isactive;
	}
	public String getCreateddate() {
		return createddate;
	}
	public void setCreateddate(String createddate) {
		this.createddate = createddate;
	}
	public String getUpdateddate() {
		return updateddate;
	}
	public void setUpdateddate(String updateddate) {
		this.updateddate = updateddate;
	}

}
