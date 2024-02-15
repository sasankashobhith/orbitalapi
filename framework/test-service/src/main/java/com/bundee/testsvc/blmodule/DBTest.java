package com.bundee.testsvc.blmodule;

import java.util.ArrayList;
import java.util.Collection;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.blmodi.*;
import com.bundee.msfw.interfaces.endpoint.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.msfw.interfaces.reqrespi.*;
import com.bundee.msfw.interfaces.restclienti.RESTClient;
import com.bundee.msfw.interfaces.utili.concurrent.ConcurrentTask;
import com.bundee.msfw.interfaces.utili.concurrent.ConcurrentTaskExecutor;
import com.bundee.testsvc.db.*;
import com.bundee.testsvc.defs.*;
import com.bundee.testsvc.pojo.*;

public class DBTest implements BLModule {
	private static final DBTest dbtest = new DBTest();
	private RESTClient selfClient;

	@Override
	public void init(BLogger logger, BLModServices blModServices) throws BExceptions {
		selfClient = blModServices.getRESTClientFactory().getNewRESTClient(logger, "self-client", "self-client",
				blModServices);
		logger.event("test event");
		String hp = "localhost:" + blModServices.getFileCfgHandler().getApplication().getThisServicePort(); 
		selfClient.setStandardHealthCheck(hp);
	}

	@BEndpoint(uri = TestDefs.Endpoints.TEST_DB, httpMethod = UniversalConstants.POST, permission = UniversalConstants.SPECIAL_NO_VALIDATE_PERMISSION, reqDTOClass = DBTestDTO.class)
	public BaseResponse dbTest(BLogger logger, BLModServices blModServices, RequestContext reqCtx, DBTestDTO dbTestDTO)
			throws BExceptions {
		if (dbTestDTO == null || dbTestDTO.getTasks() == null || dbTestDTO.getTasks().isEmpty()) {
			throw new BExceptions(UniversalConstants.PCodes.INTERNAL_ERROR, "DBTestDTO is not valid!");
		}

		int replication = (dbTestDTO.getReplication() == null || dbTestDTO.getReplication() <= 0 ? 1
				: dbTestDTO.getReplication());

		ConcurrentTaskExecutor cte = blModServices.getUtilFactory().getConcurrentTaskExecutor();
		Collection<ConcurrentTask> tasks = new ArrayList<ConcurrentTask>();

		int idx = 1;
		for (DBTaskDTO dbt : dbTestDTO.getTasks()) {
			for (int rid = 0; rid < replication; rid++) {
				DBTaskDTO dbtn = DBTaskDTO.copy(dbt);
				dbtn.initValues(idx++);
				ConcurrentTask ct = dbtest.new DBTestConcurrentTask(blModServices, dbtn);
				tasks.add(ct);
			}
		}
		cte.execute(logger, tasks);
		return null;
	}

	@BEndpoint(uri = TestDefs.Endpoints.SELF_TEST, httpMethod = UniversalConstants.GET, permission = UniversalConstants.SPECIAL_NO_VALIDATE_PERMISSION)
	public BaseResponse selfTest(BLogger logger, BLModServices blModServices, RequestContext reqCtx) {
		SelfTestResponse str = new SelfTestResponse(); 
		HealthDetails hd = selfClient.checkHealth(logger);
		str.setHealthDetails(hd);
		return str;
	}

	class DBTestConcurrentTask implements ConcurrentTask {
		BLModServices blModServices;
		DBTaskDTO dbt;

		DBTestConcurrentTask(BLModServices blModServices, DBTaskDTO dbt) {
			this.blModServices = blModServices;
			this.dbt = dbt;
		}

		@Override
		public String getUniqueTaskID() {
			return dbt.getID();
		}

		@Override
		public void runConcurrent(BLogger logger) throws BExceptions {
			BExceptions execs = new BExceptions();
			int idx = 0;
			for (; idx < dbt.getUpdLoop(); idx++) {
				TestDBDAO.update(logger, blModServices.getDBManager(), dbt.getUpdateQs(), execs);
			}
			for (idx = 0; idx < dbt.getSelLoop(); idx++) {
				TestDBDAO.select(logger, blModServices.getDBManager(), dbt.getSelectQs(), execs);
			}

			try {
				Thread.sleep(dbt.getSleepMS());
			} catch (InterruptedException e) {
			}

			if (execs.hasExceptions())
				throw execs;
		}
	}
}
