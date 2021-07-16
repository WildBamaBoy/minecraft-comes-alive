package mca.util.localization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import mca.resources.API;

class TemplateSet {
    static final TemplateSet INSTANCE = new TemplateSet().with("supporter", API::getRandomSupporter);

    private final Map<String, Supplier<String>> variables = new HashMap<>();

    public TemplateSet with(String name, Supplier<String> valueSupplier) {
        variables.put(name, valueSupplier);
        return this;
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
