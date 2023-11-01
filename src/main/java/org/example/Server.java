package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class Server {
    private static final int THREAD_POOL_SIZE = 64;


    static final Map<String, Handler> handlers = new HashMap<>();
    final static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public void dispatch(String method, String path, Request request, BufferedOutputStream outputStream) {
        final var handler = handlers.get(method + path);
        handler.handle(request, outputStream);
    }

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

    private void handleConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1

            final var requestLine = in.readLine();
            final var headersLine = in.readLine();

            Map<String, String> headers = new HashMap<>();

            Stream.of(headersLine).forEach((line) -> {
                String[] string = line.split(": ");
                headers.put(string[0], string[1]);
                System.out.println(line);
            });

            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            final var method = parts[0];
            final var path = parts[1];
            final var request = new Request(method, headers, in);

            dispatch(method, path, request, out);

            if (!validPaths.contains(path)) {
                sendNotFound(out);
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                caseClassicHtml(out, filePath, mimeType);
                return;
            }

            handleStaticFile(out, filePath, mimeType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendNotFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void caseClassicHtml(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    private void handleStaticFile(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }
}
