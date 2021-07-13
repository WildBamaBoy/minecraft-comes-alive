package mca.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mca.core.MCA;

import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

public interface Resources {
    String RESOURCE_PREFIX = "assets/mca/";

    Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(RecordTypeAdapterFactory.INSTANCE)
            .create();

    static String read(String path) {
        String data;
        String location = RESOURCE_PREFIX + path;

        try {
            data = IOUtils.toString(new InputStreamReader(MCA.class.getClassLoader().getResourceAsStream(location)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource from JAR: " + location);
        }

        return data;
    }

    static <T> T read(String path, Type type) {
        return GSON.fromJson(Resources.read(path), type);
    }

    static <T> T read(String path, Class<T> type) {
        return GSON.fromJson(Resources.read(path), type);
    }
}
