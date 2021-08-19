package pl.szymon.btt_bot.network;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.NoSuchElementException;

@Getter
@Setter
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NetworkContext {
    private static final String regex = "gsechash=\"";

    final String rootUrl;
    final HttpClient httpClient;

    String gsecHash;

    public void init() throws IOException, InterruptedException, NetworkStatusCodeException {
        HttpResponse<String> httpResponse;

        httpResponse = httpClient.send(HttpRequest.newBuilder().GET().uri(URI.create(rootUrl)).build(), HttpResponse.BodyHandlers.ofString());

        if(httpResponse.statusCode() != 200) {
            throw new NetworkStatusCodeException(httpResponse.statusCode());
        }

        String responseBody = httpResponse.body();

        if(!responseBody.contains(regex)) {
            throw new NoSuchElementException("Could not find regex. Server responded with: " + responseBody);
        }

        setGsecHash(responseBody.split(regex)[1].split("\"")[0]);
    }
}
