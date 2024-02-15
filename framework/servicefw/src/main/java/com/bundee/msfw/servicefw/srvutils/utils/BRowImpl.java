package com.bundee.msfw.servicefw.srvutils.utils;

import java.util.HashMap;
import java.util.Map;

import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.utili.csv.Column;
import com.bundee.msfw.interfaces.utili.csv.Row;

public class BRowImpl implements Row {
	int rowID;
	int maxColumnID;
	Map<Integer, Column> columns;
	boolean bEmpty;
	
	public BRowImpl(int rowID) {
		this.rowID = rowID;
		columns = new HashMap<Integer, Column>();
		maxColumnID = 0;
		bEmpty = true;
	}
	
	@Override
	public int getRowID() {
		return rowID;
	}

	@Override
	public Map<Integer, Column> getColumns() {
		return columns;
	}

	public void addColumnValue(int columnID, String val) {
		ColumnImpl column = new ColumnImpl(val);
		columns.put(columnID, column);
		bEmpty &= column.isEmpty();
		
		if(maxColumnID < columnID) {
			maxColumnID = columnID;
		}
	}

	@Override
	public int getMaxColumnID() {
		return maxColumnID;
	}
	
	@Override
	public boolean isEmpty() {
		return bEmpty;
	}

	@Override
	public void addIntColumn(Integer pos, int value) {
		addColumnValue(pos, Integer.toString(value));
	}

	@Override
	public void addDoubleColumn(Integer pos, double value) {
		addColumnValue(pos, Double.toString(value));
	}

	@Override
	public void addStrColumn(Integer pos, UTF8String value) {
		addColumnValue(pos, value.getUTF8String());
	}

	@Override
	public void addLongColumn(Integer pos, long value) {
		addColumnValue(pos, Long.toString(value));
	}
}
