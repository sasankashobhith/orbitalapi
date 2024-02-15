package com.bundee.msfw.servicefw.srvutils.utils;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.csv.BCSVReader;
import com.bundee.msfw.interfaces.utili.csv.Row;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

public class BCSVReaderImpl implements BCSVReader {
	List<BRowImpl> rows;
	CSVReader csvReader = null;
	int idx = 0;

	public BCSVReaderImpl(Reader reader, int numHeaderRows) throws BExceptions {
		CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(false).build();
		csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).withSkipLines(numHeaderRows).build();
		readRows(csvReader);
	}

	public BCSVReaderImpl(Reader reader, char seperator, int numHeaderRows, boolean bLarge) throws BExceptions {
		CSVParser parser = new CSVParserBuilder().withSeparator(seperator).withIgnoreQuotations(false).build();
		csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).withSkipLines(numHeaderRows).build();
		if (!bLarge) {
			readRows(csvReader);
		}
	}

	@Override
	public int getNumRows() {
		return rows.size();
	}

	@Override
	public Row getRow(int idx) throws BExceptions {
		BExceptions excpetions = new BExceptions();
		if (idx < 0 || idx >= rows.size()) {
			int re = (rows.size() > 0 ? rows.size() - 1 : 0);
			excpetions.add(FwConstants.PCodes.INVALID_VALUE, idx + " exceeds the range: [0:" + re + "]");
			throw excpetions;
		}
		return rows.get(idx);
	}

	private void readRows(CSVReader csvReader) throws BExceptions {
		rows = new ArrayList<BRowImpl>();
		List<String[]> allData;
		try {
			allData = csvReader.readAll();
			for (; idx < allData.size(); idx++) {
				BRowImpl row = new BRowImpl(idx);
				rows.add(row);

				String[] cols = allData.get(idx);
				buildRow(row, cols);
			}
		} catch (IOException | CsvException e) {
			BExceptions excpetions = new BExceptions();
			excpetions.add(e, FwConstants.PCodes.INVALID_VALUE);
			throw excpetions;
		}
	}

	@Override
	public Row getNextRow(BLogger logger) throws BExceptions {
		try {
			String[] cols = csvReader.readNext();
			if(cols == null) {
				return null;
			}
			BRowImpl row = new BRowImpl(idx);
			buildRow(row, cols);
			return row;
		} catch (IOException | CsvValidationException e) {
			BExceptions excpetions = new BExceptions();
			excpetions.add(e, FwConstants.PCodes.INVALID_VALUE);
			throw excpetions;
		}
	}

	private void buildRow(BRowImpl row, String[] cols) {
		if (cols == null || cols.length == 0)
			return;

		for (int cdx = 0; cdx < cols.length; cdx++) {
			String col = cols[cdx];
			row.addColumnValue(cdx, col);
		}
	}
}
