package com.bundee.msfw.servicefw.srvutils.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.html.DynamicRow;
import com.bundee.msfw.interfaces.utili.html.DynamicRowSet;
import com.bundee.msfw.interfaces.utili.html.HTMLBuilder;

public class HTMLBuilderImpl implements HTMLBuilder {
	//TODO: private static final String SYSTEM_YEAR_KEY = "SYSTEM_YEAR";
	
	private static final String DEF_ROW_STYLE = "<td style=\"font-family:'Segoe UI'; font-size:10px;border:1px solid black;text-align:center;\">ROW_VALUE</td>";
	private static final String DYNAMIC_TABLE_ROWS_MARKER = "##";
	private static final String DYNAMIC_TABLE_ROWS_START_PFX = "DYNAMIC_TABLE_ROWS_START_";
	private static final String DYNAMIC_TABLE_ROWS_END_PFX = "DYNAMIC_TABLE_ROWS_END_";
	private static final String FIELD_VALUE_KEY = "FIELD_VALUE";
	
	private static final String FINAL_DYNAMIC_TABLE_ROWS_PFX = "##FINAL_DYNAMIC_TABLE_ROWS_%s##";

	class DynamicRowSetImpl implements DynamicRowSet {
		String tag;
		String style = "";
		String finalKey = "";
		List<DynamicRowImpl> rows;

		DynamicRowSetImpl(String tag) {
			this.tag = tag;
			rows = new ArrayList<DynamicRowImpl>();
		}

		@Override
		public DynamicRow addRow(String colorCode) {
			DynamicRowImpl dr = new DynamicRowImpl(colorCode);
			rows.add(dr);
			return dr;
		}
	}

	class DynamicRowImpl implements DynamicRow {
		String colorCode;
		List<String> values;

		DynamicRowImpl(String colorCode) {
			this.colorCode = colorCode;
			values = new ArrayList<String>();
		}

		@Override
		public void addColumn(String value) {
			value = (value != null ? value : "");
			values.add(value);
		}
	}

	private String templateContents = "";
	private Map<String, DynamicRowSetImpl> dynamicRowsMap = new TreeMap<String, DynamicRowSetImpl>(
			String.CASE_INSENSITIVE_ORDER);
	private Map<String, String> keyValueMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	
	private Map<String, String> preDefinedKeyValueMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

	HTMLBuilderImpl(String templateFileName) throws BExceptions {
		//TODO:
		/*
		Application application = ServiceFramework.getInstance().getApplication();
		if (application.getTemplatesFolder() == null) {
			throw new BExceptions(FwConstants.PCodes.CONFIGURATION_MISSING, "templates folder is not present!");
		}
		if (templateFileName == null || templateFileName.isBlank()) {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "templateFileName is null or empty!");
		}
		try {
			templateContents = Files.readString(Paths.get(application.getTemplatesFolder(), templateFileName));
		} catch (IOException e) {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE,
					"templateFileName: " + templateFileName + " does not exist!");
		}
		initPredefinedKVMap();
		*/
	}
	
	/*TODO
	private void initPredefinedKVMap() {
		preDefinedKeyValueMap.put(SYSTEM_YEAR_KEY, Integer.toString(Year.now().getValue()));
	}
	*/

	@Override
	public DynamicRowSet addDynamicRowSet(String tag) throws BExceptions {
		if (tag == null || tag.isBlank())
			return null;

		String dynamicRowSetStartMarker = DYNAMIC_TABLE_ROWS_MARKER + DYNAMIC_TABLE_ROWS_START_PFX + tag;
		String dynamicRowSetEndMarker = DYNAMIC_TABLE_ROWS_END_PFX + tag + DYNAMIC_TABLE_ROWS_MARKER;
		DynamicRowSetImpl drs = dynamicRowsMap.get(tag);
		if (drs == null) {
			int keyStartIdx = templateContents.indexOf(dynamicRowSetStartMarker);
			int keyEndIdx = templateContents.indexOf(dynamicRowSetEndMarker);
			if (keyStartIdx > 0 && keyEndIdx > 0) {
				drs = new DynamicRowSetImpl(tag);
				dynamicRowsMap.put(tag, drs);
				drs.style = templateContents.substring(keyStartIdx + dynamicRowSetStartMarker.length(), keyEndIdx);
				drs.style = (drs.style != null && !drs.style.isBlank() ? drs.style : DEF_ROW_STYLE);
		
				String dynamicRowSetKey = templateContents.substring(keyStartIdx, keyEndIdx + dynamicRowSetEndMarker.length());
				drs.finalKey = String.format(FINAL_DYNAMIC_TABLE_ROWS_PFX, tag);
				templateContents = templateContents.replaceAll(dynamicRowSetKey, drs.finalKey);
			} else {
				throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Expected start and end markers missing: " + dynamicRowSetStartMarker + " " + dynamicRowSetEndMarker);				
			}
		}
		return drs;
	}

	@Override
	public void setValue(String name, String value) {
		if(name != null && !name.isBlank()) {
			value = (value != null ? value : "");
			keyValueMap.put(name, value);
		}
	}

	@Override
	public String build(BLogger logger) throws BExceptions {
		if(templateContents == null || templateContents.isBlank()) return "";
		fillKVMap(keyValueMap);
		fillKVMap(preDefinedKeyValueMap);
		processDynamicRows();
		return templateContents;
	}
	
	private void fillKVMap(Map<String, String> map) {
		for(Map.Entry<String, String> pair : map.entrySet()) {
			templateContents = templateContents.replaceAll(pair.getKey(), pair.getValue());
		}
	}
	
	private void processDynamicRows() {
		for(Map.Entry<String, DynamicRowSetImpl> pair : dynamicRowsMap.entrySet()) {
			DynamicRowSetImpl drs = pair.getValue();
			StringBuffer buffer = new StringBuffer();
			for(DynamicRowImpl dr : drs.rows) {
				buffer.append("<tr bgcolor=\"").append(dr.colorCode).append("\">");
				for(String cval : dr.values) {
					String colTD = drs.style.replaceAll(FIELD_VALUE_KEY, cval);
					buffer.append(colTD);
				}
				buffer.append("</tr>");
			}
			templateContents = templateContents.replaceAll(drs.finalKey, buffer.toString());
		}
	}
}
