package pl.szymon.btt_bot.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pl.szymon.btt_bot.network.*;
import pl.szymon.btt_bot.structures.*;
import pl.szymon.btt_bot.structures.data.Substitution;

import java.io.IOException;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@RequiredArgsConstructor
public class UpdateLessonsCallable implements Callable<CompleteTimetable> {
	private final NetworkContext networkContext = new NetworkContext(
			"https://lo3gdynia.edupage.org/",
			HttpClient.newBuilder().cookieHandler(new CookieManager()).build()
	);

	private final String klassName;

	@Override
	public CompleteTimetable call() throws RuntimeException, IOException, InterruptedException {
		log.info("Starting data update for class: {}", klassName);

		NetworkContextInitializer.getGsecHash(networkContext);

		TimetableVersionArray versionArray = TimetableVersionArrayDownloader.get(networkContext);

		CompleteTimetable.Builder builder = new CompleteTimetable.Builder();

		TimetableVersion timetableVersion = versionArray
				.getTimetables()
				.stream()
				.filter(v -> v.getTt_num() == versionArray.getDefaultNum())
				.findFirst()
				.orElseThrow(() -> new NoSuchElementException("Could not find timetable version with tt_num of: " + versionArray.getDefaultNum()));

		timetableVersion.updateDateTo();

		builder.setDateSince(timetableVersion.getDateFrom());
		builder.setDateTo(timetableVersion.getDateTo());

		log.info("Selected timetable version is: {}", timetableVersion);

		builder.setTeacherNames(TeacherNameDownloader.get(networkContext));

		TypeDeclarationsDownloader.get(networkContext, timetableVersion, builder, klassName);

		ProperTimetableDownloader.get(networkContext, timetableVersion, builder);

		log.info(builder);

		log.info("Patching substitutions");

		int retryCounter = 0;

		for(LocalDate localDate = timetableVersion.getDateFrom(); localDate.isBefore(timetableVersion.getDateTo()); localDate = localDate.plusDays(1)) {
			log.trace("Getting substitutions for {}", localDate);
			AtomicInteger counter = new AtomicInteger(0);

			try {
				List<Substitution> substitutions = SubstitutionsDownloader.get(networkContext, localDate, klassName);

				if(!substitutions.isEmpty()) {
					final LocalDate finalLocalDate = localDate;
					substitutions.forEach(substitution -> {
						String[] pattern = substitution.getWhat().split(":");

						if(pattern.length == 2) {
							String gr = pattern[0];

							for(int i = substitution.getStartPeriod(); i <= substitution.getEndPeriod(); i++) {
								builder.getLessons()[finalLocalDate.getDayOfWeek().ordinal()][i]
										.stream()
										.filter(lesson -> contains(lesson.getGroupNames(), gr))
										.forEach(lesson -> lesson.setInfo(substitution.getInfo()));
								counter.getAndIncrement();
							}
						} else {
							for (int i = substitution.getStartPeriod(); i <= substitution.getEndPeriod(); i++) {
								builder.getLessons()[finalLocalDate.getDayOfWeek().ordinal()][i].forEach(lesson -> lesson.setInfo(substitution.getInfo()));
								counter.getAndIncrement();
							}
						}
					});
				}
			} catch (IOException e) {
				if(retryCounter < 3) {
					log.info("A network exception occurred while trying to download substitutions for {}. Retrying...", localDate);
					retryCounter++;
					localDate = localDate.minusDays(1);
				} else {
					log.warn("Already retired 3 times. Giving up on {}.", localDate);
				}
			} finally {
				log.trace("Patched {} lessons", counter.get());
			}
		}

		CompleteTimetable result = builder.build();
		result.bake();

		log.info("Done!");

		return result;
	}

	private static boolean contains(String[] tab, String key) {
		for(String s: tab)
			if(s.equals(key))
				return true;

		return false;
	}
}
