package npetzall.httpdouble.unit.io;

import npetzall.httpdouble.io.TokenReplaceStream;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by nosse on 2016-01-19.
 */
public class TokenReplaceStreamTest {

    private TokenReplaceStream tokenReplaceStream;

    @Test
    public void canCreate() throws IOException {
        HashMap<String,String> tokens = new HashMap<>();
        tokens.put("name","Npetzall");
        InputStream inputStream = this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml");
        tokenReplaceStream = new TokenReplaceStream(tokens, inputStream);
        assertThat(tokenReplaceStream).isNotNull();
    }

    @Test(dependsOnMethods = "canCreate")
    public void tokensAreReplaced() {
        InputStream inputStream = this.getClass().getResourceAsStream("/expected/getQuotationResponse.xml");
        assertThat(tokenReplaceStream).hasSameContentAs(inputStream);
    }
}
