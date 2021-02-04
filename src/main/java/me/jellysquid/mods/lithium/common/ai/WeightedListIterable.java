package me.jellysquid.mods.lithium.common.ai;

import java.util.Iterator;
import net.minecraft.util.WeightedList;

public interface WeightedListIterable<U> extends Iterable<U> {
    /**
     * {@inheritDoc}
     */
    Iterator<U> iterator();

    /**
     * Returns an {@link Iterable} over the elements in the {@param list}. This allows code to circumvent the usage
     * of streams, providing a speed-up in other areas of the game.
     */
    @SuppressWarnings("unchecked")
    static <T> Iterable<? extends T> cast(WeightedList<T> list) {
        return ((WeightedListIterable<T>) list);
    }

    /**
     * A wrapper type for an iterator over the entries of a {@link WeightedList} which de-references the contained
     * values for consumers.
     *
     * @param <U> The value type stored in each list entry
     */
    class ListIterator<U> implements Iterator<U> {
        private final Iterator<WeightedList.Entry<? extends U>> inner;

        public ListIterator(Iterator<WeightedList.Entry<? extends U>> inner) {
            this.inner = inner;
        }

        @Override
        public boolean hasNext() {
            return this.inner.hasNext();
        }

        @Override
        public U next() {
            return this.inner.next().func_220647_b();
        }
    }
}
