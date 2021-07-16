package mca.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public interface NbtHelper {

    static NbtCompound copyTo(NbtCompound from, NbtCompound to) {
        from.getKeys().forEach(key -> to.put(key, from.get(key)));
        return to;
    }

    static <V> List<V> toList(NbtElement nbt, Function<NbtElement, V> valueMapper) {
        return ((NbtList)nbt).stream().map(valueMapper).collect(Collectors.toList());
    }

    static <K, V> Map<K, V> toMap(NbtCompound nbt, Function<String, K> keyMapper, Function<NbtElement, V> valueMapper) {
        return nbt.getKeys().stream().collect(Collectors.toMap(
                keyMapper,
                v -> valueMapper.apply(nbt.get(v)))
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
