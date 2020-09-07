package pl.szymon.btt_bot.structures;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Basically an ArrayList, except it doesn't throw {@link java.lang.IndexOutOfBoundsException},
 * but returns {@link #defaultObject}.
 * @param <E> the type of elements in this list
 */
public class NoThrowArrayList<E> extends ArrayList<E> {
    private final E defaultObject;

    public NoThrowArrayList(E defaultObject) {
        this.defaultObject = defaultObject;
    }

    public NoThrowArrayList(int initialCapacity) {
        super(initialCapacity);
        this.defaultObject = null;
    }

    public NoThrowArrayList() {
        this.defaultObject = null;
    }

    public NoThrowArrayList(@NotNull Collection<? extends E> c) {
        super(c);
        this.defaultObject = null;
    }

    @Override
    public E get(int index) {
        if(index < 0 || index >= size())
            return defaultObject;

        return super.get(index);
    }
}
