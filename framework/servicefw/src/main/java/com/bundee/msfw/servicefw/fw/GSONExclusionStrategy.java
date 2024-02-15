package com.bundee.msfw.servicefw.fw;

import java.util.Set;
import java.util.TreeSet;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class GSONExclusionStrategy implements ExclusionStrategy {
    Set<String> fields2Skip;
    
    public GSONExclusionStrategy() {
    	fields2Skip = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    	fields2Skip.add("detailMessage");
    	fields2Skip.add("stackTrace");
    	fields2Skip.add("suppressedExceptions");
    }
    
	@Override
	public boolean shouldSkipField(FieldAttributes field) {
		boolean bSkip = false;
		Class<?> fieldClass = field.getDeclaringClass();

		if(fieldClass != null && fieldClass == Throwable.class && fields2Skip.contains(field.getName())) {
			bSkip = true;
		}
		return bSkip;
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}
};