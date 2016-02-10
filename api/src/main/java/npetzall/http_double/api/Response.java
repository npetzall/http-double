package npetzall.http_double.api;

public interface Response {
    Response templateName(String templateKey);
    Response addToken(String tokenName, String tokenValue);
    Response contentType(String contentType);
    Response sendChunked(boolean shouldSendChunked);
    Response delay(long delay);
    Response delay(long min, long max);
}
