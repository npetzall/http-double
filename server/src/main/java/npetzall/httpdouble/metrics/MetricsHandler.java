package npetzall.httpdouble.metrics;

import com.codahale.metrics.*;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

public class MetricsHandler {

    private static final MetricRegistry metricRegistry = new MetricRegistry();
    private static final Slf4jReporter slf4jReporter;

    static {
        metricRegistry.registerAll(new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metricRegistry.registerAll(new GarbageCollectorMetricSet());
        metricRegistry.registerAll(new MemoryUsageGaugeSet());
        metricRegistry.registerAll(new ThreadStatesGaugeSet());
        slf4jReporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger("npetzall.httpdouble.metrics"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        slf4jReporter.start(1, TimeUnit.MINUTES);
    }

    public static Timer timer(Class clazz, String name) {
        return metricRegistry.timer(name(clazz, name));
    }

    public static Meter meter(Class clazz, String name) {
        return metricRegistry.meter(name(clazz, name));
    }

    public static Counter counter(Class clazz, String name) {
        return metricRegistry.counter(name(clazz, name));
    }
}
