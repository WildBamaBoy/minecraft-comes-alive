package mca.api.types;

public record NameSet (String separator, String[] first, String[] second) {
    @Deprecated
    public String getSeparator() {
        return separator;
    }
    @Deprecated
    public String[] getFirst() {
        return first;
    }
    @Deprecated
    public String[] getSecond() {
        return second;
    }
}
