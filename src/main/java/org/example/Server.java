package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import static org.example.HttpHelper.sendNotFound;

public class Server {
    private static final int THREAD_POOL_SIZE = 64;
    static final Map<String, Handler> handlers = new HashMap<>();
    public void listen(int port) {
        final var threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                var socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.put(method + " " + path, handler);
    }

    private void handleConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();

            System.out.println(requestLine);

            // Parse headers
            Map<String, String> headers = new HashMap<>();
            String line;

            while (!(line = in.readLine()).isEmpty()) {
                int separator = line.indexOf(":");
                if (separator != -1) {
                    headers.put(line.substring(0, separator), line.substring(separator + 1).trim());
                }
                System.out.println(line);
            }

            // If it's a POST request, read the body
            StringBuilder bodyBuilder = new StringBuilder();
            while (in.ready()) {
                bodyBuilder.append((char) in.read());
            }
            String body = bodyBuilder.toString();
            System.out.println(body);

            final var request = new Request(requestLine, headers, body);
            final var handler = handlers.get(request.getMethod() + " " + request.getPath());

            if (handler == null) {
                sendNotFound(out);
            } else {
                handler.handle(request, out);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
