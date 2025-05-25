package com.example.customthreadpool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

    private static final Logger LOGGER = Logger.getLogger(RejectedExecutionHandlerImpl.class.getName());

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
         LOGGER.severe("[Rejected] Task " + r.toString() + " was rejected due to overload!")

    }

    public void rejectedExecution(Runnable r, CustomThreadPool executor) {
        LOGGER.severe("[Rejected] Task " + r.toString() + " was rejected due to overload!");
    }
}
