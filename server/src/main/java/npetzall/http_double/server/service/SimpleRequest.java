package npetzall.http_double.server.service;

import npetzall.http_double.api.Request;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nosse on 2016-01-18.
 */
public class SimpleRequest implements Request {

    private final long start;

    private String path;
    private Map<String,String> headers = new HashMap<>();
    private InputStream bodyStream;

    public SimpleRequest() {
        start = System.currentTimeMillis();
    }

    public SimpleRequest setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public String getPath() {
        return path;
    }

    public SimpleRequest addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public SimpleRequest setBodyStream(InputStream bodyStream) {
        this.bodyStream = bodyStream;
        return this;
    }

    @Override
    public InputStream getBody() {
        return bodyStream;
    }
}
