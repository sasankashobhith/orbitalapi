package com.bundee.msfw.interfaces.vault;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.HealthCheck;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface VaultService extends HealthCheck {

    /**
     * @param logger logger with context to have functional logging
     * @param key    String name of key in ACCMVault
     * @return String Secret value
     * @throws BExceptions in case ACCM-VAULT is not contactable
     */
    String getValue(BLogger logger, String key) throws BExceptions;
}
