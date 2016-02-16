package npetzall.httpdouble.doubles;

import npetzall.httpdouble.api.TemplateService;

import java.io.InputStream;

public class TemplateServiceDouble implements TemplateService {

    private InputStream inputStream;

    @Override
    public void put(String serviceDoubleName, String templateName, InputStream inputstream) {
        this.inputStream = inputstream;
    }

    @Override
    public InputStream get(String serviceDoubleName, String templateName) {
        return inputStream;
    }
}
