package npetzall.httpdouble.admin.services;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import npetzall.httpdouble.metrics.MetricsHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MetricsService implements AdminService {

    private ObjectMapper mapper;

    public MetricsService() {
        mapper = new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS,
                TimeUnit.SECONDS,
                false,
                MetricFilter.ALL));
    }

    @Override
    public String getName() {
        return "Metrics";
    }

    @Override
    public String getPath() {
        return "/metrics";
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        String type = getParameter(queryStringDecoder, "type");
        String name = getParameter(queryStringDecoder, "name");

        Object dataAsObject = filter(type, name);
        byte[] dataAsBytes = new byte[0];
        try {
            dataAsBytes = getWriter(queryStringDecoder).writeValueAsBytes(dataAsObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonProcessingException("Failed to write object: " +  dataAsObject.getClass().getCanonicalName() ,e);
        }
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(dataAsBytes));
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, "must-revalidate,no-cache,no-store");
            ctx.writeAndFlush(response);
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);

    }

    private String getParameter(QueryStringDecoder queryStringDecoder, String key) {
        List<String> values = queryStringDecoder.parameters().get(key);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        } else {
            return null;
        }
    }

    private Object filter(String type, String name) {
        MetricRegistry metricRegistry = MetricsHandler.registry();
        if (shouldFilterByName(name)) {
            return metricRegistry.getMetrics().get(name);
        }
        if (shouldFilterByType(type)) {
            filterByType(type);
        }
        return metricRegistry;
    }

    private boolean shouldFilterByName(String name) {
        return name != null && !name.isEmpty();
    }

    private boolean shouldFilterByType(String type) {
        return type != null && !type.isEmpty();
    }

    private Object filterByType(String type) {
        MetricRegistry metricRegistry = MetricsHandler.registry();
        switch (type) {
            case "gauges":
                return metricRegistry.getGauges();
            case "counters":
                return metricRegistry.getCounters();
            case "histograms":
                return metricRegistry.getHistograms();
            case "meters":
                return metricRegistry.getMeters();
            default:
                return metricRegistry;
        }
    }

    private ObjectWriter getWriter(QueryStringDecoder queryStringDecoder) {
        final boolean prettyPrint = queryStringDecoder.parameters().containsKey("pretty");
        if (prettyPrint) {
            return mapper.writerWithDefaultPrettyPrinter();
        }
        return mapper.writer();
    }

    private class RuntimeJsonProcessingException extends RuntimeException {
        public RuntimeJsonProcessingException(String message, Exception exception) {
            super(message, exception);
        }
    }
}
