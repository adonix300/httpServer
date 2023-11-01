package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private String method;
    private String path;
    private String httpVersion;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    private Map<String, String> queryParams = new HashMap<>();

    public Request(String requestLine, Map<String, String> headers, String body) {
        String[] parts = requestLine.split(" ");
        this.method = parts[0];
        this.httpVersion = parts[2];
        this.headers = headers;
        this.body = body;

        String str = requestLine.split(" ")[1];

        if (parts[0].equals("GET")&&requestLine.contains("?")) {
            String[] queryParamsString = str.split("\\?");
            this.path = queryParamsString[0];

            this.queryParams = Arrays.stream(queryParamsString[1].split("&"))
                    .map(param -> param.split("="))
                    .collect(Collectors.toMap(
                            element -> element[0],
                            element -> element[1]));
        } else {
            this.path = parts[1];
        }
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getBody() {
        return body;
    }

}
