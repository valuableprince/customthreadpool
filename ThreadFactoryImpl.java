package com.example.customthreadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ThreadFactoryImpl implements ThreadFactory {

    private static final Logger LOGGER = Logger.getLogger(ThreadFactoryImpl.class.getName());
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public ThreadFactoryImpl(String poolName) {
        namePrefix = poolName + "-worker-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
        LOGGER.info("[ThreadFactory] Creating new thread: " + thread.getName());
        return thread;
    }
}