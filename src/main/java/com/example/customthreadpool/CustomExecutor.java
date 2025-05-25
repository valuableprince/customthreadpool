package com.example.customthreadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Executor;

public interface CustomExecutor extends Executor {
    void execute(Runnable command);
    <T> Future<T> submit(Callable<T> task);
    void shutdown();
    void shutdownNow();
}