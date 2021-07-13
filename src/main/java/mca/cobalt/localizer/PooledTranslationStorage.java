package mca.cobalt.localizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PooledTranslationStorage {
    private static final Pattern TRAILING_NUMBERS_PATTERN = Pattern.compile("[0-9]+$");
    private static final Predicate<String> TRAILING_NUMERS_PREDICATE = TRAILING_NUMBERS_PATTERN.asPredicate();

    private final Map<String, List<String>> multiTranslations = new HashMap<>();

    private final Random rand = new Random();

    public PooledTranslationStorage(Map<String, String> translations) {
        translations.forEach(this::addTranslation);
    }

    private void addTranslation(String key, String value) {
        if (TRAILING_NUMERS_PREDICATE.test(key)) {
            multiTranslations
                .computeIfAbsent(TRAILING_NUMBERS_PATTERN.matcher(key).replaceAll(""), k -> new ArrayList<>())
                .add(value);
        }
    }

    @NotNull
    private List<String> getOptions(String key) {
        return multiTranslations.getOrDefault(TRAILING_NUMBERS_PATTERN.matcher(key).replaceAll(""), List.of());
    }

    @Nullable
    public String get(String key) {
        List<String> options = getOptions(key);
        if (!options.isEmpty()) {
            return TemplateSet.INSTANCE.replace(options.get(rand.nextInt(options.size())));
        }
        return null;
    }

    public boolean contains(String key) {
        return !getOptions(key).isEmpty();
    }
}
