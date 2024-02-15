package com.bundee.msfw.servicefw.srvutils.email;

import com.bundee.msfw.defs.UTF8String;
import com.bundee.msfw.interfaces.emaili.EmailAttachment;

public class EmailAttachmentImpl implements EmailAttachment {

	UTF8String name;
	UTF8String mime;
	byte[] contents;
	
	public EmailAttachmentImpl() {
		mime = name = new UTF8String("");
		contents = new byte[1];
	}
	
	@Override
	public UTF8String getName() {
		return name;
	}

	@Override
	public UTF8String getMIME() {
		return mime;
	}

	@Override
	public byte[] getContents() {
		return contents;
	}

	@Override
	public void setName(UTF8String name) {
		if(name != null) {
			this.name = name;
		}
	}

	@Override
	public void setMIME(UTF8String mime) {
		if(mime != null) {
			this.mime = mime;
		}
	}

	@Override
	public void setContents(byte[] contents) {
		if(contents != null) {
			this.contents = contents;
		}
	}
}
