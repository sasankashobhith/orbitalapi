package com.bundee.ums.utils;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.crypto.CryptoService;
import com.bundee.ums.defs.UMSProcessingCode;

public class TokenSerializer {
	private static final TokenSerializer ts = new TokenSerializer();
	private static final String TOK_SEP = ":";

	public class AuthToken {
		public enum TOKEN_TYPE {
			TT_USER((byte)0),
			TT_API((byte)1);
			
			private TOKEN_TYPE(byte val) {
				this.val = val;
			}
			byte val;
			
			public byte getVal() {
				return val;
			}
			
			public static TOKEN_TYPE get(byte val) {
				if(val == TT_API.getVal()) {
					return TT_API;
				}				
				return TT_USER;
			}
		};
		
		long timeMS;
		int userID;
		TOKEN_TYPE tokType;

		public long getTimeMS() {
			return timeMS;
		}

		public int getUserID() {
			return userID;
		}

		public TOKEN_TYPE getTokType() {
			return tokType;
		}

		AuthToken() {
		}

		// String tok = Long.toString(System.currentTimeMillis()) + ":" +
		// Integer.toString(tokType) + ":" + Integer.toString(userID);
		void deserialize(String plainTok) throws BExceptions {
			if (plainTok == null || plainTok.isBlank()) {
				throw new BExceptions(UMSProcessingCode.INVALID_AUTH_TOKEN, "token is null!");
			}
			String[] elems = plainTok.split(TOK_SEP);
			if (elems == null || elems.length != 3) {
				throw new BExceptions(UMSProcessingCode.INVALID_AUTH_TOKEN, "token is tampered!");
			}
			timeMS = Long.parseLong(elems[0]);
			tokType = TOKEN_TYPE.get(Byte.parseByte(elems[1]));
			userID = Integer.parseInt(elems[2]);
		}
	}

	public static String serializeToken(BLogger logger, BLModServices blms, AuthToken.TOKEN_TYPE tokType, int userID)
			throws BExceptions {
		try {
			String tok = Long.toString(System.currentTimeMillis()) + TOK_SEP + Byte.toString(tokType.getVal()) + TOK_SEP
					+ Integer.toString(userID);
			CryptoService cs = blms.getUtilFactory().getNewCryptoService();
			byte[] tokbytes = cs.encryptData(tok.getBytes());
			return cs.hexEncode(tokbytes);
		} catch (Throwable ex) {
			logger.error(ex);
			throw new BExceptions(UMSProcessingCode.INVALID_CONFIGURATION, ex.getMessage());
		}
	}

	public static AuthToken deserializeToken(BLogger logger, BLModServices blms, String authToken) throws BExceptions {
		if (authToken == null || authToken.isBlank()) {
			throw new BExceptions(UMSProcessingCode.INVALID_AUTH_TOKEN, "token is null!");
		}
		AuthToken at = null;
		try {
			CryptoService cs = blms.getUtilFactory().getNewCryptoService();
			byte[] tokbytes = cs.decryptData(cs.hexDecode(authToken));
			String plainTok = new String(tokbytes);
			at = ts.new AuthToken();
			at.deserialize(plainTok);
		} catch (Throwable ex) {
			logger.error(ex);
			throw new BExceptions(UMSProcessingCode.INVALID_AUTH_TOKEN, "token might be tampered!");
		}
		return at;
	}
}
