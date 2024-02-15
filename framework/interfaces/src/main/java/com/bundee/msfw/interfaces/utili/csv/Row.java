package com.bundee.msfw.interfaces.utili.csv;

import java.util.Map;

import com.bundee.msfw.defs.UTF8String;

public interface Row {
	boolean isEmpty();
	int getRowID();
	int getMaxColumnID();
	Map<Integer, Column> getColumns();

	void addIntColumn(Integer pos, int value);
	void addLongColumn(Integer pos, long value);
	void addDoubleColumn(Integer pos, double value);
	void addStrColumn(Integer pos, UTF8String value);
}
