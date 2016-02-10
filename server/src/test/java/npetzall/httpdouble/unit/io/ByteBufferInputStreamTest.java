package npetzall.httpdouble.unit.io;

import npetzall.httpdouble.io.ByteBufferInputStream;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by nosse on 2016-01-19.
 */
public class ByteBufferInputStreamTest {

    private ByteBuffer byteBuffer;
    private ByteBufferInputStream byteBufferInputStream;

    @Test
    public void populateByteBuffer() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (BufferedInputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml"))) {
            byte[] buff = new byte[1024];
            int numberOfBytesRead;

            while((numberOfBytesRead = input.read(buff)) != -1) {
                byteArrayOutputStream.write(buff, 0, numberOfBytesRead);
            }
            byteBuffer = ByteBuffer.allocateDirect(byteArrayOutputStream.size());
            byteBuffer.put(byteArrayOutputStream.toByteArray());
        }
        assertThat(byteBuffer.limit()).isEqualTo(byteArrayOutputStream.size());
    }

    @Test(dependsOnMethods = "populateByteBuffer")
    public void createInputStream() {
        byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
        assertThat(byteBufferInputStream).isNotNull();
    }

    @Test(dependsOnMethods = "createInputStream")
    public void equalsTheResource() {
        BufferedInputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml"));
        assertThat(byteBufferInputStream).hasSameContentAs(input);
    }
}
