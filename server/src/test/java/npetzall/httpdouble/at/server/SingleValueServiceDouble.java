package npetzall.httpdouble.at.server;

import npetzall.httpdouble.api.Request;
import npetzall.httpdouble.api.Response;
import npetzall.httpdouble.api.ServiceDouble;
import npetzall.httpdouble.api.ServiceDoubleConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SingleValueServiceDouble implements ServiceDouble {

    @Override
    public void configure(ServiceDoubleConfiguration serviceDoubleConfiguration) {
        serviceDoubleConfiguration
                .name("SingleValue")
                .urlPath("/singleValue")
                .addTemplate("getQuotationResponse", this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml"));
    }

    @Override
    public void processRequest(Request request, Response response) {
        response
                .addToken("name", readInputStreamToString(request.body()))
                .contentType("text/xml")
                .templateName("getQuotationResponse");
        if("POST".equals(request.method())) {
            response.sendChunked(true);
        }

        if ("PUT".equals(request.method())) {
            response.sendChunked(false);
        }
    }

    private String readInputStreamToString(InputStream inputStream) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[1024];
        int numberRead = 0;
        try {
            while ((numberRead = inputStreamReader.read(buffer)) > 0) {
                stringBuilder.append(buffer, 0, numberRead);
            }
            inputStreamReader.close();
        } catch (Exception e) {
            try {
                inputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.err.println(e.getMessage());
        }
        return stringBuilder.toString();
    }
}
