package npetzall.httpdouble.at.admin;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import npetzall.httpdouble.admin.AdminServerInitializer;
import npetzall.httpdouble.admin.registry.AdminRegistry;
import npetzall.httpdouble.admin.services.ListDoubleService;
import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.server.registry.ServiceDoubleRegistry;
import npetzall.httpdouble.server.registry.ServiceLoaderBackedRegistry;
import npetzall.httpdouble.template.OffHeapTemplateRepo;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ListDoubleServicesTest {

    @Test
    public void canListServices() {
        TemplateService templateService = new OffHeapTemplateRepo();
        ServiceDoubleRegistry serviceDoubleRegistry = new ServiceLoaderBackedRegistry(templateService);
        ListDoubleService listDoubleService = new ListDoubleService(serviceDoubleRegistry);
        AdminRegistry.getInstance().register(listDoubleService.getName(),listDoubleService.getPath(), listDoubleService);
        EmbeddedChannel ch = new EmbeddedChannel(new AdminServerInitializer());
        ch.pipeline().remove("responseEncoder");
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/servicedoubles");
        ch.writeInbound(request);

        List<String> serviceNamesInServiceRegistry = serviceDoubleRegistry.getAllServiceDoubles().entrySet().stream().map(entry -> entry.getValue().getName()).collect(Collectors.toList());
        List<String> serviceNamesInResponse = null;

        for(Object obj: ch.outboundMessages()) {
            if(obj instanceof FullHttpResponse) {
                serviceNamesInResponse = findServiceNames(((FullHttpResponse)obj).content().toString(StandardCharsets.UTF_8));
            }
        }
        assertThat(serviceNamesInResponse).hasSameElementsAs(serviceNamesInServiceRegistry);
    }

    private List<String> findServiceNames(String s) {
        ArrayList<String> serviceNames = new ArrayList<>();
        Matcher matcher = Pattern.compile("(>)([a-zA-Z]*)(</a>)").matcher(s);
        while(matcher.find()) {
            serviceNames.add(matcher.group(2));
        }
        return serviceNames;
    }


}
