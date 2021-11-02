package mca.resources.data;

public class FloatAnalysis extends Analysis<Float> {
    @Override
    public boolean isPositive(Float v) {
        return v >= 0;
    }

    @Override
    public String asString(Float v) {
        return (int)(v * 100.0f) + "%";
    }

    @Override
    public Float getTotal() {
        return (float)Math.max(0.0, getSummands().stream().mapToDouble(SerializablePair::getRight).sum());
    }
}
