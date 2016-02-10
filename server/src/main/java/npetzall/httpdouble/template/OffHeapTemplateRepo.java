package npetzall.httpdouble.template;

import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.io.ByteBufferInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class OffHeapTemplateRepo implements TemplateService {

    private ConcurrentHashMap<String, ByteBuffer> templates = new ConcurrentHashMap<>();

    @Override
    public void put(String serviceDoubleName, String templateName, InputStream inputstream) {
        try {
            byte[] template = read(inputstream);
            ByteBuffer byteBuffer  = createByteBuffer(template);
            templates.put(asKey(serviceDoubleName, templateName), byteBuffer);
        } catch (Exception e) {
            throw new OffHeapTemplateRepoException("Failed to store template in OffHeapTemplateRepo",e);
        }
    }

    private byte[] read(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] readBuffer = new byte[1024];
        int numberOfReadBytes;
        while((numberOfReadBytes = bufferedInputStream.read(readBuffer)) != -1) {
            byteArrayOutputStream.write(readBuffer,0, numberOfReadBytes);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private ByteBuffer createByteBuffer(byte[] template) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(template.length);
        byteBuffer.put(template);
        return byteBuffer.asReadOnlyBuffer();
    }

    private String asKey(String serviceDoubleName, String templateName) {
        return serviceDoubleName + "." + templateName;
    }

    @Override
    public InputStream get(String serviceDoubleName, String templateName) {
        return new ByteBufferInputStream(templates.get(asKey(serviceDoubleName, templateName)));
    }
}
