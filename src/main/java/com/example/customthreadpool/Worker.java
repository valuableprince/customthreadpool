package com.example.customthreadpool;

import java.util.logging.Logger;

public class Worker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    private final CustomThreadPool pool;
    private Thread thread; // Ссылка на поток для прерывания

    public Worker(CustomThreadPool pool) {
        this.pool = pool;
    }

    @Override
    public void run() {
        this.thread = Thread.currentThread(); // Сохраняем ссылку на поток
        try {
            Runnable task;
            while ((task = pool.getTask()) != null) {
                try {
                    LOGGER.info("[Worker] " + Thread.currentThread().getName() + " executes " + task.toString());
                    task.run();
                } catch (Throwable t) {
                    // Логирование ошибок при выполнении задачи
                    LOGGER.severe("[Worker] Exception during task execution: " + t.getMessage());
                }
            }
        } finally {
            pool.removeWorker(this); // Удаляем воркера из списка
            LOGGER.info("[Worker] " + Thread.currentThread().getName() + " terminated.");
        }
    }


    public void interrupt() {
        if (thread != null) {
            thread.interrupt();
        }
    }
}