package com.bundee.msfw.defs;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

public class Utils {
	private static final int REF_YEAR = 2000;
	private static final double LEAP_YR_DIV_D = 400;
	private static final int LEAP_YR_DIV_I = 400;
	
	public static int getDay(Calendar cal) {
		int thisYR = cal.get(Calendar.YEAR);
		int day = 0;
		if(thisYR-1 > REF_YEAR) {
			for(int year = REF_YEAR; year <= thisYR-1; year++) {
				Calendar lcal = Calendar.getInstance();
				lcal.set(Calendar.YEAR, year);
				double yr = year;
				double div = yr/LEAP_YR_DIV_D;
				int iDiv = year/LEAP_YR_DIV_I;
				int days = 365;
				if(div == iDiv) {
					days++;
				}
				day += days; 
			}
		} else {
			day = cal.get(Calendar.DAY_OF_YEAR);			
		}
		return day;
	}

	public static int getWeek(Calendar cal) {
		int thisYR = cal.get(Calendar.YEAR);
		int week = 0;
		if(thisYR-1 > REF_YEAR) {
			week = (thisYR-1-REF_YEAR) * 52 + cal.get(Calendar.WEEK_OF_YEAR);
		} else {
			week = cal.get(Calendar.WEEK_OF_YEAR);			
		}
		return week;
	}
	
	public static String intCollection2String(Collection<Integer> coll, String sep) {
		String strValue = "";
		if(!coll.isEmpty()) {
			if(sep == null) sep = ",";
			StringBuffer sb = new StringBuffer();
			for(int iVal : coll) {
				sb.append(iVal).append(sep);
			}
			int lastSepIdx = sb.lastIndexOf(sep);
			if(lastSepIdx >= 0) {
				sb.replace(lastSepIdx, sb.length(), "");
			}
			strValue = sb.toString();
		}
		
		return strValue;
	}

	public static void string2IntCollection(String strValue, String sep, Collection<Integer> coll) {
		if(strValue != null && !strValue.isEmpty()) {
			if(sep == null) sep = ",";
			String[] values = strValue.split(sep);
			if(values != null && values.length > 0) {
				for(int idx = 0; idx < values.length; idx++) {
					coll.add(Integer.parseInt(values[idx]));
				}
			}
		}
	}

	public static void string2KeyValueMap(UTF8String strValue, String nvSep, String fieldSep, Map<String, UTF8String> map) {
		if(strValue != null && strValue.getUTF8String() != null && !strValue.isEmpty()) {
			if(fieldSep == null) fieldSep = "\n";
			if(nvSep == null) nvSep = "\t";
			String str = strValue.getUTF8String();
			String[] nvList = str.split(fieldSep);
			if(nvList != null && nvList.length > 0) {
				for(int idx = 0; idx < nvList.length; idx++) {
					String nvs = nvList[idx];
					String[] nv = nvs.split(nvSep);
					if(nv != null && nv.length > 1) {
						String key = nv[0];
						String value = nv[1];
						map.put(key, new UTF8String(value));
					}
				}
			}
		}
	}
	
	public static UTF8String keyValueMap2String(Map<String, UTF8String> map, String nvSep, String fieldSep) {
		if(fieldSep == null) fieldSep = "\n";
		if(nvSep == null) nvSep = "\t";
		StringBuffer sb = new StringBuffer(); 
		for(Map.Entry<String, UTF8String> pair : map.entrySet()) {
			sb.append(pair.getKey());
			sb.append(nvSep);
			sb.append(pair.getValue().getUTF8String());
			sb.append(fieldSep);
		}
		return new UTF8String(sb.toString());
	}

	public static String getValueFromArgs(String args[], String name) {
		String val = "";
		if (args == null || args.length < 2 || name == null || name.isEmpty())
			return val;

		for (int idx = 0; idx < args.length; idx++) {
			String arg = args[idx];
			if (arg.equalsIgnoreCase(name) && idx < args.length - 1) {
				val = args[idx + 1];
				break;
			}
		}

		return val;
	}

	public static ZonedDateTime toZDT(long millis) {
		Instant instant = Instant.ofEpochMilli(millis);
		ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return zdt;
	}
	
	public static boolean isNullOrEmptyOrBlank(UTF8String val) {
		return (val == null || val.getUTF8String() == null || val.getUTF8String().isBlank() || val.getUTF8String().isEmpty());
	}
	public static boolean isNullOrEmptyOrBlank(String val) {
		return (val == null || val.isBlank() || val.isEmpty());
	}
}
