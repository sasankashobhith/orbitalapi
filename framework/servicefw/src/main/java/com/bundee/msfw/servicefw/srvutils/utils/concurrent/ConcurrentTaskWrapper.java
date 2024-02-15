package com.bundee.msfw.servicefw.srvutils.utils.concurrent;

import com.bundee.msfw.defs.*;
import com.bundee.msfw.interfaces.logi.*;
import com.bundee.msfw.interfaces.utili.concurrent.*;
import com.bundee.msfw.servicefw.logger.*;
import com.bundee.msfw.servicefw.srvutils.utils.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ConcurrentTaskWrapper implements ConcurrentTask, Runnable {
    private static AtomicLong taskIdx = new AtomicLong(0);
    private ThreadLocalData callerTLD;
    private ConcurrentTask ctask;
    private BExceptions exceptions = null;
    private Future<?> ftask = null;

    public ConcurrentTaskWrapper(ThreadLocalData callerTLD, ConcurrentTask ctask) {
        this.callerTLD = callerTLD;
        this.ctask = ctask;
    }

    @Override
    public void runConcurrent(BLogger logger) throws BExceptions {
        TLSDataCapsule.makeNewThreadLocalData();
        ThreadLocalData tld = TLSDataCapsule.getCurrentThreadLocalData();
        tld.copy(callerTLD);
        BLogger tLogger = BLoggerFactory.create("ConcurrentTask", taskIdx.incrementAndGet(), ctask.getUniqueTaskID());
        long timeTaken = System.currentTimeMillis();
        tLogger.info("START - ConcurrentTask");
        try {
            ctask.runConcurrent(tLogger);
        } catch (BExceptions ex) {
            exceptions = ex;
        }
        timeTaken = System.currentTimeMillis() - timeTaken;
        tLogger.info("END - ConcurrentTask [" + timeTaken + "]");
    }

    @Override
    public String getUniqueTaskID() {
        return ctask.getUniqueTaskID();
    }

    @Override
    public void run() {
        try {
            runConcurrent(null);
        } catch (BExceptions e) {
        }
    }

    public BExceptions getExceptions() {
        return exceptions;
    }

    public void setFTask(Future<?> ftask) {
        this.ftask = ftask;
    }

    public boolean isDone() {
        return (ftask != null ? ftask.isDone() : true);
    }
}
