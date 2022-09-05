package pl.szymon.btt_bot.bot;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import pl.szymon.btt_bot.async.UpdateLessonsCallable;
import pl.szymon.btt_bot.structures.CompleteTimetable;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class BotDataHandler {
    private final static int DAY_CONST = 86400; // 60 * 60 * 24
    public static final PrintHandler DEFAULT_LOG_PRINT_HANDLER = (level, text) -> log.log(level, text.getString());

    private final AtomicReference<CompleteTimetable> completeTimetableAtomicReference = new AtomicReference<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new PrefixedThreadFactory("DATA_UPDATE_THREAD"));
    private final String klassName;
    private final String login, password;

    public BotDataHandler(String klassName, String login, String password) {
        this.klassName = klassName;
        this.login = login;
        this.password = password;

        executorService.scheduleAtFixedRate(
                updateData(DEFAULT_LOG_PRINT_HANDLER),
                DAY_CONST - LocalTime.now().toSecondOfDay(),
                DAY_CONST,
                TimeUnit.SECONDS
        );
    }

    public CompleteTimetable get() {
        return completeTimetableAtomicReference.get();
    }

    public Runnable updateData(PrintHandler printHandler) {
        return () -> {
            CompleteTimetable data = null;
            int retryCounter = 0;
            boolean shouldRetry = false;

            var now = LocalDate.now();

            if(now.getDayOfWeek().equals(DayOfWeek.SUNDAY)) now = now.plusDays(1);

            do {
                if(shouldRetry) log.info("Retrying...");

                shouldRetry = false;

                try {
                    data = new UpdateLessonsCallable(klassName, login, password, now).call();
                } catch (InterruptedException | IOException e) {
                    log.warn("Download failed with exception!", e);

                    shouldRetry = true;
                    retryCounter++;
                }
            } while (shouldRetry && retryCounter < 3);

            if(data != null) {
                completeTimetableAtomicReference.set(data);
                printHandler.info(new TranslatableText("download_success"));
            } else {
                printHandler.error(new TranslatableText("download_failed"));
            }
        };
    }

    public interface PrintHandler {
        void print(Level level, TranslatableText text);

        default void info(TranslatableText text) {
            print(Level.INFO, text);
        }

        default void error(TranslatableText text) {
            print(Level.ERROR, text);
        }
    }

    private static class PrefixedThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);
        private final String prefix;

        public PrefixedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, prefix + "_" + counter.getAndIncrement());
        }
    }
}
