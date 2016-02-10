package npetzall.http_double.server.service;

import npetzall.http_double.api.Request;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SimpleRequest implements Request {

    private boolean keepAlive;

    private String path;
    private Map<String,String> headers = new HashMap<>();
    private InputStream bodyStream;

    public void shouldKeepAlive(boolean shouldKeepAlive) {
        this.keepAlive = shouldKeepAlive;
    }

    public boolean shouldKeepAlive() {
        return keepAlive;
    }

    public SimpleRequest path(String path) {
        this.path = path;
        return this;
    }

    public String path() {
        return path;
    }

    public SimpleRequest addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public SimpleRequest body(InputStream bodyStream) {
        this.bodyStream = bodyStream;
        return this;
    }

    public InputStream body() {
        return bodyStream;
    }
}
