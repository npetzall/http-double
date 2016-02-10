package npetzall.httpdouble.unit.template;

import npetzall.httpdouble.api.TemplateService;
import npetzall.httpdouble.template.OffHeapTemplateRepo;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by nosse on 2016-01-19.
 */
public class OffHeapTemplateRepoTest {

    @Test
    public void canCreate() {
        TemplateService templateService = new OffHeapTemplateRepo();
        assertThat(templateService).isNotNull();
    }

    @Test
    public void storeAndRetrieve() {
        TemplateService templateService = new OffHeapTemplateRepo();

        InputStream toSave = this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml");
        InputStream expected = this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml");

        templateService.put("UnitTest","getQuotationResponse", toSave);

        InputStream retrived = templateService.get("UnitTest","getQuotationResponse");

        assertThat(retrived).hasSameContentAs(expected);

    }
}
