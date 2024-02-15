package com.bundee.msfw.servicefw.srvutils.utils;


import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.vault.VaultService;

public interface ServiceIniter {
	void init(BLogger logger, FileCfgHandler fch, VaultService vaultService, BLModServices blModServices) throws BExceptions;
}
