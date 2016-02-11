package npetzall.httpdouble.unit.io;

import npetzall.httpdouble.io.TokenReplaceStream;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenReplaceStreamTest {

    @Test
    public void tokensAreReplaced() {
        HashMap<String,String> tokens = new HashMap<>();
        tokens.put("name","Npetzall");
        InputStream inputStreamTemplate = this.getClass().getResourceAsStream("/templates/getQuotationResponse.xml");
        TokenReplaceStream tokenReplaceStream = new TokenReplaceStream(tokens, inputStreamTemplate);
        InputStream inputStreamExpected = this.getClass().getResourceAsStream("/expected/getQuotationResponse.xml");
        assertThat(tokenReplaceStream).hasSameContentAs(inputStreamExpected);
    }

    @Test
    public void willWriteDollarAndCurlyBracketIfNotComplete() {
        ByteArrayInputStream input = new ByteArrayInputStream("hej${as".getBytes(StandardCharsets.UTF_8));
        HashMap<String,String> tokens = new HashMap<>();
        tokens.put("name","Npetzall");
        TokenReplaceStream tokenReplaceStream = new TokenReplaceStream(tokens, input);
        assertThat(tokenReplaceStream).hasSameContentAs(new ByteArrayInputStream("hej${as".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void willWriteDollarAndCurlyBracketIfNotCompleteAndLonger() {
        ByteArrayInputStream input = new ByteArrayInputStream("hej${asasdfasdfasdf".getBytes(StandardCharsets.UTF_8));
        HashMap<String,String> tokens = new HashMap<>();
        tokens.put("name","Npetzall");
        TokenReplaceStream tokenReplaceStream = new TokenReplaceStream(tokens, input);
        assertThat(tokenReplaceStream).hasSameContentAs(new ByteArrayInputStream("hej${asasdfasdfasdf".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void willKeepDollarAndCurlyBracketIfTokenDoesntExist() {
        ByteArrayInputStream input = new ByteArrayInputStream("hej${as}".getBytes(StandardCharsets.UTF_8));
        HashMap<String,String> tokens = new HashMap<>();
        tokens.put("sa","Npetzall");
        TokenReplaceStream tokenReplaceStream = new TokenReplaceStream(tokens, input);
        assertThat(tokenReplaceStream).hasSameContentAs(new ByteArrayInputStream("hej${as}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void willKeepDollar() {
        ByteArrayInputStream input = new ByteArrayInputStream("hej$as".getBytes(StandardCharsets.UTF_8));
        HashMap<String,String> tokens = new HashMap<>();
        tokens.put("sa","Npetzall");
        TokenReplaceStream tokenReplaceStream = new TokenReplaceStream(tokens, input);
        assertThat(tokenReplaceStream).hasSameContentAs(new ByteArrayInputStream("hej$as".getBytes(StandardCharsets.UTF_8)));
    }
}
