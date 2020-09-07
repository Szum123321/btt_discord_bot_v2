package pl.szymon.btt_bot.structures;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Wrapper class for accessing multiple lists as one
 * <em>WARNING!</em> methods marked with @Deprecated are not implemented and <em>SHOULD NOT EVER BE USED!</em>
 * @param <E> the type of elements in this list
 */
public class UnionList<E> implements List<E> {
    private final List<E>[] data;
    private int[] sizeList;
    private int size;
/*
    public UnionList(List<E>... data) {
        this.sizeList = new int[data.length];
        int size1 = 0;

        for(int i = 0; i < data.length; i++) {
            size1 += data[i].size();
            sizeList[i] = data[i].size();
        }

        size = size1;

        this.data = data;
    }
*/

    public UnionList(List<E> data1, List<E> data2) {
        this.sizeList = new int[]{data1.size(), data2.size()};

        size = data1.size() + data2.size();

        this.data = new List[]{data1, data2};
    }

    private IntPair getArrayId(int index) {
        int arrayId;

        for(arrayId = 0; arrayId < data.length; arrayId++) {
            if(index >= sizeList[arrayId])
                index -= sizeList[arrayId];
            else
                break;
        }

        return new IntPair(arrayId, index);
    }

    private E get(IntPair index) {
        return data[index.getA()].get(index.getB());
    }

    /*private void set(IntPair index, E element) {
        data[index.getA()].set(index.getB(), element);
    }*/

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size > 0;
    }

    @Override
    @Deprecated
    public boolean contains(Object o) {
        return false;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Itr(0);
    }

    @NotNull
    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int accumulator = 0;

        for(int i = 0; i < sizeList.length; i++) {
            System.arraycopy(data[i].toArray(), 0, arr, accumulator, sizeList[i]);
            accumulator += sizeList[i];
        }

        return arr;
    }

    @NotNull
    @Override
    @Deprecated
    public <T> T[] toArray(@NotNull T[] a) {
        return null;
    }

    @Override
    @Deprecated
    public boolean add(E e) {
        return false;
    }

    @Override
    @Deprecated
    public boolean remove(Object o) {
        return false;
    }

    @Override
    @Deprecated
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    @Deprecated
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return false;
    }

    @Override
    @Deprecated
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        return false;
    }

    @Override
    @Deprecated
    public boolean removeAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    @Deprecated
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    @Deprecated
    public void clear() {

    }

    @Override
    public E get(int index) {
        return get(getArrayId(index));
    }

    @Override
    @Deprecated
    public E set(int index, E element) {
        E old = get(index);
        //set(getArrayId(index), element);
        return old;
    }

    @Override
    @Deprecated
    public void add(int index, E element) {

    }

    @Override
    @Deprecated
    public E remove(int index) {
        return null;
    }

    @Override
    @Deprecated
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    @Deprecated
    public int lastIndexOf(Object o) {
        return 0;
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return new Itr(0);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return new Itr(index);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Deprecated
    public List<E> subList(int fromIndex, int toIndex) {
        return null;
    }

    private class Itr implements ListIterator<E> {
        int lastReturned = -1;
        int cursor;

        public Itr(int cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public E next() {
            try {
                int i = cursor;
                E next = get(i);
                lastReturned = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public E previous() {
            int i = cursor - 1;
            E previous = get(i);
            lastReturned = cursor = i;
            return previous;
        }

        @Override
        public int nextIndex() {
            return cursor + 1;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {

        }

        @Override
        public void set(E e) {
            if(lastReturned < 0)
                throw new IllegalStateException();

            UnionList.this.set(lastReturned, e);
        }

        @Override
        public void add(E e) {

        }
    }

    private static class IntPair {
        private final int a, b;

        public IntPair(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }
    }
}
