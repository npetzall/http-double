package npetzall.httpdouble.unit.server.service;

import npetzall.httpdouble.server.service.SimpleResponse;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by nosse on 2016-01-21.
 */
public class SimpleResponseTest {

    //TODO: Replace with ticker/clock
    @Test
    public void delayTest() {
        SimpleResponse response = new SimpleResponse();
        long start = response.startTime();
        response.delay(100,200);
        long timer1 = System.currentTimeMillis() - start;
        assertThat(response.delay()).isBetween(100 - timer1 - 1, 200 -timer1 + 1);
        long timer2 = System.currentTimeMillis() - start;
        assertThat(response.delay()).isBetween(100 - timer1 - 1, 200 -timer1 + 1);
        do{
            Thread.yield();
            Math.random();
        }while((System.currentTimeMillis() - start) < 200);
        assertThat(response.delay()).isEqualTo(0);
    }
}
