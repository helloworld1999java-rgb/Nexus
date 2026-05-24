package com.marketplace.util;

import javafx.application.Platform;
import javafx.concurrent.Task;
import java.util.function.Consumer;

public class AsyncTask {

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static <T> void run(ThrowingSupplier<T> background,
                               Consumer<T> onSuccess,
                               Consumer<Exception> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return background.get();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            Platform.runLater(() -> onError.accept(
                ex instanceof Exception ? (Exception) ex : new Exception(ex.getMessage(), ex)
            ));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public static <T> void run(ThrowingSupplier<T> background, Consumer<T> onSuccess) {
        run(background, onSuccess,
            ex -> AlertUtil.showError("Ошибка", ex.getMessage() != null ? ex.getMessage() : ex.toString()));
    }

    public static void run(ThrowingRunnable background, Runnable onSuccess, Consumer<Exception> onError) {
        run(() -> { background.run(); return null; }, v -> onSuccess.run(), onError);
    }

    public static void run(ThrowingRunnable background, Runnable onSuccess) {
        run(background, onSuccess,
            ex -> AlertUtil.showError("Ошибка", ex.getMessage() != null ? ex.getMessage() : ex.toString()));
    }
}
