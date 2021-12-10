package mca.resources.data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class Analysis<T extends Serializable> implements Serializable, Iterable<Analysis.AnalysisElement> {
    private static final long serialVersionUID = 2255112660663961645L;

    private List<SerializablePair<String, T>> summands = new LinkedList<>();

    public void add(String key, T value) {
        summands.add(new SerializablePair<>(key, value));
    }

    public List<SerializablePair<String, T>> getSummands() {
        return summands;
    }

    public String getTotalAsString() {
        return asString(getTotal());
    }

    public static class AnalysisElement {
        private final boolean positive;
        private final String value;
        private final String key;

        public AnalysisElement(boolean positive, String value, String key) {
            this.positive = positive;
            this.value = value;
            this.key = key;
        }

        public boolean isPositive() {
            return positive;
        }

        public String getValue() {
            return value;
        }

        public String getKey() {
            return key;
        }
    }

    @NotNull
    @Override
    public Iterator<AnalysisElement> iterator() {
        return new Iterator<AnalysisElement>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < summands.size();
            }

            @Override
            public AnalysisElement next() {
                SerializablePair<String, T> pair = summands.get(i++);
                return new AnalysisElement(isPositive(pair.getRight()), asString(pair.getRight()), pair.getLeft());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    abstract public boolean isPositive(T v);

    abstract public String asString(T v);

    abstract public T getTotal();
}
