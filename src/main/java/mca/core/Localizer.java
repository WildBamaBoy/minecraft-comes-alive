package mca.core;

import com.google.common.base.Charsets;
import net.minecraft.util.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Localizer {
    private Map<String, String> localizerMap = new HashMap<>();
    private static final ArrayList<String> EMPTY_LIST = new ArrayList<>();

    public Localizer() {
        InputStream inStream = StringUtils.class.getResourceAsStream("/assets/mca/lang/en_us.lang");

        try {
            List<String> lines = IOUtils.readLines(inStream, Charsets.UTF_8);

            for (String line : lines) {
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                String[] split = line.split("\\=");
                String key = split[0];
                String value = split[1];

                localizerMap.put(key, value);
            }
        } catch (IOException e) {
            MCA.getLog().error("Error initializing localizer: " + e);
        }
    }

    public String localize(String key, String... vars) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, vars);
        return localize(key, vars != null ? list : EMPTY_LIST);
    }

    public String localize(String key, ArrayList<String> vars) {
        String result = localizerMap.getOrDefault(key, key);
        if (result.equals(key)) {
            List<String> responses = localizerMap.entrySet().stream().filter(entry -> entry.getKey().contains(key)).map(Map.Entry::getValue).collect(Collectors.toList());
            if (responses.size() > 0) result = responses.get(new Random().nextInt(responses.size()));
        }

        return parseVars(result, vars).replaceAll("\\\\", "");
    }

    private String parseVars(String str, ArrayList<String> vars) {
        int index = 1;
        str = str.replaceAll("%Supporter%", MCA.getInstance().getRandomSupporter());

        String varString = "%v" + index + "%";
        while (str.contains("%v") && index < 10) { // signature of a var being present
            try {
                str = str.replaceAll(varString, vars.get(index - 1));
            } catch (IndexOutOfBoundsException e) {
                str = str.replaceAll(varString, "");
                MCA.getLog().warn("Failed to replace variable in localized string: " + str);
            } finally {
                index++;
                varString = "%v" + index + "%";
            }
        }

        return str;
    }
}
