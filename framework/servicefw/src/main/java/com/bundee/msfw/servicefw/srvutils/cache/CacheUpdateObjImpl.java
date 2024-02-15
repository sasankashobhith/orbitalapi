package com.bundee.msfw.servicefw.srvutils.cache;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.utili.dc.UpdateCacheObj;

public class CacheUpdateObjImpl implements UpdateCacheObj {
	private String businessModID;
	private int ucId;
	private int targetTenantId;
	private UTF8String ucData;
	private long creationTS;
	private STATUS status;
	private long pickupTimeTS;
	private long completionTimeTS;
	
	public CacheUpdateObjImpl(String businessModID, int targetTenantId, UTF8String ucData) {
		this.businessModID = businessModID;
		this.targetTenantId = targetTenantId;
		this.ucData = (ucData == null ? new UTF8String("") : ucData);
		
		creationTS = System.currentTimeMillis()/1000;		
		status = STATUS.INITIAL;
	}

	public CacheUpdateObjImpl(ResultSet rs) throws SQLException {
		ucId = rs.getInt(dbf_uc_id);
		targetTenantId = rs.getInt(dbf_targettenant_id);

		businessModID = rs.getString(dbf_uc_modid);
		ucData = new UTF8String(rs.getString(dbf_uc_data));
		creationTS = rs.getLong(dbf_create_ts);		
		pickupTimeTS = rs.getLong(dbf_pickup_ts);		
		completionTimeTS = rs.getLong(dbf_complete_ts);		
		status = STATUS.get(rs.getInt(dbf_uc_status));
	}

	@Override
	public STATUS getStatus() {
		return status;
	}

	@Override
	public String getBusinessModID() {
		return businessModID;
	}

	@Override
	public int getUCId() {
		return ucId;
	}

	@Override
	public int getTargetTenantId() {
		return targetTenantId;
	}

	@Override
	public UTF8String getUCData() {
		return ucData;
	}

	@Override
	public long getCreationTS() {
		return creationTS;
	}

	@Override
	public long getPickupTS() {
		return pickupTimeTS;
	}

	@Override
	public long getCompletionTS() {
		return completionTimeTS;
	}

	//All DB fields needed from CacheUpdate table
	private static final String dbf_uc_id = "uc_id";
	private static final String dbf_uc_modid = "uc_modid";
	private static final String dbf_targettenant_id = "targettenant_id";
	private static final String dbf_uc_data = "uc_data";
	private static final String dbf_uc_status = "status";
	private static final String dbf_create_ts = "create_ts";
	private static final String dbf_pickup_ts = "pickup_ts";
	private static final String dbf_complete_ts = "complete_ts";
}
