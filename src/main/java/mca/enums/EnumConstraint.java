package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum EnumConstraint {
    FAMILY("family"),
    NOT_FAMILY("notfamily"),
    ADULTS("adults"),
    SPOUSE("spouse"),
    NOT_SPOUSE("notspouse"),
    HIDE_ON_FAIL("hideonfail");

    String id;

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
        Optional<EnumConstraint> state = Arrays.stream(values()).filter((e) -> e.id.equals(value)).findFirst();
        return state.orElse(null);
    }

}

