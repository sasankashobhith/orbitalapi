package com.bundee.msfw.defs;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class UTF8String {
	public String javaString = "";
	public String utf8String = "";
	
	public UTF8String(String javaString) {
		if (javaString != null && !javaString.isEmpty()) {
			this.javaString = javaString.trim();
			ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(this.javaString);
			try {
				this.utf8String = new String(byteBuffer.array(), "UTF-8").trim();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getUTF8String() {
		return utf8String;
	}

	public String getJavaString() {
		return javaString;
	}
	
	public boolean isEmpty() {
		return utf8String.isEmpty();
	}

	public boolean isBlank() {
		return utf8String.isBlank();
	}
	
	public byte[] getBytes() {
		return utf8String.getBytes();
	}
	
	public UTF8String trim() {
		return this;
	}
	
	@Override
	public String toString() {
		return utf8String;
	}
	
	@Override
	public boolean equals(Object rhs) {
		boolean bRes = false;
		if(rhs instanceof UTF8String) {
			UTF8String urhs = (UTF8String)rhs;
			bRes = utf8String.equals(urhs.utf8String);
		} else if(rhs instanceof String) {
			String srhs = (String)rhs;
			bRes = utf8String.equals(srhs);
		}
		return bRes;
	}

	@Override
	public int hashCode() {
		return utf8String.hashCode();
	}

	public boolean equalsIgnoreCase(Object rhs) {
		boolean bRes = false;
		if(rhs instanceof UTF8String) {
			UTF8String urhs = (UTF8String)rhs;
			bRes = utf8String.equalsIgnoreCase(urhs.utf8String);
		} else if(rhs instanceof String) {
			String srhs = (String)rhs;
			bRes = utf8String.equalsIgnoreCase(srhs);
		}
		return bRes;
	}
}
