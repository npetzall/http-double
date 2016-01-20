package npetzall.http_double.api;

public interface ServiceDouble {
    public void configure(ServiceDoubleConfiguration serviceDoubleConfiguration);
    public void processRequest(Request request, Response response);
}
