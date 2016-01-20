package npetzall.http_double.api;

public interface Response {
  public Response setTemplate(String templateKey);
  public Response addToken(String tokenName, String tokenValue);
}
