package pl.szymon.btt_bot.network;

import com.google.gson.*;
import lombok.extern.log4j.Log4j2;
import pl.szymon.btt_bot.structures.*;
import pl.szymon.btt_bot.structures.data.RawLesson;

import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public class ProperTimetableDownloader {
	private final static int year = LocalDateTime.now().getYear();

	public static void get(NetworkContext networkContext, TimetableVersion version, CompleteTimetable.Builder builder) throws IOException, InterruptedException {
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.POST(
						HttpRequest.BodyPublishers.ofString(
						"{\"__args\":[null,{\"year\":" +
								year +
								",\"datefrom\":\"" +
								version.getDateFrom() +
								"\",\"dateto\":\"" +
								version.getDateTo() +
								"\",\"table\":\"classes\",\"id\":\"" +
								builder.getKlasaId() +
								"\",\"showColors\":true,\"showIgroupsInClasses\":false,\"showOrig\":true}],\"__gsh\":\"" +
								networkContext.getGsecHash() +
								"\"}"
						)
				).uri(URI.create(networkContext.getRootUrl() + "timetable/server/currenttt.js?__func=curentttGetData"))
				.build();

		HttpResponse<String> httpResponse = networkContext.getHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

		if(httpResponse.statusCode() != 200) {
			throw new NetworkStatusCodeException(httpResponse.statusCode());
		}

		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
				.registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, typeOfT, context) -> LocalTime.parse(json.getAsString()))
				.create();

		Map<Integer, List<Lesson>>[] tempMap = new HashMap[5];

		for(int i = 0; i < 5; i++)
			tempMap[i] = new HashMap<>();

		try {
			Spliterator<JsonElement> spliterator = JsonParser.parseString(httpResponse.body())
					.getAsJsonObject()
					.getAsJsonObject("r")
					.getAsJsonArray("ttitems")
					.spliterator();

			StreamSupport.stream(spliterator, false)
					.map(jsonElement -> gson.fromJson(jsonElement, RawLesson.class))
					.flatMap(ProperTimetableDownloader::unpack)
					.map(rawLesson -> new Lesson(rawLesson, builder.getLessonTimes().get(rawLesson.getPeriod())))
					.forEach(lesson -> addToMap(tempMap, lesson));

			builder.setLessons(tempMap);
		} catch (Exception e) {
			log.error("Server responded with {}", httpResponse.body());
			throw e;
		}
	}

	private static Stream<RawLesson> unpack(RawLesson input) {
		if(input.getStartTime().until(input.getEndTime(), ChronoUnit.MINUTES) > 45) {
			int n = (int) (input.getStartTime().until(input.getEndTime(), ChronoUnit.MINUTES) / 45);

			Stream.Builder<RawLesson> builder = Stream.builder();

			for(int i = 0; i < n; i++) {
				builder.accept(
						new RawLesson(
								input,
								input.getStartTime().plusMinutes(i * 45),
								input.getEndTime().plusMinutes((i + 1) * 45),
								input.getPeriod() + i)
				);
			}

			return builder.build();
		}

		return Stream.of(input);
	}

	private static void addToMap(Map<Integer, List<Lesson>>[] map, Lesson lesson) {
		map[lesson.getDayOfWeek()].computeIfAbsent(lesson.getPeriod(), k -> new ArrayList<>());
		map[lesson.getDayOfWeek()].get(lesson.getPeriod()).add(lesson);
	}
}
