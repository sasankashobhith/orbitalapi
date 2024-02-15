package com.bundee.msfw.servicefw.srvutils.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.defs.UniversalConstants;
import com.bundee.msfw.interfaces.validi.BMaxStringLength;
import com.bundee.msfw.interfaces.validi.CommonValidator;

public class CommonValidatorImpl implements CommonValidator {
	private static final int INVALID_VALUE = -111111; 

	public CommonValidatorImpl() {
	}
	
	@Override
	public void validateString(String name, String value, BExceptions exceptions) {
		if(value == null || value.isEmpty()) {
			exceptions.add(FwConstants.PCodes.MANDATORY_FIELD_MISSING, name + " is a mandatory field!");
		}
	}

	@Override
	public void validateUTF8String(String name, UTF8String value, BExceptions exceptions) {
		try {
			validateUTF8String(name, value);
		} catch (BExceptions e) {
			exceptions.add(e);
		}
	}

	@Override
	public void validateGreaterThanInt(String name, int value, int minValue, BExceptions exceptions) {
		if(value < minValue) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, name + " has invalid value!");
		}
	}

	@Override
	public void validateLesserThanInt(String name, int value, int maxValue, BExceptions exceptions) {
		if(value > maxValue) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, name + " has invalid value!");
		}
	}

	@Override
	public void validateRangeInt(String name, int value, int lowerBound, int upperBound, BExceptions exceptions) {
		if(value < lowerBound || value > upperBound) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, name + " has invalid value!");
		}
	}

	@Override
	public void validateCollection(String name, Collection<?> value, BExceptions exceptions) {
		if(value == null || value.isEmpty()) {
			exceptions.add(FwConstants.PCodes.MANDATORY_FIELD_MISSING, name + " is a mandatory field!");
		}
	}

	@Override
	public void validateMap(String name, Map<?, ?> value, BExceptions exceptions) {
		if(value == null || value.isEmpty()) {
			exceptions.add(FwConstants.PCodes.MANDATORY_FIELD_MISSING, name + " is a mandatory field!");
		}
	}
	
	public void validateEmailAddress(String name, UTF8String value, BExceptions exceptions) {
		try {
			validateUTF8String(name, value);
			InternetAddress emailAddr = new InternetAddress(value.getUTF8String());
			emailAddr.validate();
		} catch (BExceptions e) {
			exceptions.add(e);
		} catch (AddressException e) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, name + " is invalid email format!");
		}
	}
	
	private void validateUTF8String(String name, UTF8String value) throws BExceptions {
		if(value == null || value.getUTF8String() == null || value.getUTF8String().isEmpty()) {
			throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, name + " is a mandatory field!");
		}
	}

	@Override
	public void validateMandatoryAndPositiveLong(String name, Long val, BExceptions exceptions) {

		if(val == null || val == INVALID_VALUE) {
			exceptions.add(FwConstants.PCodes.MANDATORY_FIELD_MISSING, name + " is a mandatory field!");
		} else if(val <= 0) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, name + " value is invalid");
		}
	}

	@Override
	public void validateTenantID(String name, int val, BExceptions exceptions) {
		if(val == INVALID_VALUE) {
			exceptions.add(FwConstants.PCodes.MANDATORY_FIELD_MISSING, name + " is a mandatory field!");
		} else if(val < 0) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, name + " value is invalid");
		}
	}
	
	@Override
	public void validateMandatoryAndPositiveInt(String name, int val, BExceptions exceptions) {
		if(val == INVALID_VALUE) {
			exceptions.add(FwConstants.PCodes.MANDATORY_FIELD_MISSING, name + " is a mandatory field!");
		} else if(val <= 0) {
			exceptions.add(FwConstants.PCodes.INVALID_VALUE, name + " value is invalid");
		}
	}

	@Override
	public void validateIsFieldNull(Object object, BExceptions exceptions, Field field) {
		try {
			Object value = field.get(object);
			if (value == null) {
				exceptions.add(FwConstants.PCodes.INVALID_VALUE,
						String.format(UniversalConstants.ERROR_MESSAGE_MANDATORY_FIELD,field.getName()));
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			exceptions.add(e, FwConstants.PCodes.INVALID_VALUE);
		}
	}

	@Override
	public void validateMaxStringLength(Object object, BExceptions exceptions, Field field) {
		try {
			String value = (String) field.get(object);
			if (value == null || value.length() > field.getAnnotation(BMaxStringLength.class).length()) {
				exceptions.add(FwConstants.PCodes.INVALID_VALUE,
						field.getName() + UniversalConstants.ERROR_MESSAGE_MAX_STRING_LENGTH
								+ ":" + field.getAnnotation(BMaxStringLength.class).length() + ":" + field.getName());
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			exceptions.add(e, FwConstants.PCodes.INVALID_VALUE);
		}
	}
}
