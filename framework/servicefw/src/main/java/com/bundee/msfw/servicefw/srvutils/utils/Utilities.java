package com.bundee.msfw.servicefw.srvutils.utils;

import java.io.File;
import java.util.*;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UniversalConstants;

public class Utilities {
	public static String trimAdvanced(String value) {

        Objects.requireNonNull(value);

        int strLength = value.length();
        int len = value.length();
        int st = 0;
        char[] val = value.toCharArray();

        if (strLength == 0) {
            return "";
        }

        while ((st < len) && (val[st] <= ' ') || (val[st] == '\u00A0')) {
            st++;
            if (st == strLength) {
                break;
            }
        }
        while ((st < len) && (val[len - 1] <= ' ') || (val[len - 1] == '\u00A0')) {
            len--;
            if (len == 0) {
                break;
            }
        }


        return (st > len) ? "" : ((st > 0) || (len < strLength)) ? value.substring(st, len) : value;
    }

	public static List<String> getValueList(String valStr, String sep) {
		List<String> vals = new ArrayList<String>();
		if(sep == null || sep.isEmpty()) sep = ",";
		if(valStr == null || valStr.isBlank()) return vals;
		String[] splitVals = valStr.split(sep);
		for(String val : splitVals) {
			vals.add(val);
		}
		return vals;		
	}
	
	public static Set<String> getValueSet(String valStr, String sep) {
		Set<String> vals = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		if(sep == null || sep.isEmpty()) sep = ",";
		if(valStr == null || valStr.isBlank()) return vals;
		String[] splitVals = valStr.split(sep);
		for(String val : splitVals) {
			vals.add(val);
		}
		return vals;		
	}

	public static String endWithSlash(String path) {
		if(path == null) path = "";
		return (path.isBlank() || !path.endsWith(File.separator) ? path + File.separator : path);
	}
	
	public static void extractErrCodes(Object codes, BExceptions exceptions) {
		if(codes != null && codes instanceof BResponseCodes) {
			BResponseCodes brc = (BResponseCodes) codes;
			exceptions.add(brc);
		}
	}
	public static String getValueFromHeader(Map<String, List<String>> reqHeaders, String name) {
		List<String> values = reqHeaders.get(name);
		if (values == null || values.isEmpty()) {
			return null;
		}
		return values.get(0);
	}

	public static void setValueInHeader(Map<String, List<String>> reqHeaders, String name, String value) {
		if(value == null) return;

		List<String> values = reqHeaders.get(name);
		if (values == null || values.isEmpty()) {
			values = new ArrayList<String>();
			reqHeaders.put(name, values);
		}
		values.add(value);
	}

	public static String getClientIpAddr(Map<String, List<String>> reqHeaders) {
		String ipAddr = getValueFromHeader(reqHeaders, "X-Forwarded-For");

		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "Proxy-Client-IP");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "WL-Proxy-Client-IP");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "HTTP_X_FORWARDED_FOR");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "HTTP_X_FORWARDED");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "HTTP_X_CLUSTER_CLIENT_IP");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "HTTP_CLIENT_IP");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "HTTP_FORWARDED_FOR");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "HTTP_FORWARDED");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "HTTP_VIA");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, "REMOTE_ADDR");
		}
		if (ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
			ipAddr = getValueFromHeader(reqHeaders, UniversalConstants.HOST_HEADER);
		}

		return ipAddr;
	}
}


