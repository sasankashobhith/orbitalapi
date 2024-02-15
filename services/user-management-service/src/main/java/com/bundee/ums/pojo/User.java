package com.bundee.ums.pojo;
import com.bundee.msfw.defs.BaseResponse;
import com.bundee.msfw.defs.UTF8String;

public class User {
	
	private int iduser;
	private UTF8String email;
	private UTF8String firstname;
	private UTF8String lastname;
	private int count ;
	

	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getId() {
		return iduser;
	}
	public void setId(int iduser) {
		this.iduser = iduser;
	}


	public UTF8String getEmail() {
		return email;
	}


	public void setEmailid(UTF8String emailID) {
		this.email = emailID;
	}


	public UTF8String getfirstname() {
		return firstname;
	}


	public void setfirstname(UTF8String first) {
		firstname = first;
	}


	public UTF8String getlastname() {
		return lastname;
	}


	public void setlastname(UTF8String lname) {
		this.lastname = lname;
	}



}
