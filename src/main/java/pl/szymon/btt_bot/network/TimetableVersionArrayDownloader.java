package pl.szymon.btt_bot.network;

import com.google.gson.*;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import pl.szymon.btt_bot.structures.TimetableVersionArray;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Log4j2
public class TimetableVersionArrayDownloader {
	private final static Gson gson = new GsonBuilder()
			.registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
			.create();

	private final static int YEAR = LocalDateTime.now().getYear();

	public static TimetableVersionArray get(NetworkContext context) throws IOException, InterruptedException {
		var req = new HttpPost(context.getRootUrl() + "/timetable/server/ttviewer.js?__func=getTTViewerData");
		req.setEntity(new StringEntity("{\"__args\":[null," + YEAR +"],\"__gsh\":\"" + context.getGsecHash() + "\"}", StandardCharsets.UTF_8));

		CloseableHttpResponse resp;
		String responseBody;
		try (var client = HttpClients.createDefault()) {
			resp = client.execute(req, context.getContext());
			responseBody = new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
		}

		if(resp.getStatusLine().getStatusCode() != 200) throw new NetworkStatusCodeException(resp.getStatusLine().getStatusCode());

		try {
			return gson.fromJson(
					JsonParser.parseString(responseBody)
							.getAsJsonObject()
							.getAsJsonObject("r")
							.getAsJsonObject("regular"),
					TimetableVersionArray.class
			);
		} catch (Exception e) {
			log.error("Server responded with {}", responseBody);
			throw e;
		}
	}
}