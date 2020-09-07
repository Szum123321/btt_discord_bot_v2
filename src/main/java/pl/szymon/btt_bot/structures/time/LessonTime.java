package pl.szymon.btt_bot.structures.time;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.szymon.btt_bot.bot.TranslatableText;
import pl.szymon.btt_bot.network.TypeDeclarationsDownloader;

import java.time.Duration;
import java.time.LocalTime;

@Value
@EqualsAndHashCode(callSuper = true)
public class LessonTime extends LocalTimeRange {
    int id;
    PeriodType type;

    public LessonTime(LocalTime start, LocalTime end, int id, PeriodType type) {
        super(start, end);
        this.id = id;
        this.type = type;
    }

    public static LessonTime fromPeriod(TypeDeclarationsDownloader.Period period) {
        return new LessonTime(period.getStartTime(), period.getEndTime(), period.getId(), PeriodType.LESSON);
    }

    public static LessonTime newPause(LocalTime start, LocalTime end, int id) {
        return new LessonTime(start, end, id, PeriodType.PAUSE);
    }

    public Duration getDuration() {
        return Duration.ofSeconds(getEnd().toSecondOfDay() - getStart().toSecondOfDay());
    }

    public TranslatableText print() {
        switch (type) {
            case LESSON:
                return new TranslatableText("lessontime_lesson_print_format", id, getStart(), getEnd());

            case PAUSE:
                return new TranslatableText("lessontime_pause_print_format", id, getDuration().toMinutes(), getStart(), getEnd());

            default:
                return new TranslatableText("missing_translation");
        }
    }

    @Override
    public String toString() {
        return "LessonTime(type=" + getType() + ", start=" + getStart() +", end=" + getEnd() + ", duration=" + getDuration().toMinutes() + ", id=" + id + ")";
    }
}
