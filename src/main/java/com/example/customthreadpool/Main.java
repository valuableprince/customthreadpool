package com.example.customthreadpool;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws InterruptedException {
        // Настройка логирования (пример)
        LOGGER.setLevel(Level.INFO);

        int corePoolSize = 2;
        int maxPoolSize = 4;
        long keepAliveTime = 5;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        int queueSize = 5;
        int minSpareThreads = 1;

        ThreadFactoryImpl threadFactory = new ThreadFactoryImpl("MyPool");
        // БОЛЬШЕ НЕ НУЖЕН: RejectedExecutionHandlerImpl rejectedExecutionHandler = new RejectedExecutionHandlerImpl();

        CustomThreadPool threadPool = new CustomThreadPool(corePoolSize, maxPoolSize, keepAliveTime, timeUnit,
                queueSize, minSpareThreads, threadFactory/*, rejectedExecutionHandler*/);

        // Отправка задач
        for (int i = 0; i < 20; i++) {
            int taskId = i;
            threadPool.execute(() -> {
                try {
                    LOGGER.info("[Task] Task " + taskId + " started by " + Thread.currentThread().getName());
                    Thread.sleep(new Random().nextInt(3000)); // Имитация работы
                    LOGGER.info("[Task] Task " + taskId + " finished by " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warning("[Task] Task " + taskId + " interrupted.");
                }
            });
        }

        Thread.sleep(15000); // Даем время задачам выполниться

        threadPool.shutdown();

        Thread.sleep(5000); // Даем время потокам завершиться
        LOGGER.info("Thread pool shutdown completed.");
    }
}