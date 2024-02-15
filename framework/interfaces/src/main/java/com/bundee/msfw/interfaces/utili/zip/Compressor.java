package com.bundee.msfw.interfaces.utili.zip;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;

public interface Compressor {
	public byte[] compress(BLogger logger, byte[] data) throws BExceptions;
	public byte[] uncompress(BLogger logger, byte[] data) throws BExceptions;
}
