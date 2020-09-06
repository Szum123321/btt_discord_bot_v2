package pl.szymon.btt_bot.network;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.NoSuchElementException;

@Log4j2
public class NetworkContextInitializer {
	private static final String regex = "gsechash=\"";

	public static void getGsecHash(NetworkContext context) throws IOException, InterruptedException, NetworkStatusCodeException {
		HttpResponse<String> httpResponse;

		httpResponse = context.getHttpClient().send(HttpRequest.newBuilder().GET().uri(URI.create(context.getRootUrl())).build(), HttpResponse.BodyHandlers.ofString());

		if(httpResponse.statusCode() != 200) {
			throw new NetworkStatusCodeException(httpResponse.statusCode());
		}

		String responseBody = httpResponse.body();

		if(!responseBody.contains(regex)) {
			throw new NoSuchElementException("Could not find regex. Server responded with: " + responseBody);
		}

		try {
			context.setGsecHash(responseBody.split(regex)[1].split("\"")[0]);
		} catch (Exception e) {
			log.error("Server responded with {}", responseBody);
			throw e;
		}
	}
}
