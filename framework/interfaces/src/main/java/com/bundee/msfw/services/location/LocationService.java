package com.bundee.msfw.services.location;

import java.util.Collection;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface LocationService {
	Collection<LocationDetails> getLocationsByCityState(BLogger logger, UTF8String city, UTF8String state) throws BExceptions;
}
