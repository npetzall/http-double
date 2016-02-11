package npetzall.httpdouble.unit.io;

import npetzall.httpdouble.io.CircularIntBuffer;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class CircularIntBufferTest {
    @Test
    public void createCircularIntBuffer() {
        CircularIntBuffer cib = new CircularIntBuffer(1024);
        assertThat(cib).isNotNull();
    }

    @Test
    public void canWriteFullBuffer() {
        CircularIntBuffer cib = new CircularIntBuffer(10);
        for(int i = 0; i < 10; i++) {
            cib.write(i);
        }
    }

    @Test
    public void canNotWriteLongerThanBufferIfNoRead() {
        CircularIntBuffer cib = new CircularIntBuffer(10);
        for(int i = 0; i < 10; i++) {
            if (!cib.write(i)) {
                fail("All writes should be true");
            }
        }
        assertThat(cib.write(10)).isFalse();
    }

    @Test
    public void canWriteLongerIfBufferIsRead() {
        CircularIntBuffer cib = new CircularIntBuffer(10);
        for(int i = 0; i < 10; i++) {
            if (!cib.write(i)) {
                fail("All writes should be true");
            }
        }
        assertThat(cib.available()).isEqualTo(10);
        for(int i = 0; i < 10; i++) {
            assertThat(cib.read()).isEqualTo(i);
        }
        assertThat(cib.available()).isEqualTo(0);
        assertThat(cib.write(10)).isTrue();
        assertThat(cib.available()).isEqualTo(1);
        assertThat(cib.read()).isEqualTo(10);
        assertThat(cib.available()).isEqualTo(0);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void cantWriteToClosedBuffer() {
        CircularIntBuffer cib = new CircularIntBuffer(10);
        cib.close();
        assertThat(cib.read()).isEqualTo(-1);
        cib.write(0);
    }
}
