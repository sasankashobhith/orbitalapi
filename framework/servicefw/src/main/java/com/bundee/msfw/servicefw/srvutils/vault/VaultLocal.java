package com.bundee.msfw.servicefw.srvutils.vault;



import java.util.HashMap;
import java.util.Map;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.defs.ProcessingCode;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.vault.VaultService;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;
import com.bundee.msfw.servicefw.srvutils.utils.ServiceIniter;

public class VaultLocal implements VaultService, ServiceIniter {
	private static final String VAULT_KEY_PFX = "vault.local.";
	private Map<String, String> keyValues;
	
    public String getValue(BLogger logger, String key) throws BExceptions {
    	String value = keyValues.get(key);
    	if(value == null) {
    		throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Key " + key + " not found in the vault!");
    	}
    	
    	return value;
    }

	@Override
	public void init(BLogger logger, FileCfgHandler fch, VaultService vaultService, BLModServices blModServices) throws BExceptions {
		keyValues = new HashMap<String, String>();
		Map<String, Object> kvs = fch.getAllCfgParams();
		for(Map.Entry<String, Object> pair : kvs.entrySet()) {
			if(pair.getKey().startsWith(VAULT_KEY_PFX)) {
				String vk = pair.getKey().substring(VAULT_KEY_PFX.length());
				keyValues.put(vk, (String)pair.getValue());
			}
		}
		logger.info("Loaded " + keyValues.size() + " key-value pairs in to Vault!");
	}

	@Override
	public HealthDetails checkHealth(BLogger logger) {
		HealthDetails hd = new HealthDetails();
		hd.add(ProcessingCode.SUCCESS_PC, ProcessingCode.SUCCESS_KEY, "localhost");
		return hd;
	}
}
