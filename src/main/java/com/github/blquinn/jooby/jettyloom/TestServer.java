package com.github.blquinn.jooby.jettyloom;

import io.jooby.Jooby;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestServer extends Jooby {
    {
        get("/", (ctx -> {
            try (ExecutorService e = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 1; i <= 50; i++) {
                    int ii = i;
                    e.submit(() -> {
                        try {
                            Thread.sleep(ii);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(ex);
                        }
                    });
                }
            }

            return ctx.send("Hello world");
        }));
    }

    public static void main(String[] args) {
        Jooby.runApp(args, TestServer.class);
    }
}
