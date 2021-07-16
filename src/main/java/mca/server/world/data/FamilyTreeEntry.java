package mca.server.world.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import mca.entity.ai.relationship.Gender;
import mca.util.NbtHelper;

public record FamilyTreeEntry (
        String name,
        boolean isPlayer,
        Gender gender,
        UUID father,
        UUID mother,
        Set<UUID> children) implements Serializable {
    public FamilyTreeEntry(NbtCompound nbt) {
        this(
            nbt.getString("name"),
            nbt.getBoolean("isPlayer"),
            Gender.byId(nbt.getInt("gender")),
            nbt.getUuid("father"),
            nbt.getUuid("mother"),
            new HashSet<>(NbtHelper.toList(nbt.getList("children", NbtElement.COMPOUND_TYPE), c -> ((NbtCompound)c).getUuid("uuid")))
        );
    }

    public NbtCompound save() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putBoolean("isPlayer", isPlayer);
        nbt.putInt("gender", gender.getId());
        nbt.putUuid("father", father);
        nbt.putUuid("mother", mother);
        nbt.put("children", NbtHelper.fromList(children, child -> {
            NbtCompound n = new NbtCompound();
            n.putUuid("uuid", child);
            return n;
        }));
        return nbt;
    }

    public boolean isParent(UUID id) {
        return father().equals(id) || mother().equals(id);
    }

    public Stream<UUID> streamChildren() {
        return children.stream();
    }

    public Stream<UUID> parents() {
        return Stream.of(father(), mother());
    }
}
