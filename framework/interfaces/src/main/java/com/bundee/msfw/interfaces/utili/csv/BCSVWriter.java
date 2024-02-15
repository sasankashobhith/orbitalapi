package com.bundee.msfw.interfaces.utili.csv;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface BCSVWriter {
	Row addNewHeaderRow();
	Row addNewDataRow();
	byte[] writeCSV(BLogger logger) throws BExceptions;
}
