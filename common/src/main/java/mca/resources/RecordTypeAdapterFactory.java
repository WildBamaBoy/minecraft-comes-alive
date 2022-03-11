/*
package mca.resources;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.Util;

/**
 * https://github.com/google/gson/issues/1794
 * /
final class RecordTypeAdapterFactory implements TypeAdapterFactory {
    public static final TypeAdapterFactory INSTANCE = new RecordTypeAdapterFactory();

    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Util.make(new HashMap<>(), m -> {
        m.put(byte.class, (byte)0);
        m.put(int.class, 0);
        m.put(long.class, 0L);
        m.put(double.class, 0D);
        m.put(float.class, 0F);
        m.put(char.class, '\0');
        m.put(boolean.class, false);
    });

    private RecordTypeAdapterFactory() {}

    @Nullable
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
        @SuppressWarnings("unchecked")
        final Class<T> rawType = (Class<T>)type.getRawType();

        if (!rawType.isRecord()) {
            return null;
        }

        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<>() {
            @Nullable
            private TypeParameters<T> typeParams;

            @Override
            public void write(JsonWriter out, @Nullable T value) throws IOException {
                delegate.write(out, value);
            }

            @Nullable
            @Override
            public T read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }

                try {
                    if (typeParams == null) {
                        typeParams = new TypeParameters<>(rawType);
                    }

                    return typeParams.constructor.newInstance(typeParams.parseArgs(gson, in));
                } catch (InstantiationException | SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static final class TypeParameters<T> {
        final Map<String, TypeToken<?>> typeMap = new HashMap<>();
        final RecordComponent[] recordComponents;

        final Constructor<T> constructor;

        TypeParameters(Class<T> rawType) throws NoSuchMethodException, SecurityException {
            recordComponents = rawType.getRecordComponents();

            for (int i = 0; i < recordComponents.length; i++) {
                typeMap.put(recordComponents[i].getName(), TypeToken.get(recordComponents[i].getGenericType()));
            }

            Class<?>[] argTypes = new Class<?>[recordComponents.length];
            for (int i = 0; i < recordComponents.length; i++) {
                argTypes[i] = recordComponents[i].getType();
            }

            constructor = rawType.getDeclaredConstructor(argTypes);
            constructor.setAccessible(true);
        }

        Object[] parseArgs(Gson gson, JsonReader in) throws IOException {
            Map<String, Object> argsMap = new HashMap<>();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (typeMap.containsKey(name)) {
                    argsMap.put(name, gson.getAdapter(typeMap.get(name)).read(in));
                } else {
                    gson.getAdapter(Object.class).read(in);
                }
            }
            in.endObject();

            var args = new Object[recordComponents.length];
            for (int i = 0; i < recordComponents.length; i++) {
                String name = recordComponents[i].getName();
                Object value = argsMap.get(name);
                TypeToken<?> type = typeMap.get(name);
                if (value == null && (type != null && type.getRawType().isPrimitive())) {
                    value = PRIMITIVE_DEFAULTS.get(type.getRawType());
                }
                args[i] = value;
            }

            return args;
        }

    }

}
*/