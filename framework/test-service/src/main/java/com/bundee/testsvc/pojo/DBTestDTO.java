package com.bundee.testsvc.pojo;

import java.util.List;

import com.bundee.msfw.defs.BaseResponse;

public class DBTestDTO extends BaseResponse {
	List<DBTaskDTO> tasks;
	Integer replication;
	
	public List<DBTaskDTO> getTasks() {
		return tasks;
	}
	public Integer getReplication() {
		return replication;
	}
}
