package com.example.customthreadpool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

    private static final Logger LOGGER = Logger.getLogger(RejectedExecutionHandlerImpl.class.getName());

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
         LOGGER.severe("[Rejected] Task " + r.toString() + " was rejected due to overload!");
        // Можно реализовать разные стратегии:
        // 1. Отклонение задачи (как сейчас)
        // 2. Выполнение задачи в текущем потоке: r.run();
        // 3. Сброс самой старой задачи из очереди и добавление новой
        // 4. Реализация backpressure (например, с использованием Reactive Streams)

        // Выбор стратегии зависит от требований вашего приложения.  Простое отклонение может привести к потере данных,
        // выполнение в текущем потоке замедлит обработку текущего запроса, а сброс задач может быть неприемлем, если важна
        // обработка всех задач. Backpressure – наиболее сложный вариант, но и самый гибкий.
    }

    public void rejectedExecution(Runnable r, CustomThreadPool executor) {
        LOGGER.severe("[Rejected] Task " + r.toString() + " was rejected due to overload!");
    }
}