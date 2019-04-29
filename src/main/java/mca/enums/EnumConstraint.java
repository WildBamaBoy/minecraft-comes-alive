package mca.enums;

import java.util.ArrayList;
import java.util.List;

public enum EnumConstraint {
    ROMANTIC("romantic"),
    ADULTS("adults"),
    SPOUSE("spouse"),
    NOT_SPOUSE("notspouse"),
    HIDE_ON_FAIL("hideonfail");
    String id;

    EnumConstraint(String id) {
        this.id = id;
    }

    public static List<EnumConstraint> fromStringList(String constraints) {
        List<EnumConstraint> list = new ArrayList<>();

        if (constraints != null && !constraints.isEmpty()) {
            String[] splitConstraints = constraints.split("\\|");

            for (String s : splitConstraints) {
                EnumConstraint constraint = byValue(s);
                if (s != null) {
                    list.add(constraint);
                }
            }
        }

        return list;
    }

    public static EnumConstraint byValue(String value) {
        for (EnumConstraint constraint : values()) {
            if (constraint.getId().equals(value)) {
                return constraint;
            }
        }

        return null;
    }

    public String getId() {
        return id;
    }
}

