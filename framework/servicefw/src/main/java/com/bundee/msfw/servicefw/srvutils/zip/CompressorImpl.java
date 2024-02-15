package com.bundee.msfw.servicefw.srvutils.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.zip.Compressor;

public class CompressorImpl implements Compressor {

	@Override
	public byte[] compress(BLogger logger, byte[] data) throws BExceptions {
		Deflater deflater = new Deflater();
		deflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

		deflater.finish();
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer); // returns the generated code... index
			outputStream.write(buffer, 0, count);
		}
		try {
			outputStream.close();
		} catch (IOException e) {
			logger.error(e);
		}
		byte[] output = outputStream.toByteArray();
		return output;
	}

	@Override
	public byte[] uncompress(BLogger logger, byte[] data) throws BExceptions {
		Inflater inflater = new Inflater();
		inflater.setInput(data);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[1024];
		try {
			while (!inflater.finished()) {
				int count;
				count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
		} catch (DataFormatException e) {
			logger.error(e);
		}
		try {
			outputStream.close();
		} catch (IOException e) {
			logger.error(e);
		}
		byte[] output = outputStream.toByteArray();
		return output;
	}
}
