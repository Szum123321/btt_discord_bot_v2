package pl.szymon.btt_bot.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import pl.szymon.btt_bot.network.*;
import pl.szymon.btt_bot.structures.*;
import pl.szymon.btt_bot.structures.data.Substitution;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@RequiredArgsConstructor
public class UpdateLessonsCallable implements Callable<CompleteTimetable> {
	private final NetworkContext networkContext = new NetworkContext (
			"https://lo3gdynia.edupage.org/"
	);

	private final String klassName;
	private final String login, password;
	private final LocalDate date;

	@Override
	public CompleteTimetable call() throws RuntimeException, IOException, InterruptedException {
		log.info("Starting data update for class: {}", klassName);

		networkContext.init();
		networkContext.update_gsec(networkContext.getRootUrl());
		if(Objects.nonNull(password) && Objects.nonNull(login)) networkContext.log_in(login, password);

		networkContext.update_gsec("https://lo3gdynia.edupage.org/timetable/");

		CompleteTimetable.Builder builder = new CompleteTimetable.Builder();

		var mon = date.minusDays(date.getDayOfWeek().getValue() - 1);
		var sun = mon.plusDays(7);

		builder.setDateSince(mon);
		builder.setDateTo(sun);

		TimetableVersion ver = new TimetableVersion(-1, 2022, "", false, mon, sun);

		TypeDeclarationsDownloader.get(networkContext, ver, builder);

		builder.setKlasaId(builder.getClasses().values().stream().filter(k -> k.getName().equals(klassName)).findFirst().orElseThrow().getId());

		ProperTimetableDownloader.get(networkContext, ver, builder);

		log.info("Patching substitutions");

		int retryCounter = 0;

		for(LocalDate localDate = mon; localDate.isBefore(sun); localDate = localDate.plusDays(1)) {
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
