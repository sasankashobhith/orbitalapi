package com.bundee.msfw.servicefw.srvutils.utils;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.defs.UniversalConstants;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.validi.BCollection;
import com.bundee.msfw.interfaces.validi.BContainsHTML;
import com.bundee.msfw.interfaces.validi.BMandatory;
import com.bundee.msfw.interfaces.validi.BMandatoryBoolean;
import com.bundee.msfw.interfaces.validi.BMandatoryInteger;
import com.bundee.msfw.interfaces.validi.BMandatoryLong;
import com.bundee.msfw.interfaces.validi.BMandatoryString;
import com.bundee.msfw.interfaces.validi.BMaxStringLength;
import com.bundee.msfw.interfaces.validi.CommonValidator;

public class AnnotatedFieldValidator {
	private static final PolicyFactory DISALLOW_ALL = new HtmlPolicyBuilder().toFactory();

	private static final Set<Class<?>> skipAnnotations = new HashSet<>(Arrays.asList(BContainsHTML.class));

	private static final List<Class<?>> validAnnotations = new ArrayList<>(Arrays.asList(BMandatory.class,
			BMandatoryString.class, BMandatoryLong.class, BMaxStringLength.class, BCollection.class,
			BMandatoryInteger.class, BMandatoryBoolean.class));

	public static void validate(BLogger logger, Object object, CommonValidator commonValidator,
			BExceptions exceptions) {
		if (object == null)
			return;

		Class<?> clazz = object.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			performUnconditionalValidations(logger, field, object, exceptions);
			Arrays.stream(field.getAnnotations()).map(annotationItem -> annotationItem.annotationType())
					.filter(annotationClass -> validAnnotations.contains(annotationClass)).forEach(validAnnotation -> {
						try {
							callCommonValidatorMethod(logger, validAnnotation, object, commonValidator,
									exceptions, field);
						} catch (IllegalAccessException e) {
							exceptions.add(FwConstants.PCodes.INTERNAL_ERROR, UniversalConstants.ERROR_MESSAGE_ILLEGAL_ACCESS + ":" + field.getName());
							e.printStackTrace();
						}
					});
		}
	}

	private static void callCommonValidatorMethod(BLogger logger, Class<?> annotation, Object pobject,
			CommonValidator commonValidator, BExceptions exceptions, Field field)
			throws IllegalAccessException {
		switch (annotation.getSimpleName()) {
		case "BMandatoryCollection": {
			Collection<?> coll = (Collection<?>) field.get(pobject);
			commonValidator.validateCollection(field.getName(), coll, exceptions);
		}
			break;
		case "BMandatoryString": {
			if (field.get(pobject) != null && field.get(pobject).getClass().getSimpleName().equals("UTF8String")) {
				commonValidator.validateUTF8String(field.getName(), (UTF8String) field.get(pobject), exceptions);
			} else {
				commonValidator.validateString(field.getName(), (String) field.get(pobject), exceptions);
			}
		}
			break;
		case "BMandatoryLong":
		case "BMandatory":
		case "BMandatoryInteger":
		case "BMandatoryBoolean": {
			commonValidator.validateIsFieldNull(pobject, exceptions, field);
		}
			break;
		case "BMaxStringLength": {
			commonValidator.validateMaxStringLength(pobject, exceptions, field);
		}
			break;
		case "BCollection": {
			Collection<?> coll = (Collection<?>) field.get(pobject);
			if (coll != null && !coll.isEmpty()) {
				for (Object obj : coll) {
					validate(logger, obj, commonValidator, exceptions);
				}
			}
		}
			break;
		}
	}

	private static void performUnconditionalValidations(BLogger logger, Field field, Object pobject,
			BExceptions exceptions) {
		List<Annotation> annotations = Arrays.asList(field.getAnnotations());
		boolean bSkipValidation = false;

		for (Annotation annotation : annotations) {
			if (skipAnnotations.contains(annotation.annotationType())) {
				bSkipValidation = true;
				break;
			}
		}

		if(bSkipValidation) {
			logger.debug("skipping validation: " + field.getName());
			return;
		}
		
		try {
			Object strobj = field.get(pobject);
			String value = null;
			if (strobj instanceof UTF8String) {
				UTF8String ustr = (UTF8String) strobj;
				value = (ustr != null && ustr.getUTF8String() != null ? ustr.getUTF8String() : null);
			} else if (strobj instanceof String) {
				value = (String) strobj;
			}

			if (value != null && !value.isBlank()) {
				String sanitized = DISALLOW_ALL.sanitize(value);
				String decodedSanitizedValue = StringEscapeUtils.unescapeHtml4(sanitized);
				if (!decodedSanitizedValue.equals(value)) {
					exceptions.add(FwConstants.PCodes.INVALID_VALUE, "Contains unacceptable HTML tags! " + field.getName());
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			exceptions.add(e, FwConstants.PCodes.INVALID_VALUE);
		}
	}
}
