package npetzall.http_double.api;

public interface ServiceDouble {
    void configure(ServiceDoubleConfiguration serviceDoubleConfiguration);
    void processRequest(Request request, Response response);
}
