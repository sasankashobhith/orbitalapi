package com.bundee.msfw.servicefw.srvutils.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.csv.BCSVWriter;
import com.bundee.msfw.interfaces.utili.csv.Column;
import com.bundee.msfw.interfaces.utili.csv.Row;
import com.opencsv.CSVWriter;

public class BCSVWriterImpl implements BCSVWriter {
	List<BRowImpl> hdrRows;
	List<BRowImpl> dataRows;

	public BCSVWriterImpl() {
		hdrRows = new ArrayList<BRowImpl>();
		dataRows = new ArrayList<BRowImpl>();
	}

	@Override
	public Row addNewHeaderRow() {
		BRowImpl row = new BRowImpl(hdrRows.size());
		hdrRows.add(row);
		return row;
	}

	@Override
	public Row addNewDataRow() {
		BRowImpl row = new BRowImpl(dataRows.size());
		dataRows.add(row);
		return row;
	}

	@Override
	public byte[] writeCSV(BLogger logger) throws BExceptions {
		BExceptions exceptions = new BExceptions();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Writer writer = new BufferedWriter(new OutputStreamWriter(bos));
		CSVWriter csvWriter = new CSVWriter(writer);
		List<String[]> allData = new ArrayList<String[]>();
		rows2CSVValues(hdrRows, allData);
		rows2CSVValues(dataRows, allData);
		csvWriter.writeAll(allData);
		try {
			csvWriter.flush();
			csvWriter.close();
			bos.close();
		} catch (IOException e) {
			exceptions.add(e, FwConstants.PCodes.INTERNAL_ERROR);
			throw exceptions;
		}
		return bos.toByteArray();
	}

	private void rows2CSVValues(List<BRowImpl> rows, List<String[]> allData) {
		for(BRowImpl row : rows) {
			Map<Integer, Column> cols = row.getColumns();
			String[] colStr = new String[cols.size()];
			allData.add(colStr);
			int idx = 0;
			for(Map.Entry<Integer, Column> pair : cols.entrySet()) {
				colStr[idx++] = pair.getValue().getStrValue().getUTF8String();
			}
		}
	}
}
