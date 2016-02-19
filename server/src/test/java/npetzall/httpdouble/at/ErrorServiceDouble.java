package npetzall.httpdouble.at;

import npetzall.httpdouble.api.Request;
import npetzall.httpdouble.api.Response;
import npetzall.httpdouble.api.ServiceDouble;
import npetzall.httpdouble.api.ServiceDoubleConfiguration;

public class ErrorServiceDouble implements ServiceDouble{
    @Override
    public void configure(ServiceDoubleConfiguration serviceDoubleConfiguration) {
        serviceDoubleConfiguration
                .name("ErrorServiceDouble")
                .urlPath("/error");
    }

    @Override
    public void processRequest(Request request, Response response) {
        throw new RuntimeException("Error!");
    }
}
