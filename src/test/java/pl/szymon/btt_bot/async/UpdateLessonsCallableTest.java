package pl.szymon.btt_bot.async;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import pl.szymon.btt_bot.structures.CompleteTimetable;

import java.io.IOException;
import java.time.LocalDate;

@Log4j2
class UpdateLessonsCallableTest {

    @Test
    void call() throws IOException, InterruptedException {
        long startTime = System.nanoTime();

        UpdateLessonsCallable callable = new UpdateLessonsCallable("4P", "-", "-", LocalDate.now());
        CompleteTimetable timetable = callable.call();

        long stop = System.nanoTime();

        assert !timetable.getLessons().isEmpty();

        timetable.getLessons().forEach(l -> log.info("{}", l));
        log.info("Test took: {} ns", stop - startTime);
    }
}