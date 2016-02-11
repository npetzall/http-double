package npetzall.httpdouble.unit.io;

import npetzall.httpdouble.io.ByteBufferInputStream;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteBufferInputStreamTest {

    private ByteBuffer byteBuffer;

    @Test
    public void populateByteBuffer() throws IOException {
        byte[] data = readBytes(new BufferedInputStream(this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml")));
        byteBuffer = ByteBuffer.allocate(data.length);
        byteBuffer.put(data);
        assertThat(byteBuffer.limit()).isEqualTo(data.length);
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int numberOfBytesRead;

        while((numberOfBytesRead = inputStream.read(buff)) != -1) {
            byteArrayOutputStream.write(buff, 0, numberOfBytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Test(dependsOnMethods = "populateByteBuffer")
    public void equalsTheResource() {
        ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
        BufferedInputStream input = new BufferedInputStream(this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml"));
        assertThat(byteBufferInputStream).hasSameContentAs(input);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullConstructor() {
        new ByteBufferInputStream(null);
    }

    @Test(dependsOnMethods = "populateByteBuffer")
    public void readComplete() throws IOException {
        ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream(byteBuffer);
        byte[] data = readBytes(new BufferedInputStream(this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml")));
        byte[] dataFromBuffer = readBytes(byteBufferInputStream);
        assertThat(data).containsExactly(dataFromBuffer);
        assertThat(byteBufferInputStream.read() == -1).isTrue();
    }

}
