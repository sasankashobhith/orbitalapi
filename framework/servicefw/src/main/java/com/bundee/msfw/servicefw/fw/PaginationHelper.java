package com.bundee.msfw.servicefw.fw;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.Utils;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.dbi.DBException;
import com.bundee.msfw.interfaces.dbi.DBManager;
import com.bundee.msfw.interfaces.dbi.DBQuery;
import com.bundee.msfw.interfaces.dbi.DBQueryBuilder;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.crypto.CryptoService;
import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;
import com.bundee.msfw.servicefw.srvutils.utils.Utilities;

import java.math.BigDecimal;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaginationHelper {
	private static PaginationHelper ph = new PaginationHelper();

	// version:timestamp:id:curIdx
	private class PaginationToken {
		int version = 0;
		long timestamp = System.currentTimeMillis() / 1000;
		long id = -1L;
		int curIdx = 0;

		boolean isEmpty = false;
		boolean isPagingOver = false;
		int pgSize;
		List<Long> ids;
		String api;

		PaginationToken(int pgSize) {
			this.pgSize = pgSize;
		}

		List<Long> getNextIDs() throws BExceptions {
			if (ids == null) {
				throw new BExceptions(FwConstants.PCodes.INTERNAL_ERROR, "Something went wrong!");
			}
			int idx = curIdx;
			int resIdx = curIdx + pgSize;
			List<Long> pageIDs = new ArrayList<Long>();
			isPagingOver = false;
			if (resIdx > ids.size()) {
				resIdx = ids.size();
				isPagingOver = true;
			} else {
				curIdx += pgSize;
			}
			for (; idx < resIdx; idx++) {
				pageIDs.add(ids.get(idx));
			}

			return pageIDs;
		}

		void deserialize(BLogger logger, String token, BLModServices blModServices) throws BExceptions {
			if (Utils.isNullOrEmptyOrBlank(token)) {
				isEmpty = true;
			} else {
				String plainTok = null;
				try {
					CryptoService cs = blModServices.getUtilFactory().getNewCryptoService();
					byte[] decBytes = cs.hexDecode(token);
					byte[] decrBytes = cs.decryptData(decBytes);
					plainTok = new String(decrBytes);
				} catch (BExceptions bex) {
					logger.error(bex);
					throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Page Token is not valid, tampered?");
				}
				if (Utils.isNullOrEmptyOrBlank(plainTok)) {
					throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Page Token is not valid, tampered?");
				}
				String[] tokens = plainTok.split(":");
				if (tokens == null || tokens.length != 4) {
					throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "Page Token is not valid, tampered?");
				}
				version = Integer.parseInt(tokens[0]);
				timestamp = Long.parseLong(tokens[1]);
				id = Long.parseLong(tokens[2]);
				curIdx = Integer.parseInt(tokens[3]);
			}
		}

		String serialize(BLogger logger, BLModServices blModServices) throws BExceptions {
			if (isPagingOver)
				return null;

			String plainTok = toString();
			CryptoService cs = blModServices.getUtilFactory().getNewCryptoService();
			byte[] encBytes = cs.encryptData(plainTok.getBytes());
			return cs.hexEncode(encBytes);
		}

		@Override
		public String toString() {
			return version + ":" + timestamp + ":" + id + ":" + curIdx;
		}

		void log(BLogger logger) {
			logger.debug(api + ":" + toString());
		}
	}

	public static boolean preHandlePagination(BLogger logger, BLModServices blModServices,
			ReqRespObjCapsule reqRespCapsule) throws BExceptions {
		boolean bHandled = false;
		if (reqRespCapsule.pageHandler != null) {
			String pageToken = Utilities.getValueFromHeader(reqRespCapsule.reqHeaders,
					FwConstants.GENERAL_CONSTANTS.PAGE_TOKEN);
			PaginationToken pt = ph.new PaginationToken(reqRespCapsule.pageHandler.getPageSize());
			pt.api = reqRespCapsule.sr.varH.epd.uri;
			pt.deserialize(logger, pageToken, blModServices);
			pt.log(logger);
			reqRespCapsule.pageToken = pt;
			bHandled = !pt.isEmpty;
		}
		return bHandled;
	}

	public static void postHandlePagination(BLogger logger, BLModServices blModServices,
			ReqRespObjCapsule reqRespCapsule) throws BExceptions {
		if (reqRespCapsule.pageToken != null) {
			PaginationToken pt = (PaginationToken) reqRespCapsule.pageToken;
			List<Long> pageIDs = null;
			if (pt.isEmpty) {
				pt.ids = reqRespCapsule.pageHandler.getFullDataIDs(logger);
				if (pt.ids != null && pt.ids.size() > pt.pgSize) {
					pageIDs = pt.getNextIDs();
					createEntry(logger, blModServices.getDBManager(), pt);
				} else {
					pt.isPagingOver = true;
					pageIDs = pt.ids;
				}
			} else {
				getEntry(logger, blModServices.getDBManager(), pt);
				pageIDs = pt.getNextIDs();
			}

			reqRespCapsule.respDTOObject = reqRespCapsule.pageHandler.getDataFromIDs(logger, blModServices, pageIDs);

			String pageToken = pt.serialize(logger, blModServices);
			Utilities.setValueInHeader(reqRespCapsule.respHeaders, FwConstants.GENERAL_CONSTANTS.PAGE_TOKEN, pageToken);
			pt.log(logger);
		}
	}

	private static final String INSERT_ENTRY = "insert into pagination_data (all_ids, api) values (?, ?)";
	private static final String SELECT_ENTRY = "select * from pagination_data where id=?";

	public static void createEntry(BLogger logger, DBManager dbm, PaginationToken pt) throws BExceptions {
		DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
		DBQuery iq = dbQB.setQueryString(INSERT_ENTRY).setBindInputFunction((dbLogger, ps) -> {
			ps.setArray(1, ps.getConnection().createArrayOf("bigint", pt.ids.toArray()));
			ps.setString(2, pt.api);
		}).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
			pt.id = rs.getBigDecimal("id").longValue();
		}).logQuery(true).build();

		try {
			dbm.update(logger, iq);
		} catch (DBException e) {
			logger.error(e);
			throw new BExceptions(FwConstants.PCodes.INTERNAL_ERROR, "Failed to create pagination entry!");
		}
	}

	public static void getEntry(BLogger logger, DBManager dbm, PaginationToken pt) throws BExceptions {
		DBQueryBuilder dbQB = dbm.getDBQueryBuilder();
		DBQuery sq = dbQB.setQueryString(SELECT_ENTRY).setBindInputFunction((dbLogger, ps) -> {
			ps.setBigDecimal(1, new BigDecimal(pt.id));
		}).setReturnKeys().setFetchDataFunction((dbFLogger, rs) -> {
			pt.id = rs.getBigDecimal("id").longValue();
			pt.api = rs.getString("api");
			Array a = rs.getArray("all_ids");
			Long[] ba = (Long[]) a.getArray();
			pt.ids = Arrays.asList(ba);
		}).logQuery(true).build();
		try {
			dbm.select(logger, sq);
		} catch (DBException e) {
			logger.error(e);
			throw new BExceptions(FwConstants.PCodes.INTERNAL_ERROR, "Failed to retrieve pagination entry!");
		}
	}
}
