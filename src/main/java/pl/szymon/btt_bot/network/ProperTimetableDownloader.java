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
	public static void get(NetworkContext networkContext, TimetableVersion version, CompleteTimetable.Builder builder) throws IOException, InterruptedException {
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.POST(
						HttpRequest.BodyPublishers.ofString(
						"{\"__args\":[null,{\"year\":" +
								version.getYear() +
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

		@SuppressWarnings("unchecked")
		Map<Integer, List<Lesson>>[] tempMap = new HashMap[5];

		List<Lesson>[][] tempArray = new ArrayList[5][15];


		for(int i = 0; i < 5; i++) {
			for (int j = 0; j < 15; j++) {
				tempArray[i][j] = new ArrayList<>();
			}
		}

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
					.forEach(lesson -> addToList(tempArray, lesson));
					//.forEach(lesson -> addToMap(tempMap, lesson));

			builder.setLessons(tempArray);
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

	private static void addToList(List<Lesson>[][] list, Lesson lesson) {
		list[lesson.getDayOfWeek()][lesson.getPeriod()].add(lesson);
	}
}
