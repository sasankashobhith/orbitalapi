package com.bundee.msfw.servicefw.srvutils.utils;

import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.utili.csv.Column;

public class ColumnImpl implements Column {
	UTF8String val;
	boolean bEmpty = true;
	
	public ColumnImpl(String val) {
		if(val == null || val.isEmpty()) {
			this.val = new UTF8String("");
		} else {
			this.val = new UTF8String(val.stripLeading().stripTrailing());
			bEmpty = false;
		}
	}
	
	@Override
	public int getIntValue() {
		return Integer.parseInt(val.getUTF8String());
	}

	@Override
	public UTF8String getStrValue() {
		return val;
	}

	@Override
	public double getDoubleValue() {
		return Double.parseDouble(val.getUTF8String());
	}

	@Override
	public boolean isEmpty() {
		return bEmpty;
	}
}
