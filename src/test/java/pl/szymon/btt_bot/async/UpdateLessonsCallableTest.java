package pl.szymon.btt_bot.async;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import pl.szymon.btt_bot.structures.CompleteTimetable;

import java.io.IOException;

@Log4j2
class UpdateLessonsCallableTest {

    @Test
    void call() throws IOException, InterruptedException {
        long startTime = System.nanoTime();

        UpdateLessonsCallable callable = new UpdateLessonsCallable("4P");
        CompleteTimetable timetable = callable.call();

        long stop = System.nanoTime();

        timetable.getSubjects().values().forEach(log::info);
        log.info(timetable.toString());
        log.info("Test took: {} ns", stop - startTime);
    }
}