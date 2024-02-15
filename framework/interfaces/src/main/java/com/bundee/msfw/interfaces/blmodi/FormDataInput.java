package com.bundee.msfw.interfaces.blmodi;

import java.util.Collection;

public interface FormDataInput {
	Collection<String> getFieldNames();
	String getContentDisposition(String fieldName);
	byte[] getContents(String fieldName);
}
