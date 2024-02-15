package com.bundee.msfw.servicefw.fw;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import com.bundee.msfw.interfaces.blmodi.FormDataInput;

public class FormDataInputImpl implements FormDataInput {
	private Map<String, String> formHeaders;
	private MultivaluedMap<String, byte[]> formData;

	public FormDataInputImpl(Map<String, String> formHeaders, MultivaluedMap<String, byte[]> formData) {
		this.formHeaders = formHeaders;
		this.formData = formData;
	}
	@Override
	public Collection<String> getFieldNames() {
		return formHeaders.keySet();
	}

	@Override
	public String getContentDisposition(String fieldName) {
		return formHeaders.get(fieldName);
	}

	@Override
	public byte[] getContents(String fieldName) {
		List<byte[]> values = formData.get(fieldName);
		return (values == null || values.isEmpty() ? null : values.get(0));
	}

}
