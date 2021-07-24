package mca.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public interface NbtHelper {

    @SuppressWarnings("unchecked")
    static <T extends NbtElement> T computeIfAbsent(NbtCompound nbt, String key, int type, Supplier<T> factory) {
        if (!nbt.contains(key, type)) {
            nbt.put(key, factory.get());
        }
        return (T)nbt.get(key);
    }

    static NbtCompound copyTo(NbtCompound from, NbtCompound to) {
        from.getKeys().forEach(key -> to.put(key, from.get(key)));
        return to;
    }

    static <V> List<V> toList(NbtElement nbt, Function<NbtElement, V> valueMapper) {
        return toStream(nbt, valueMapper).collect(Collectors.toList());
    }

    static <V> Stream<V> toStream(NbtElement nbt, Function<NbtElement, V> valueMapper) {
        return ((NbtList)nbt).stream().map(valueMapper);
    }

    static <K, V> Map<K, V> toMap(NbtCompound nbt, Function<String, K> keyMapper, Function<NbtElement, V> valueMapper) {
        return nbt.getKeys().stream()
                .map(e -> {
                    K k = keyMapper.apply(e);
                    if (k == null) return null;
                    V v = valueMapper.apply(nbt.get(e));
                    if (v == null) return null;
                    return k == null ? null : new Pair<>(k, v);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)
        );
    }

    static <V> NbtList fromList(Iterable<V> list, Function<V, NbtElement> valueMapper) {
        NbtList output = new NbtList();
        list.forEach(item -> {
            output.add(valueMapper.apply(item));
        });
        return output;
    }

    static <K, V> NbtCompound fromMap(NbtCompound output, Map<K, V> map, Function<K, String> keyMapper, Function<V, NbtElement> valueMapper) {
        map.forEach((key, value) -> {
            output.put(keyMapper.apply(key), valueMapper.apply(value));
        });
        return output;
    }
}
