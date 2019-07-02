package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Handler implements HttpHandler {

    private ExecutorService executor;

    public Handler()
    {
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        executor.execute(new WorkThread(httpExchange));
    }
}
