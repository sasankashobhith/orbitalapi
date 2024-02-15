package com.bundee.testsvc.pojo;

import java.util.List;

import com.bundee.msfw.defs.BaseResponse;

public class DBTaskDTO extends BaseResponse {
	Integer sleepms;
	Integer sel_loop;
	Integer upd_loop;
	List<String> selectqs;
	List<String> updateqs;

	String id;
	
	public Integer getSleepMS() {
		return sleepms;
	}
	public List<String> getSelectQs() {
		return selectqs;
	}
	public List<String> getUpdateQs() {
		return updateqs;
	}
	public String getID() {
		return id;
	}
	public int getSelLoop() {
		return sel_loop;
	}
	public int getUpdLoop() {
		return upd_loop;
	}
	
	public void initValues(int idx) {
		if(sleepms == null || sleepms <= 10)
			sleepms = 10;
		if(sel_loop == null || sel_loop <= 0)
			sel_loop = 1;
		if(upd_loop == null || upd_loop <= 0)
			upd_loop = 1;
		
		id = "DBTASK-" + idx;
	}
	
	public static DBTaskDTO copy(DBTaskDTO dbt) {
		DBTaskDTO dbtn = new DBTaskDTO();

		dbtn.sleepms = dbt.sleepms;
		dbtn.sel_loop = dbt.sel_loop;
		dbtn.upd_loop = dbt.upd_loop;
		dbtn.selectqs = dbt.selectqs;
		dbtn.updateqs = dbt.updateqs;
		
		return dbtn;
	}
}
