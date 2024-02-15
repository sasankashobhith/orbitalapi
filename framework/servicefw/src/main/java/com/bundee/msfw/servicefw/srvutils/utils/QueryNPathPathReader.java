package com.bundee.msfw.servicefw.srvutils.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.MultivaluedMap;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.blmodi.FormDataInput;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.servicefw.fw.FormDataInputImpl;
import com.google.common.collect.Lists;

public class QueryNPathPathReader {

	public static void readFormData(BLogger logger, Object pobject, Map<String, String> formHeaders, MultivaluedMap<String, byte[]> formData, BExceptions exceptions) {
		if (pobject == null || formHeaders == null || formData.isEmpty())
			return;
		
		Class<?> clazz = pobject.getClass();
		for(Field field : clazz.getDeclaredFields()) {
			try {
				field.setAccessible(true);
				if(field.getType() == FormDataInput.class) {
					FormDataInput mfi = new FormDataInputImpl(formHeaders, formData);
					field.set(pobject,(Object) mfi);
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	public static void readQueryParams(BLogger logger, Object object, Map<String, List<String>> qparameters, BExceptions exceptions) {
		if (object == null || qparameters == null || qparameters.isEmpty())
			return;
		
		Class<?> clazz = object.getClass();
		for(Field field : clazz.getDeclaredFields()) {
			if(qparameters.containsKey(field.getName())) {
				List<String> qVals = qparameters.get(field.getName());
				if(qVals != null && !qVals.isEmpty()) {
					CheckTypeAndAssignValue(logger, field, object, qVals);
				}
			}
		}
	}
	
	public static void readPathParams(BLogger logger, Object object, List<String> variables, String[] psegments, int varStartIdx, BExceptions exceptions) {
		if (object == null || variables == null || variables.isEmpty() || psegments == null || psegments.length <= varStartIdx || variables.size() != (psegments.length - varStartIdx))
			return;

		Class<?> clazz = object.getClass();
		Map<String, Field> fieldMap = new TreeMap<String, Field>(String.CASE_INSENSITIVE_ORDER);
		constructFieldMap(clazz.getDeclaredFields(), fieldMap);

		for(int idx = 0; (varStartIdx + idx) < psegments.length; idx++) {
			String varName = variables.get(idx);
			if(fieldMap.containsKey(varName)) {
				Field field = fieldMap.get(varName);
				CheckTypeAndAssignValue(logger, field, object, Lists.newArrayList(psegments[varStartIdx + idx]));
			}
		}
	}

	
	private static void CheckTypeAndAssignValue(BLogger logger, Field field, Object pobject, List<String> value) {
		if(value == null || value.get(0) == null) return;
		
		String valueStr = value.get(0);
		
		try {
			field.setAccessible(true);
			switch (field.getType().getSimpleName()) {
			case "Integer": {
				field.set(pobject, (Object)Integer.valueOf(Integer.parseInt(valueStr)));
				break;
			}
			case "Long": {
				field.set(pobject, (Object)Long.valueOf(Long.parseLong(valueStr)));
				break;
			}
			case "String": {
				field.set(pobject, valueStr);
				break;
			}
			case "UTF8String": {
				field.set(pobject, new UTF8String(valueStr));
				break;
			}case "Boolean":{
				field.set(pobject, (Object) Boolean.valueOf(Boolean.parseBoolean(valueStr)));
				break;
			}case "BigDecimal":{
					field.set(pobject, new BigDecimal(valueStr));
					break;
			}case "List":{
					if ("java.lang.Integer".equals((((ParameterizedType) field.getGenericType()).getActualTypeArguments())[0].getTypeName())) {
					    	List<Integer> integerList=new ArrayList<Integer>();
							for(String val:value){
								if(val != null) {
									integerList.add(Integer.valueOf(Integer.parseInt(val)));
								} else {
									integerList.add(null);
								}
							}
							field.set(pobject,(Object) integerList);
					}
					break;
				}case "FormDataInput": {
					field.set(pobject, null);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private  static void constructFieldMap(Field[] fields, Map<String, Field> fieldMap) {
		if(fieldMap == null || fields == null || fields.length == 0) return;
		for(Field field : fields) {
			fieldMap.put(field.getName(), field);
		}
	}

}
