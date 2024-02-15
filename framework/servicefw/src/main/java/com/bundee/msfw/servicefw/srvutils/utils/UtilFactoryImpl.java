package com.bundee.msfw.servicefw.srvutils.utils;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;

import org.owasp.esapi.ESAPI;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.UtilFactory;
import com.bundee.msfw.interfaces.utili.concurrent.ConcurrentTaskExecutor;
import com.bundee.msfw.interfaces.utili.crypto.CryptoService;
import com.bundee.msfw.interfaces.utili.csv.BCSVReader;
import com.bundee.msfw.interfaces.utili.csv.BCSVWriter;
import com.bundee.msfw.interfaces.utili.html.HTMLBuilder;
import com.bundee.msfw.interfaces.utili.zip.Compressor;
import com.bundee.msfw.servicefw.srvutils.utils.concurrent.ConcurrentTaskExecutorImpl;
import com.bundee.msfw.servicefw.srvutils.zip.CompressorImpl;

public class UtilFactoryImpl implements UtilFactory {

	private CryptoServiceImpl cryptoServiceImpl = new CryptoServiceImpl();
	private BJson jsonProc = new BJson();
	private static ConcurrentTaskExecutorImpl concurrentTaskExecutorImpl = new ConcurrentTaskExecutorImpl(5);

	public UtilFactoryImpl() {
	}

	@Override
	public BCSVReader getNewCSVReader(Reader reader, int numHeaderRows) throws BExceptions {
		return new BCSVReaderImpl(reader, numHeaderRows);
	}

	@Override
	public CryptoService getNewCryptoService() {
		return cryptoServiceImpl;
	}

	@Override
	public BCSVReader getNewCSVReader(Reader reader, char seperator, int numHeaderRows) throws BExceptions {
		return new BCSVReaderImpl(reader, seperator, numHeaderRows, false);
	}

	@Override
	public BCSVReader getNewLargeCSVReader(Reader reader, char seperator, int numHeaderRows) throws BExceptions {
		return new BCSVReaderImpl(reader, seperator, numHeaderRows, true);
	}
	
	@Override
	public Compressor getNewCompressor() {
		return new CompressorImpl();
	}

	@Override
	public BCSVWriter getNewCSVWriter() throws BExceptions {
		return new BCSVWriterImpl();
	}

	@Override
	public Object getObjectFromJSON(BLogger logger, String jsonString, Class<?> dtoClass) throws BExceptions {
		return jsonProc.fromJson(jsonString, dtoClass);
	}

	@Override
	public Collection<Map<String, String>> getObjectFromJSONAsCollMap(BLogger logger, String jsonString) throws BExceptions {
		return jsonProc.fromJSONAsCollMap(logger, jsonString);
	}
	
	@Override
	public Map<String, String> getObjectFromJSONAsMap(BLogger logger, String jsonString) throws BExceptions {
		return jsonProc.fromJSONAsMap(logger, jsonString);
	}
	
	@Override
	public String getJSONFromObject(BLogger logger, Object javaPOJO) throws BExceptions {
		UTF8String str = jsonProc.toJson(javaPOJO);
		if(str != null && !str.isBlank()) {
			return str.getUTF8String();
		}
		return null;
	}

	@Override
	public String encodeForHTML(BLogger logger, String inString) throws BExceptions {
		String encString = null; 
		if(inString != null && !inString.isBlank()) {
			encString = ESAPI.encoder().encodeForHTML(inString);
		}
		return encString;
	}

	@Override
	public HTMLBuilder getHTMLBuilder(String templateFileName) throws BExceptions {
		return new HTMLBuilderImpl(templateFileName);
	}

	@Override
	public ConcurrentTaskExecutor getConcurrentTaskExecutor() {
		return concurrentTaskExecutorImpl;
	}
}
