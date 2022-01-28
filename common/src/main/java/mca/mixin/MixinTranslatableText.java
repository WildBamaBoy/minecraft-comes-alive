package mca.mixin;

import java.util.List;
import java.util.Locale;
import mca.entity.ai.DialogueType;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.TranslationException;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TranslatableText.class)
public abstract class MixinTranslatableText {
    @Shadow
    @Final
    private String key;

    @Shadow
    @Final
    private List<StringVisitable> translations;

    @Shadow
    @Nullable
    private Language languageCache;

    @Shadow
    protected abstract void setTranslation(String p_240758_1_);

    private String getExistingKey() {
        int split = key.indexOf(".");
        if (split > 0) {
            DialogueType t = DialogueType.MAP.get(key.substring(0, split));
            if (t == null) {
                return key;
            } else {
                String phrase = key.substring(split + 1);
                while (t != null) {
                    String s = t.name().toLowerCase(Locale.ENGLISH) + "." + phrase;
                    if (Language.getInstance().hasTranslation(s)) {
                        return s;
                    } else {
                        t = t.fallback;
                    }
                }
                return phrase;
            }
        }
        return key;
    }

    @Inject(method = "updateTranslations", at = @At("HEAD"), cancellable = true)
    private void updateTranslations(CallbackInfo ci) {
        Language languagemap = Language.getInstance();
        if (languagemap != this.languageCache) {
            this.languageCache = languagemap;
            this.translations.clear();
            String s = languagemap.get(getExistingKey());

            try {
                this.setTranslation(s);
            } catch (TranslationException var4) {
                this.translations.clear();
                this.translations.add(StringVisitable.plain(s));
            }
        }
        ci.cancel();
    }
}
