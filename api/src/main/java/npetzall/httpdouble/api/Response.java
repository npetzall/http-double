package npetzall.httpdouble.api;

import java.util.Map;

public interface Response {
    Response templateName(String templateKey);
    Response addToken(String tokenName, String tokenValue);
    Response addTokens(Map<String,String> tokens);
    Response contentType(String contentType);
    Response sendChunked(boolean shouldSendChunked);
    Response delay(long delay);
    Response delay(long min, long max);
}
