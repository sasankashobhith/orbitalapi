package com.bundee.msfw.interfaces.validi;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;

public interface CommonValidator {
	void validateString(String name, String value, BExceptions exceptions);
	void validateUTF8String(String name, UTF8String str, BExceptions exceptions);
	
	void validateMandatoryAndPositiveLong(String name, Long val, BExceptions exceptions);
	void validateMandatoryAndPositiveInt(String name, int val, BExceptions exceptions);
	void validateTenantID(String name, int val, BExceptions exceptions);
	
	void validateGreaterThanInt(String name, int val, int minValue, BExceptions exceptions);
	void validateLesserThanInt(String name, int val, int maxValue, BExceptions exceptions);
	void validateRangeInt(String name, int val, int lowerBound, int upperBound, BExceptions exceptions);
	void validateCollection(String name, Collection<?> coll, BExceptions exceptions);
	void validateMap(String name, Map<?, ?> map, BExceptions exceptions);
	void validateEmailAddress(String name, UTF8String str, BExceptions exceptions);
	void validateIsFieldNull(Object object, BExceptions exceptions, Field field);
	void validateMaxStringLength(Object object, BExceptions exceptions, Field field);
}
