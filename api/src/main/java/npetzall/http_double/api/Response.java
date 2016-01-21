package npetzall.http_double.api;

public interface Response {
    public Response templateName(String templateKey);
    public Response addToken(String tokenName, String tokenValue);
    public Response contentType(String contentType);
    public Response sendChunked(boolean shouldSendChunked);
    public Response delay(long delay);
    public Response delay(long min, long max);
}
