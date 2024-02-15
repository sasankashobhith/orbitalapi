package com.bundee.msfw.interfaces.utili.html;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface HTMLBuilder {
	DynamicRowSet addDynamicRowSet(String tag) throws BExceptions;
	void setValue(String name, String value);
	String build(BLogger logger) throws BExceptions;
}
