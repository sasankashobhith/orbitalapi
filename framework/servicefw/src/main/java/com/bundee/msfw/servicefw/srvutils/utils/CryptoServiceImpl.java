package com.bundee.msfw.servicefw.srvutils.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.utili.crypto.CryptoService;

public class CryptoServiceImpl implements CryptoService {
	private static final String DATA_ENC_KEY  = "`~!@#$%^&*/-_=+?";

	//This should be 16 bytes and NEVER CHANGE THIS!
	private static final byte[] fixed_padding = new byte[] { (byte)0xe3, 0x4f, 0x76, 0x2c, 0x1d, 0x2e, 0x50, 0x79, 0x6f, 0x26, (byte)0x80, 0x0f, 0x0a, 0x0c, 0x2e, 0x5d };
	
	@Override
	public String base64Encode(byte[] bytes) throws BExceptions {
		Base64.Encoder encoder = Base64.getEncoder(); 
		return encoder.encodeToString(bytes);
	}

	@Override
	public byte[] base64Decode(String str) throws BExceptions {
		Base64.Decoder decoder = Base64.getDecoder(); 
		return decoder.decode(str);
	}

	@Override
	public String hexEncode(byte[] bytes) throws BExceptions {
		return Hex.encodeHex(bytes);
	}

	@Override
	public byte[] hexDecode(String str) throws BExceptions {
		return Hex.decodeHex(str);
	}

	@Override
	public byte[] hashData(byte[] bytes) throws BExceptions {
		byte[] hashed_bytes = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			hashed_bytes = digest.digest(bytes);
		} catch (Exception ex) {
			throw new BExceptions(ex, FwConstants.PCodes.INVALID_VALUE);
		}
		return hashed_bytes;
	}

	@Override
	public byte[] encryptData(byte[] bytes) throws BExceptions {
		return encrypt(DATA_ENC_KEY, bytes, null);
	}

	@Override
	public byte[] decryptData(byte[] bytes) throws BExceptions {
		return decrypt(DATA_ENC_KEY, bytes);
	}

	@Override
	public byte[] encryptSearchableData(byte[] bytes) throws BExceptions {
		return encrypt(DATA_ENC_KEY, bytes, fixed_padding);
	}

	@Override
	public byte[] decryptSearchableData(byte[] bytes) throws BExceptions {
		return decrypt(DATA_ENC_KEY, bytes);
	}

	public static byte[] encrypt(String keyStr, byte[] bytes, byte[] padding) throws BExceptions {
		byte[] finalEnc = null;
		
		try {
			byte[] ivBytes = padding;
			if(ivBytes == null) {
				SecureRandom sr = new SecureRandom();
				ivBytes = new byte[16];
				sr.nextBytes(ivBytes);
			}
			
			IvParameterSpec iv = new IvParameterSpec(ivBytes);
			SecretKeySpec skeySpec = new SecretKeySpec(keyStr.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(bytes);
			finalEnc = new byte[encrypted.length + ivBytes.length];
			System.arraycopy(ivBytes, 0, finalEnc, 0, ivBytes.length);
			System.arraycopy(encrypted, 0, finalEnc, ivBytes.length, encrypted.length);

		} catch (Exception ex) {
			throw new BExceptions(ex, FwConstants.PCodes.INVALID_VALUE);
		}
		
		return finalEnc;
	}

	public static byte[] decrypt(String key, byte[] encBytes) throws BExceptions {
		if (encBytes == null || encBytes.length == 0 || key == null || key.isEmpty()) {
			throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Looks like the encrypted field is empty!");
		}

		byte[] finalDec = null;
		
		try {
			byte[] ivBytes = Arrays.copyOfRange(encBytes, 0, 16);
			byte[] finEncBytes = Arrays.copyOfRange(encBytes, 16, encBytes.length);

			IvParameterSpec iv = new IvParameterSpec(ivBytes);
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			finalDec = cipher.doFinal(finEncBytes);
		} catch (Exception ex) {
			throw new BExceptions(ex, FwConstants.PCodes.INVALID_VALUE);
		}
		
		return finalDec;
	}
}
