package pl.szymon.btt_bot.structures.time;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractComparableRange<T extends Comparable<T>> implements Comparable<AbstractComparableRange<T>> {
    final T start;
    final T end;

    public boolean isWithin(T value) {
        return start.compareTo(value) <= 0 && end.compareTo(value) > 0;
    }

    @Override
    public int compareTo(AbstractComparableRange<T> o) {
        return start.compareTo(o.getStart());
    }

    protected abstract AbstractComparableRange<T> merge(AbstractComparableRange<T> value);
}
