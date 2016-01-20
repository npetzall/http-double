package npetzall.http_double.server.service;

import npetzall.http_double.api.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nosse on 2016-01-18.
 */
public class SimpleResponse implements Response {

    private String templateName;
    private Map<String, String> tokenMap = new HashMap<>();

    @Override
    public Response setTemplate(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getTemplateName() {
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
}
