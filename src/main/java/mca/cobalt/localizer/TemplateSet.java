package mca.cobalt.localizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import mca.api.API;

class TemplateSet {
    static final TemplateSet INSTANCE = new TemplateSet().with("supporter", API::getRandomSupporter);

    private final Map<String, Supplier<String>> variables = new HashMap<>();

    public TemplateSet with(String name, Supplier<String> valueSupplier) {
        variables.put(name, valueSupplier);
        return this;
    }

    public String fill(String input, List<String> vars) {
        input = replace(input);
        // TODO: use regular string templating
        int index = 1;
        String varString = "%v" + index + "%";
        while (input.contains("%v") && index < 10) { // signature of a var being present
            try {
                input = input.replaceAll(varString, vars.get(index - 1));
            } catch (IndexOutOfBoundsException e) {
                input = input.replaceAll(varString, "");
            } finally {
                index++;
                varString = "%v" + index + "%";
            }
        }

        return input;
    }

    public String replace(String input) {
        for (String name : variables.keySet()) {
            input = replaceAll(name, input);
        }
        return input;
    }

    private String replaceAll(String name, String input) {
        String variable = "%v" + name + "%";
        while (input.contains(variable)) {
            input = input.replaceAll(variable, variables.get(name).get());
        }
        return input;
    }
}
