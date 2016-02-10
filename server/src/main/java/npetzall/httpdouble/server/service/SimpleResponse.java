package npetzall.httpdouble.server.service;

import io.netty.util.AsciiString;
import npetzall.httpdouble.api.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleResponse implements Response {

    private final long startTime;

    private String templateName;
    private Map<String, String> tokenMap = new HashMap<>();
    private AsciiString contentType;
    private boolean sendChunkedResponse = false;
    private long delayMin = 0;
    private long delayMax = 1;

    public SimpleResponse() {
        startTime = System.currentTimeMillis();
    }

    public long startTime() {
        return startTime;
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

    @Override
    public Response addTokens(Map<String,String> tokens) {
        tokenMap.putAll(tokens);
        return this;
    }

    public Map<String,String> getTokens() {
        return tokenMap;
    }

    @Override
    public Response contentType(String contentType) {
        this.contentType = new AsciiString(contentType);
        return this;
    }

    public AsciiString contentType() {
        return contentType;
    }

    @Override
    public Response sendChunked(boolean shouldSendChunked) {
        sendChunkedResponse = shouldSendChunked;
        return this;
    }

    public boolean sendChunkedResponse() {
        return sendChunkedResponse;
    }

    @Override
    public Response delay(long delay) {
        delayMin = delay;
        delayMax = delay+1;
        return this;
    }

    @Override
    public Response delay(long min, long max) {
        delayMin = min;
        delayMax = max+1;
        return this;
    }

    public long delay() {
        return Math.max(0L, ThreadLocalRandom.current().nextLong(delayMin,delayMax) - (System.currentTimeMillis() - startTime));
    }
}
