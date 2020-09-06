package pl.szymon.btt_bot.structures.time;

import lombok.EqualsAndHashCode;

import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
public class LocalTimeRange extends AbstractComparableRange<LocalTime> {
    public LocalTimeRange(LocalTime start, LocalTime end) {
        super(start, end);
    }

    @Override
    public AbstractComparableRange<LocalTime> merge(AbstractComparableRange<LocalTime> value) {
        return new LocalTimeRange(
                LocalTime.ofNanoOfDay(Math.min(getStart().toNanoOfDay(), value.getStart().toNanoOfDay())),
                LocalTime.ofNanoOfDay(Math.max(getEnd().toNanoOfDay(), value.getEnd().toNanoOfDay()))
        );
    }

    @Override
    public String toString() {
        return "LocalTimeRange(start=" + getStart() + ", end=" + getEnd() + ")";
    }
}
