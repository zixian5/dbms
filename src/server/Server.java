package server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class Server {

    public static void startServer() throws Exception
    {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8001), 0);
        httpServer.setExecutor(null);
        httpServer.createContext("/dbms",new Handler());
        httpServer.start();
    }

    public static void main(String[] args) throws Exception{
        startServer();
    }
}
