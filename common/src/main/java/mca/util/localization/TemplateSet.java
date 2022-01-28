package mca.util.localization;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mca.resources.API;
import mca.resources.Supporters;

class TemplateSet {
    static final TemplateSet INSTANCE = new TemplateSet().with("supporter", Supporters::getRandomSupporter);

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
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = Pattern.compile("\\%" + name + "(\\:[0-9]+)?\\%", Pattern.CASE_INSENSITIVE).matcher(input);

        Map<String, String> computed = new HashMap<>();

        while (matcher.find()) {
            String found = matcher.group();
            String replacement = computed.get(found.toLowerCase(Locale.ENGLISH));
            if (replacement == null) {
                replacement = variables.get(name).get();
                computed.put(found.toLowerCase(Locale.ENGLISH), replacement);
            }

            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }
}
