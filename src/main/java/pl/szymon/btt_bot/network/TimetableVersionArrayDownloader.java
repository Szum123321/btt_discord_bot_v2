package pl.szymon.btt_bot.network;

import com.google.gson.*;
import lombok.extern.log4j.Log4j2;
import pl.szymon.btt_bot.structures.TimetableVersionArray;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Log4j2
public class TimetableVersionArrayDownloader {
	private final static Gson gson = new GsonBuilder()
			.registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
			.create();

	private final static int YEAR = LocalDateTime.now().getYear();

	public static TimetableVersionArray get(NetworkContext context) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString("{\"__args\":[null," + YEAR +"],\"__gsh\":\"" + context.getGsecHash() + "\"}"))
				.uri(URI.create(context.getRootUrl() + "timetable/server/ttviewer.js?__func=getTTViewerData"))
				.build();

		HttpResponse<String> httpResponse = context.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		if(httpResponse.statusCode() != 200) {
			throw new NetworkStatusCodeException(httpResponse.statusCode());
		}

		try {
			return gson.fromJson(
					JsonParser.parseString(httpResponse.body())
							.getAsJsonObject()
							.getAsJsonObject("r")
							.getAsJsonObject("regular"),
					TimetableVersionArray.class
			);
		} catch (Exception e) {
			log.error("Server responded with {}", httpResponse.body());
			throw e;
		}
	}
}