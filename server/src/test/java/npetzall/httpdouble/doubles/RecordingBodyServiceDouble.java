package npetzall.httpdouble.doubles;

import npetzall.httpdouble.api.Request;
import npetzall.httpdouble.api.Response;
import npetzall.httpdouble.api.ServiceDouble;
import npetzall.httpdouble.api.ServiceDoubleConfiguration;

import java.io.InputStreamReader;

public class RecordingBodyServiceDouble implements ServiceDouble {

    private StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void configure(ServiceDoubleConfiguration serviceDoubleConfiguration) {
        //nothing don't care
    }

    @Override
    public void processRequest(Request request, Response response) {
        InputStreamReader inputStreamReader = new InputStreamReader(request.body());
        char[] buffer = new char[1024];
        int numberRead = 0;
        try {
            while ((numberRead = inputStreamReader.read(buffer)) > 0) {
                stringBuilder.append(buffer, 0, numberRead);
            }
            inputStreamReader.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public String getRequestBody() {
        return stringBuilder.toString();
    }
}
