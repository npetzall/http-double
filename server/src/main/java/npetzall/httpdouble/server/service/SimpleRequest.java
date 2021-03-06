package npetzall.httpdouble.server.service;

import io.netty.handler.codec.http.QueryStringDecoder;
import npetzall.httpdouble.api.Request;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SimpleRequest implements Request {

    private boolean keepAlive;

    private QueryStringDecoder queryStringDecoder;
    private String method;
    private Map<String,String> headers = new HashMap<>();
    private InputStream bodyStream = null;

    public void shouldKeepAlive(boolean shouldKeepAlive) {
        this.keepAlive = shouldKeepAlive;
    }

    public boolean shouldKeepAlive() {
        return keepAlive;
    }

    public SimpleRequest queryStringDecoder(QueryStringDecoder queryStringDecoder) {
        this.queryStringDecoder = queryStringDecoder;
        return this;
    }

    @Override
    public String path() {
        return queryStringDecoder.path();
    }

    public SimpleRequest method(String method) {
        this.method = method;
        return this;
    }

    @Override
    public String method() {
        return method;
    }

    public SimpleRequest addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    @Override
    public Map<String, String> headers() {
        return headers;
    }


    public SimpleRequest body(InputStream bodyStream) {
        this.bodyStream = bodyStream;
        return this;
    }

    @Override
    public InputStream body() {
        return bodyStream;
    }
}
