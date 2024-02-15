package com.bundee.msfw.interfaces.utili.csv;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface BCSVReader {
	int getNumRows();
	Row getRow(int idx) throws BExceptions;
	
	Row getNextRow(BLogger logger) throws BExceptions;
}
