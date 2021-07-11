package mca.cobalt.localizer;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import mca.api.API;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

// TODO: Just use TranslatableText please.
public final class Localizer {
    private static final Localizer instance = new Localizer()
            .registerVarParser(v -> v.replaceAll("%Supporter%", API.getRandomSupporter()));

    @Deprecated
    public static Localizer getInstance() {
        return instance;
    }

    private final ArrayList<VarParser> registeredVarParsers = new ArrayList<>();

    private Localizer() {}

    public Text localizeText(String key, String... vars) {
        return new LiteralText(localize(key, vars));
    }

    public String localize(String key, String... vars) {
        return localizeWithFallback(key, null, vars);
    }

    public String localizeWithFallback(String key, @Nullable String keyFallback, String... vars) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, vars);
        return localize(key, keyFallback, list);
    }

    public String localize(String key, String keyFallback, ArrayList<String> vars) {
        String result = Language.getInstance().get(key);

        //multi-variant text
        result = getLocalizedString(key, result);

        //multi-variant fallback text
        result = getLocalizedString(keyFallback, result);

        return parseVars(result, vars);
    }

    private String getLocalizedString(String key, String result) {
        if (result.equals(key)) {
            // TODO: You can't do it like this.
            //List<String> responses = Language.getInstance().entrySet().stream().filter(entry -> entry.getKey().startsWith(key)).map(Map.Entry::getValue).collect(Collectors.toList());
            //if (responses.size() > 0) result = responses.get(new Random().nextInt(responses.size()));
        }
        return result;
    }

    public Localizer registerVarParser(VarParser parser) {
        this.registeredVarParsers.add(parser);
        return this;
    }

    private String parseVars(String str, ArrayList<String> vars) {
        int index = 1;
        for (VarParser processor : registeredVarParsers) {
            try {
                str = processor.parse(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String varString = "%v" + index + "%";
        while (str.contains("%v") && index < 10) { // signature of a var being present
            try {
                str = str.replaceAll(varString, vars.get(index - 1));
            } catch (IndexOutOfBoundsException e) {
                str = str.replaceAll(varString, "");
                //Cobalt.getLog().warn("Failed to replace variable in localized string: " + str);
            } finally {
                index++;
                varString = "%v" + index + "%";
            }
        }

        return str;
    }
}
