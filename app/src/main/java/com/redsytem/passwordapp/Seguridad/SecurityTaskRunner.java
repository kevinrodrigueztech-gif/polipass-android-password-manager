package com.redsytem.passwordapp.Seguridad;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Runs deliberately expensive password operations away from Android's UI thread. */
public final class SecurityTaskRunner {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    private SecurityTaskRunner() {
    }

    public static void execute(Runnable task) {
        EXECUTOR.execute(task);
    }
}
