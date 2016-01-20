package npetzall.http_double.example;

import npetzall.http_double.api.Request;
import npetzall.http_double.api.Response;
import npetzall.http_double.api.ServiceDouble;
import npetzall.http_double.api.ServiceDoubleConfiguration;

/**
 * Created by nosse on 2016-01-17.
 */
public class ExampleServiceDouble implements ServiceDouble{

    @Override
    public void configure(ServiceDoubleConfiguration serviceDoubleConfiguration) {
        serviceDoubleConfiguration
            .name("Example")
            .urlPath("/example")
            .addTemplate("reverseText", this.getClass().getResourceAsStream("/templates/reverseText.xml"))
            .addTemplate("toUpperCase", this.getClass().getResourceAsStream("/templates/toUpperCase.xml"))
            .addTemplate("getQuotationResponse", this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml"));
    }

    @Override
    public void processRequest(Request request, Response response) {
        response.templateName("getQuotationResponse").addToken("name","Microsoft");
    }

    private void reverseText(Request request, Response response) {

    }

    private void toUpperCase(Request request, Response response) {

    }
}
