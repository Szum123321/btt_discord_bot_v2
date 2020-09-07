package pl.szymon.btt_bot.structures.time;

import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class PointRangeTimePeriodTree<P extends Comparable<P>, R extends AbstractComparableRange<P>> {
    private final Object[] tree;
    private final int size;
    private final int halfSize;

    public PointRangeTimePeriodTree(List<R> inputArray) {
        this.size = (int) Math.pow(2, Math.ceil(Math.log(inputArray.size())/ Math.log(2))) * 2;
        this.halfSize = size / 2;

        tree = new Object[size];

        tree[0] = null;

        System.arraycopy(inputArray.toArray(), 0, tree, halfSize, inputArray.size());

        Arrays.sort(tree, halfSize, halfSize + inputArray.size());

        for(int i = halfSize - 1; i > 0; i--) {
            if(tree[i * 2] != null && tree[i * 2 + 1] != null)
                tree[i] = getNode(i * 2).merge(getNode(i * 2 + 1));
            else if(tree[i * 2 + 1] != null)
                tree[i] = tree[i * 2 + 1];
            else if(tree[i * 2] != null)
                tree[i] = tree[i * 2];
        }
    }

    public Optional<R> get (P then) {
        if(!getNode(1).isWithin(then))
            return Optional.empty();

        int i = 1;

        while (i < halfSize) {
            if(getNode(i * 2) != null && getNode(i * 2).isWithin(then)) {
                i *= 2;
            } else if (getNode(i * 2 + 1) != null && getNode(i * 2 + 1).isWithin(then)) {
                i = i * 2 + 1;
            } else {
                return Optional.empty();
            }
        }

        return Optional.ofNullable(getNode(i));
    }

    @SuppressWarnings("unchecked")
    private R getNode(int id) {
        return (R)tree[id];
    }
}
