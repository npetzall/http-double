package npetzall.http_double.server.service;

import npetzall.http_double.api.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nosse on 2016-01-18.
 */
public class SimpleResponse implements Response {

    private final long start;

    private String templateName;
    private Map<String, String> tokenMap = new HashMap<>();
    private long delayMin = 0;
    private long delayMax = 0;
    private boolean sendChunkedResponse = false;

    public SimpleResponse() {
        start = System.currentTimeMillis();
    }

    @Override
    public Response templateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String templateName() {
        return templateName;
    }

    @Override
    public Response addToken(String tokenName, String tokenValue) {
        tokenMap.put(tokenName, tokenValue);
        return this;
    }

    public Map<String,String> getTokens() {
        return tokenMap;
    }

    @Override
    public Response delay(long delay) {
        delayMin = delay;
        delayMax = delay;
        return this;
    }

    @Override
    public Response delay(long min, long max) {
        delayMin = min;
        delayMax = max;
        return this;
    }

    public long delayMin() {
        return delayMin;
    }

    public long delayMax() {
        return delayMax;
    }

    @Override
    public Response sendChunked(boolean shouldSendChunked) {
        sendChunkedResponse = shouldSendChunked;
        return this;
    }

    public boolean sendChunkedResponse() {
        return sendChunkedResponse;
    }
}
