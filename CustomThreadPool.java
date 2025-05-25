package com.example.customthreadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class CustomThreadPool implements CustomExecutor {

    private static final Logger LOGGER = Logger.getLogger(CustomThreadPool.class.getName());

    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;
    private final ThreadFactory threadFactory;
    private final BlockingQueue<Runnable> taskQueue;
    private final List<Worker> workers = new ArrayList<>();
    private final AtomicInteger workerCount = new AtomicInteger(0);
    private volatile boolean isShutdown = false;

    public CustomThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit,
                            int queueSize, int minSpareThreads, ThreadFactory threadFactory) {
        if (corePoolSize < 0 || maxPoolSize <= 0 || maxPoolSize < corePoolSize || keepAliveTime < 0 ||
                queueSize < 0 || minSpareThreads < 0 || minSpareThreads > corePoolSize) {
            throw new IllegalArgumentException("Invalid thread pool parameters");
        }

        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;
        this.threadFactory = threadFactory;
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);

        // Инициализация начальных потоков
        for (int i = 0; i < corePoolSize; i++) {
            addWorker();
        }
    }

    private void addWorker() {
        if (isShutdown) {
            return;
        }

        Worker worker = new Worker(this);
        Thread thread = threadFactory.newThread(worker);

        if (thread != null) {
            workers.add(worker);
            workerCount.incrementAndGet();
            thread.start();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException("Command cannot be null");
        }

        if (isShutdown) {
            // ЛОГИКА ОТКЛОНЕНИЯ ЗДЕСЬ:
            LOGGER.severe("[Rejected] Task " + command.toString() + " was rejected due to shutdown!");
            // Опционально: throw new RejectedExecutionException("Задача отклонена из-за остановки");
            return;
        }

        try {
            if (!taskQueue.offer(command, 100, TimeUnit.MILLISECONDS)) { // Добавлено ожидание
                // ЛОГИКА ОТКЛОНЕНИЯ ЗДЕСЬ:
                LOGGER.severe("[Rejected] Task " + command.toString() + " was rejected due to queue overload!");
                // Опционально: command.run(); // Выполнить в текущем потоке (НЕ рекомендуется для высокой нагрузки)
                ensureMinimumSpareThreads(); // Проверяем после отклонения
            } else {
                LOGGER.info("[Pool] Task accepted into queue: " + command.toString());
                ensureMinimumSpareThreads(); // Проверяем после добавления
            }
        } catch (RejectedExecutionException | InterruptedException e) {
            // ЛОГИКА ОТКЛОНЕНИЯ ЗДЕСЬ:
            LOGGER.severe("[Rejected] Task " + command.toString() + " was rejected due to interruption!");
            ensureMinimumSpareThreads(); // Проверяем после отклонения
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    private void ensureMinimumSpareThreads() {
        if (workerCount.get() < minSpareThreads) {
            synchronized (workers) {
                while (workerCount.get() < minSpareThreads && workerCount.get() < maxPoolSize && !isShutdown) {
                    addWorker();
                }
            }
        }
    }

    public Runnable getTask() {
        while (!isShutdown) {
            try {
                Runnable task = taskQueue.poll(keepAliveTime, timeUnit);
                if (task != null) {
                    return task;
                } else {
                    // Idle timeout
                    synchronized (workers) {
                        if (workerCount.get() > corePoolSize) {
                            workerCount.decrementAndGet();
                            return null; // Worker will terminate
                        }
                    }
                }
            } catch (InterruptedException e) {
                // Thread interrupted, retry
            }
        }
        return null; // Shutdown requested
    }


    @Override
    public void shutdown() {
        isShutdown = true;
        synchronized (workers) {
            for (Worker worker : workers) {
                worker.interrupt(); // Прерываем потоки, чтобы они завершили работу
            }
        }
    }

    @Override
    public void shutdownNow() {
        isShutdown = true;
        synchronized (workers) {
            for (Worker worker : workers) {
                worker.interrupt();
            }
            taskQueue.clear(); // Очищаем очередь
        }
    }


    // Геттеры для Worker
    public int getCorePoolSize() { return corePoolSize; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public long getKeepAliveTime() { return keepAliveTime; }
    public TimeUnit getTimeUnit() { return timeUnit; }
    public boolean isShutdown() { return isShutdown; }

    // Метод для удаления завершенного воркера
    public void removeWorker(Worker worker) {
        synchronized (workers) {
            workers.remove(worker);
            workerCount.decrementAndGet();
        }
    }
}