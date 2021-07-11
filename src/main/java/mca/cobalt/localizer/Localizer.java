package mca.cobalt.localizer;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

// TODO: Just use TranslatableText please.
public interface Localizer {
    static Text localizeText(String key, String... vars) {
        return new LiteralText(localize(key, vars));
    }

    static String localize(String key, String... vars) {
        return localizeWithFallback(key, null, vars);
    }

    static String localizeWithFallback(String key, @Nullable String keyFallback, String... vars) {
        return localize(key, keyFallback, List.of(vars));
    }

    static String localize(String key, String keyFallback, List<String> vars) {
        return TemplateSet.INSTANCE.fill(Language.getInstance().get(key), vars);
    }
}
