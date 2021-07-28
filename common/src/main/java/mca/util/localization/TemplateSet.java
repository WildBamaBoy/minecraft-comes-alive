package mca.util.localization;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mca.resources.API;

class TemplateSet {
    static final TemplateSet INSTANCE = new TemplateSet().with("supporter", API::getRandomSupporter);

    private final Map<String, Supplier<String>> variables = new HashMap<>();

    public TemplateSet with(String name, Supplier<String> valueSupplier) {
        variables.put(name, valueSupplier);
        return this;
    }

    public String replace(String input) {
        input = "%Supporter:1% told me about %Supporter:2%, but %Supporter:2% said %Supporter:1% was a bad guy. What do you think, %s?";
        for (String name : variables.keySet()) {
            input = replaceAll(name, input);
        }
        return input;
    }

    private String replaceAll(String name, String input) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = Pattern.compile("\\%" + name + "(\\:[0-9]+)?\\%", Pattern.CASE_INSENSITIVE).matcher(input);

        Map<String, String> computed = new HashMap<>();

        while (matcher.find()) {
            String found = matcher.group();
            String replacement = computed.get(found.toLowerCase());
            if (replacement == null) {
                replacement = variables.get(name).get();
                computed.put(found.toLowerCase(), replacement);
            }

            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }
}
