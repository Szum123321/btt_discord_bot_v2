package pl.szymon.btt_bot.network;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class NetworkContext {
    private static final String regex = "gsechash=\"";
    private static final Pattern CSRF_PATTERN = Pattern.compile("<input[ a-z\\\"=]*name=\\\"csrfauth\\\"[ a-z\\\"=]*value=\\\"(?<token>[A-Za-z0-9]{10,})");

    final URI rootUrl;

    final HttpClientContext context = HttpClientContext.create();

    String gsecHash;
    String csrfToken;

    public void init() throws IOException, NetworkStatusCodeException {
        context.setCookieStore(new BasicCookieStore());
        String responseBody = GET(rootUrl);
        Matcher match =  CSRF_PATTERN.matcher(responseBody);
        if(!match.find()) throw new NoSuchElementException("Could not find csrf token. Server responded with: " + responseBody);

        csrfToken = match.group("token");
    }

    public String GET(URI url) throws IOException {
        var req = new HttpGet(url);
        CloseableHttpResponse resp;
        try (var client = HttpClients.createDefault()) {
            resp = client.execute(req, context);
        }

        if(resp.getStatusLine().getStatusCode() != 200) throw new NetworkStatusCodeException(resp.getStatusLine().getStatusCode());

        return new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);//httpResponse.body();
    }

    public String update_gsec(String url) throws IOException {
        String responseBody = GET(rootUrl.resolve(url));

        if(!responseBody.contains(regex)) throw new NoSuchElementException("Could not find gsec hash. Server responded with: " + responseBody);

        setGsecHash(responseBody.split(regex)[1].split("\"")[0]);

        return responseBody;
    }

    private void printCookies() {
        context.getCookieStore().getCookies().forEach(k -> log.info("{}", k));
    }

    public void log_in(String login, String password) throws IOException {
        var req = new HttpPost(rootUrl.resolve("login/edubarLogin.php"));
        List<NameValuePair> params = new ArrayList<>(3);
        params.add(new BasicNameValuePair("csrfauth", csrfToken));
        params.add(new BasicNameValuePair("username", login));
        params.add(new BasicNameValuePair("password", password));
        req.setEntity(new UrlEncodedFormEntity(params));

        CloseableHttpResponse resp;
        try (var client = HttpClients.createDefault()) {
            resp = client.execute(req, context);
        }

        if(resp.getStatusLine().getStatusCode() != 302) throw new NetworkStatusCodeException(resp.getStatusLine().getStatusCode());
    }
}
