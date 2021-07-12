package mca.cobalt.localizer;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;

// TODO: Just use TranslatableText please.
public interface Localizer {
    static Text localizeText(String key, Object... vars) {
        return new TranslatableText(key, vars);
    }

    static String localize(String key, Object... vars) {
        return localizeText(key, vars).getString();
    }

    static String localize(String key, String keyFallback, Object... vars) {
        return TemplateSet.INSTANCE.replace(Language.getInstance().get(key));
    }
}
