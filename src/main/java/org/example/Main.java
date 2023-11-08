package org.example;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.example.HttpHelper.*;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        server.addHandler("POST", "/urlik", (request, responseStream) -> {
            System.out.println("Нам пришел ПОСТ запрос");
            return200(responseStream);
        });

        server.addHandler("GET", "/file", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                handleReturnFileRequest(request, responseStream, request.getQueryParam("fileName"));
            }
        });


        server.addHandler("GET", "/classic.html", (request, responseStream) -> {
            var filePath = Path.of(".", "public", request.getPath());
            var mimeType = Files.probeContentType(filePath);
            caseClassicHtml(responseStream, filePath, mimeType);
        });

        server.listen(9999);
    }
}