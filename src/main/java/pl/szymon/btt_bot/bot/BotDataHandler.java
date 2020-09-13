package pl.szymon.btt_bot.bot;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import pl.szymon.btt_bot.async.UpdateLessonsCallable;
import pl.szymon.btt_bot.structures.CompleteTimetable;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class BotDataHandler {
    private final static int DAY_CONST = 86400; // 60 * 60 * 24
    public static final PrintHandler LOG_PRINT_HANDLER = (level, text) -> log.log(level, text.getString());

    private final AtomicReference<CompleteTimetable> completeTimetableAtomicReference = new AtomicReference<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "DATA_UPDATE_THREAD"));

    private final String klassName;

    public BotDataHandler(String klassName) {
        this.klassName = klassName;

        executorService.scheduleAtFixedRate(
                updateData(LOG_PRINT_HANDLER),
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
            boolean shouldRetry;

            do {
                shouldRetry = false;

                try {
                    data = new UpdateLessonsCallable(klassName).call();
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
}
