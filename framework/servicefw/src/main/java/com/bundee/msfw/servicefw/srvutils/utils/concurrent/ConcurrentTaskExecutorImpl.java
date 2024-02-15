package com.bundee.msfw.servicefw.srvutils.utils.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.utili.concurrent.ConcurrentTask;
import com.bundee.msfw.interfaces.utili.concurrent.ConcurrentTaskExecutor;
import com.bundee.msfw.servicefw.srvutils.utils.TLSDataCapsule;
import com.bundee.msfw.servicefw.srvutils.utils.ThreadLocalData;

public class ConcurrentTaskExecutorImpl implements ConcurrentTaskExecutor {
	ExecutorService concurrentExecutor;
	long sleepMS;
	
	public ConcurrentTaskExecutorImpl(long sleepMS) {
		this.sleepMS = sleepMS;
		concurrentExecutor = Executors.newCachedThreadPool();
	}

	@Override
	public void execute(BLogger logger, Collection<ConcurrentTask> tasks) throws BExceptions {
		ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();

		List<ConcurrentTaskWrapper> submittedTasks = new ArrayList<ConcurrentTaskWrapper>();

		for (ConcurrentTask ctask : tasks) {
			ConcurrentTaskWrapper ctw = new ConcurrentTaskWrapper(tld, ctask);
			Future<?> ftask = concurrentExecutor.submit(ctw);
			ctw.setFTask(ftask);
			submittedTasks.add(ctw);
		}

		BExceptions taskExceptions = new BExceptions();
		while (!submittedTasks.isEmpty()) {
			final Iterator<ConcurrentTaskWrapper> itr = submittedTasks.iterator();
			while (itr.hasNext()) {
				ConcurrentTaskWrapper ctw = itr.next();
				if (ctw.isDone()) {
					itr.remove();
					if (ctw.getExceptions() != null) {
						taskExceptions.add(ctw.getExceptions());
					}
				}
			}
			try {
				Thread.sleep(sleepMS);
			} catch (InterruptedException e) {
			}
		}
		
		if(taskExceptions.hasExceptions()) {
			throw taskExceptions;
		}
	}
}
