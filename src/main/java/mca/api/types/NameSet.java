package mca.api.types;

public class NameSet {
    private final String separator;
    private final String[] first;
    private final String[] second;

    public NameSet(String separator, String[] first, String[] second) {
        this.separator = separator;
        this.first = first;
        this.second = second;
    }

    public String getSeparator() {
        return separator;
    }

    public String[] getFirst() {
        return first;
    }

    public String[] getSecond() {
        return second;
    }
}
