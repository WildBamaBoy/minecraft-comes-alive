package mca.cobalt.localizer;

import net.minecraft.util.text.LanguageMap;

import java.util.*;
import java.util.stream.Collectors;

public class Localizer {
    private final ArrayList<VarParser> registeredVarParsers = new ArrayList<>();

    public String localize(String key, String... vars) {
        return localize(key, null, vars);
    }

    public String localize(String key, String keyFallback, String... vars) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, vars);
        return localize(key, keyFallback, list);
    }

    public String localize(String key, String keyFallback, ArrayList<String> vars) {
        LanguageMap localizerMap = LanguageMap.getInstance();

        String result = localizerMap.getOrDefault(key);

        //multi-variant text
        result = getLocalizedString(key, key, localizerMap, result);

        //multi-variant fallback text
        result = getLocalizedString(key, keyFallback, localizerMap, result);

        return parseVars(result, vars);
    }

    private String getLocalizedString(String key, String keyFallback, LanguageMap localizerMap, String result) {
        if (result.equals(key)) {
            List<String> responses = localizerMap.getLanguageData().entrySet().stream().filter(entry -> entry.getKey().startsWith(keyFallback)).map(Map.Entry::getValue).collect(Collectors.toList());
            if (responses.size() > 0) result = responses.get(new Random().nextInt(responses.size()));
        }
        return result;
    }

    public void registerVarParser(VarParser parser) {
        this.registeredVarParsers.add(parser);
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
