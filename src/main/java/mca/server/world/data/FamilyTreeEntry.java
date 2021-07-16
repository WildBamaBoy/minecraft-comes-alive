package mca.server.world.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import mca.entity.ai.relationship.Gender;
import mca.util.NbtHelper;

public class FamilyTreeEntry implements Serializable {
    private static final long serialVersionUID = -5088784719024378021L;

    private final String name;
    private final boolean isPlayer;
    private final Gender gender;
    private final UUID father;
    private final UUID mother;
    private final List<UUID> children;

    public FamilyTreeEntry(String name, boolean isPlayer, Gender gender, UUID father, UUID mother, List<UUID> children) {
        this.name = name;
        this.isPlayer = isPlayer;
        this.gender = gender;
        this.father = father;
        this.mother = mother;
        this.children = children;
    }

    public FamilyTreeEntry(NbtCompound nbt) {
        this(
            nbt.getString("name"),
            nbt.getBoolean("isPlayer"),
            Gender.byId(nbt.getInt("gender")),
            nbt.getUuid("father"),
            nbt.getUuid("mother"),
            NbtHelper.toList(nbt.getList("children", NbtElement.COMPOUND_TYPE), c -> ((NbtCompound)c).getUuid("uuid"))
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
    public String getName() {
        return name;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public Gender getGender() {
        return gender;
    }

    public UUID getFather() {
        return father;
    }

    public UUID getMother() {
        return mother;
    }

    public List<UUID> getChildren() {
        return children;
    }
}
