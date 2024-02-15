package com.bundee.msfw.interfaces.emaili;

import com.bundee.msfw.defs.UTF8String;

public interface EmailAttachment {
	public UTF8String getName();
	public UTF8String getMIME();
	public byte[] getContents();

	public void setName(UTF8String name);
	public void setMIME(UTF8String mime);
	public void setContents(byte[] contents);
}
