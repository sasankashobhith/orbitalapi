package com.bundee.ums.utils;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.ums.pojo.OwnerEmployeeMappingResponse;

public class OwnerUtill {

	public static OwnerEmployeeMappingResponse createsingleowner(OwnerEmployeeMappingResponse row)
			throws BExceptions {
		OwnerEmployeeMappingResponse res = new OwnerEmployeeMappingResponse();

		res.setId(row.getId());
		res.setUserid(row.getUserid());
		res.setRoleid(row.getRoleid());

		res.setCreatedby(row.getCreatedby());
		res.setUpdatedby(row.getUpdatedby());

		res.setIsactive(row.getisIsactive());

		return res;

	}

}
