package server;

import api.API;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


public class WorkThread implements Runnable{
 //   private API api;
    private HttpExchange httpExchange;

    public WorkThread(HttpExchange httpExchange)
    {
        this.httpExchange = httpExchange;
    }


    @Override
    public void run() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        OutputStream outputStream = httpExchange.getResponseBody();
       OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        try {
            String data = "1";
            httpExchange.sendResponseHeaders(200,data.length());
            outputStreamWriter.write(data);
            outputStream.close();
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
