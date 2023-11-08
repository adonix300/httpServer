package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private String method;
    private String path;
    private String httpVersion;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    private Map<String, String> queryParams = new HashMap<>();
    private Map<String, String> postParams = new HashMap<>();

    public Request(String requestLine, Map<String, String> headers, String body) {
        String[] parts = requestLine.split(" ");
        this.method = parts[0];
        this.httpVersion = parts[2];
        this.headers = headers;
        this.body = body;

        String str = requestLine.split(" ")[1];

        if (this.method.equals("GET") && requestLine.contains("?")) {
            String[] queryParamsString = str.split("\\?");
            this.path = queryParamsString[0];

            List<NameValuePair> params = URLEncodedUtils.parse(queryParamsString[1], StandardCharsets.UTF_8);
            this.queryParams = params.stream()
                    .collect(Collectors.toMap(
                            NameValuePair::getName,
                            NameValuePair::getValue
                    ));


//            this.queryParams = Arrays.stream(queryParamsString[1].split("&"))
//                    .map(param -> param.split("="))
//                    .collect(Collectors.toMap(
//                            element -> element[0],
//                            element -> element[1]));
        } else {
            this.path = parts[1];
        }

        if (this.method.equals("POST") && headers.get("Content-Type").equals("x-www-form-urlencoded")) {
            String[] postParamsString = str.split("\\?");
            List<NameValuePair> params = URLEncodedUtils.parse(postParamsString[1], StandardCharsets.UTF_8);
            this.postParams = params.stream()
                    .collect(Collectors.toMap(
                            NameValuePair::getName,
                            NameValuePair::getValue));
        }
    }

    public String getMethod() {
        return method;
    }

    public String getQueryParam(String name) {
        return this.queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getPostParam(String name) {
        return this.postParams.get(name);
    }

    public Map<String, String> getPostParams() {
        return postParams;
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