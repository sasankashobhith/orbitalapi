package com.bundee.msfw.interfaces.utili.csv;

import com.bundee.msfw.defs.*;

public interface Column {
	boolean isEmpty();
	int getIntValue();
	double getDoubleValue();
	UTF8String getStrValue();
}
