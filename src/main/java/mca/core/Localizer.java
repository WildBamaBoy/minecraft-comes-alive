package mca.core;

import com.google.common.base.Charsets;
import net.minecraft.util.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Localizer {
    private Map<String, String> localizerMap = new HashMap<String, String>();

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
        String result = localizerMap.getOrDefault(key, key);
        if (result.equals(key)) {
            List<String> responses = new ArrayList<>();
            for (String value : localizerMap.keySet()) {
                if (value.contains(key)) {
                    responses.add(localizerMap.get(value).replace("\\", ""));
                }
            }

            if (responses.size() > 0) {
                result = responses.get(new Random().nextInt(responses.size()));
            }
        }

        return parseVars(result, vars).replaceAll("\\\\", "");
    }

    private String parseVars(String str, String... vars) {
        int index = 1;
        String varString = "%v" + index + "%";
        while (str.contains(varString)) {
            try {
                str = str.replaceAll(varString, vars[index - 1]);
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
