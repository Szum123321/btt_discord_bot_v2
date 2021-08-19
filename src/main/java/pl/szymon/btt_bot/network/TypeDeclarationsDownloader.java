package pl.szymon.btt_bot.network;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import pl.szymon.btt_bot.structures.*;
import pl.szymon.btt_bot.structures.data.*;
import pl.szymon.btt_bot.structures.time.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Log4j2
public class TypeDeclarationsDownloader {
	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, typeOfT, context) -> LocalTime.parse(json.getAsString()))
			.create();

	public static void get(NetworkContext networkContext, TimetableVersion version, CompleteTimetable.Builder builder) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(
						"{\"__args\":[null," +
								version.getYear() +
								",{\"vt_filter\":{\"datefrom\":\"" +
								version.getDateFrom() +
								"\",\"dateto\":\"" +
								version.getDateTo() +
								"\"}},{\"op\":\"fetch\",\"tables\":[],\"columns\":[],\"needed_part\":{\"teachers\":[\"__name\",\"firstname\",\"lastname\",\"short\"],\"classes\":[\"__name\"],\"classrooms\":[\"__name\",\"name\",\"short\"],\"igroups\":[\"__name\"],\"subjects\":[\"__name\",\"name\",\"short\"],\"events\":[\"typ\",\"name\"],\"event_types\":[\"name\"],\"subst_absents\":[\"date\",\"absent_typeid\",\"groupname\"],\"periods\":[\"__name\",\"period\",\"starttime\",\"endtime\"],\"dates\":[\"tt_num\",\"tt_day\"]},\"needed_combos\":{},\"client_filter\":{},\"info_tables\":[],\"info_columns\":[],\"has_columns\":{}}],\"__gsh\":\"" +
								networkContext.getGsecHash() +
								"\"}"
				))
				.uri(URI.create(networkContext.getRootUrl() + "rpr/server/maindbi.js?__func=mainDBIAccessor"))
				.build();

		HttpResponse<String> httpResponse = networkContext.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		if(httpResponse.statusCode() != 200) {
			throw new NetworkStatusCodeException(httpResponse.statusCode());
		}

		NoThrowArrayList<LessonTime> lessonTimeList = new NoThrowArrayList<>();

		String responseBody = httpResponse.body();

		JsonArray rootArray = JsonParser.parseString(responseBody)
				.getAsJsonObject()
				.getAsJsonObject("r")
				.getAsJsonArray("tables");

		rootArray.forEach(jsonElement -> {
			String id = jsonElement.getAsJsonObject().get("id").getAsString();
			JsonArray dataRows = jsonElement.getAsJsonObject().getAsJsonArray("data_rows");

			switch(id) {
				case "classes": {
					dataRows.forEach(jsonElement1 -> builder.addClass(gson.fromJson(jsonElement1, Klasa.class)));
					break;
				}

				case "classrooms": {
					dataRows.forEach(jsonElement1 -> builder.addClassroom(gson.fromJson(jsonElement1, Classroom.class)));
					break;
				}

				case "periods": {
					dataRows.forEach(jsonElement1 -> {
						try {
							lessonTimeList.add(LessonTime.fromPeriod(gson.fromJson(jsonElement1, Period.class)));
						} catch (DateTimeParseException ignored) {} //For some reason timetable sends empty periods with which I now have to deal with!
					});
					break;
				}

				case "subjects": {
					dataRows.forEach(jsonElement1 -> builder.addSubject(gson.fromJson(jsonElement1, Subject.class)));
					break;
				}

				case "teachers": {
					dataRows.forEach(jsonElement1 -> builder.addTeacher(gson.fromJson(jsonElement1, Teacher.class)));
					break;
				}

				default: {
					log.trace("Skipping: {}", id);
					break;
				}
			}
		});

		lessonTimeList.sort(Comparator.comparing(LocalTimeRange::getStart));

		NoThrowArrayList<LessonTime> pauseTimeList = new NoThrowArrayList<>(lessonTimeList.size() - 1);

		for(int i = 0; i < lessonTimeList.size() - 1; i++)
			pauseTimeList.add(LessonTime.newPause(lessonTimeList.get(i).getEnd(), lessonTimeList.get(i + 1).getStart(), i));

		builder.setPauseTimes(pauseTimeList);
		builder.setLessonTimes(lessonTimeList);

		builder.setLessonTimeTree(new PointRangeTimePeriodTree<>(new UnionList<>(lessonTimeList, pauseTimeList)));
	}

	@Value
	public static class Period {
		int id;

		@SerializedName("short")
		String shortName;
		String name;

		String period;

		@SerializedName("starttime")
		LocalTime startTime;

		@SerializedName("endtime")
		LocalTime endTime;
	}
}