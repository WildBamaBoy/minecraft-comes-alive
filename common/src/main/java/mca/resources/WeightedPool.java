package mca.resources;

import java.util.ArrayList;
import java.util.List;

public class WeightedPool<T> {

    private final T defaultValue;

    protected final List<WeightedPool.Entry<T>> entries = new ArrayList<>();

    public WeightedPool(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public T pickOne() {
        double totalChance = entries.stream().mapToDouble(a -> a.weight).sum() * API.getRng().nextDouble();

        for (WeightedPool.Entry<T> e : entries) {
            totalChance -= e.weight;
            if (totalChance <= 0.0) {
                return e.value;
            }
        }
        return defaultValue;
    }

    public T pickNext(T current, int next) {
        //look for the current one
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).value.equals(current)) {
                return entries.get(Math.floorMod(i + next, entries.size())).value;
            }
        }

        //fallback
        return pickOne();
    }

    public static class Entry<T> {
        private final T value;
        private final float weight;

        public Entry(T value, float weight) {
            this.value = value;
            this.weight = Math.max(1, weight);
        }
    }

    public static class Mutable<T> extends WeightedPool<T> {

        public Mutable(T defaultValue) {
            super(defaultValue);
        }

        public void add(T value, float weight) {
            entries.add(new WeightedPool.Entry<>(value, weight));
        }
    }
}
