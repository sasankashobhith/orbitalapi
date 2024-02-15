package com.bundee.msfw.interfaces.utili.crypto;

import com.bundee.msfw.defs.BExceptions;

public interface CryptoService {
	public String base64Encode(byte[] bytes) throws BExceptions;
	public byte[] base64Decode(String str) throws BExceptions;
	
	public String hexEncode(byte[] bytes) throws BExceptions;
	public byte[] hexDecode(String str) throws BExceptions;
	
	public byte[] hashData(byte[] bytes) throws BExceptions;

	public byte[] encryptData(byte[] bytes) throws BExceptions;
	public byte[] decryptData(byte[] bytes) throws BExceptions;
	
	public byte[] encryptSearchableData(byte[] bytes) throws BExceptions;
	public byte[] decryptSearchableData(byte[] bytes) throws BExceptions;
}
