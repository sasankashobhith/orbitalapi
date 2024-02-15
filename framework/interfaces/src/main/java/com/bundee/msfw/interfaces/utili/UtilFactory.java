package com.bundee.msfw.interfaces.utili;

import java.io.Reader;
import java.util.Collection;
import java.util.Map;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.concurrent.ConcurrentTaskExecutor;
import com.bundee.msfw.interfaces.utili.crypto.CryptoService;
import com.bundee.msfw.interfaces.utili.csv.BCSVReader;
import com.bundee.msfw.interfaces.utili.csv.BCSVWriter;
import com.bundee.msfw.interfaces.utili.html.HTMLBuilder;
import com.bundee.msfw.interfaces.utili.zip.Compressor;

public interface UtilFactory {
	BCSVReader getNewCSVReader(Reader reader, int numHeaderRows) throws BExceptions;
	BCSVReader getNewCSVReader(Reader reader, char seperator, int numHeaderRows) throws BExceptions;
	BCSVReader getNewLargeCSVReader(Reader reader, char seperator, int numHeaderRows) throws BExceptions;
	
	BCSVWriter getNewCSVWriter() throws BExceptions;
	
	CryptoService getNewCryptoService();
	Compressor getNewCompressor();
	
	Object getObjectFromJSON(BLogger logger, String jsonString, Class<?> dtoClass) throws BExceptions;
	Collection<Map<String, String>> getObjectFromJSONAsCollMap(BLogger logger, String jsonString) throws BExceptions;
	Map<String, String> getObjectFromJSONAsMap(BLogger logger, String jsonString) throws BExceptions;
	String getJSONFromObject(BLogger logger, Object javaPOJO) throws BExceptions;
	String encodeForHTML(BLogger logger, String inString) throws BExceptions;
	
	HTMLBuilder getHTMLBuilder(String templateFileName) throws BExceptions;
	
	ConcurrentTaskExecutor getConcurrentTaskExecutor() throws BExceptions;
}
