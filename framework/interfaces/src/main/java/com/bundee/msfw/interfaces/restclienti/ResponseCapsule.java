package com.bundee.msfw.interfaces.restclienti;

public interface ResponseCapsule {

	Object getResponseObject();
	Object getErrorObject(Class<?> errClass);
	byte[] getResponseBytes();
	int getRespSize();
}
