package mca.entity.data;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.enums.Gender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FamilyTreeEntry implements Serializable {
    private static final long serialVersionUID = -5088784719024378021L;

    private final String name;
    private final boolean isPlayer;
    private final Gender gender;
    private final UUID father;
    private final UUID mother;
    private final List<UUID> children;

    public FamilyTreeEntry(String name, boolean isPlayer, Gender gender, UUID father, UUID mother) {
        this.name = name;
        this.isPlayer = isPlayer;
        this.gender = gender;
        this.father = father;
        this.mother = mother;
        children = new LinkedList<>();
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

    public CNBT save() {
        CNBT nbt = CNBT.createNew();
        nbt.setString("name", name);
        nbt.setBoolean("isPlayer", isPlayer);
        nbt.setInteger("gender", gender.getId());
        nbt.setUUID("father", father);
        nbt.setUUID("mother", mother);

        NbtList childrenList = new NbtList();
        for (UUID child : children) {
            CNBT n = CNBT.createNew();
            n.setUUID("uuid", child);
            childrenList.add(n.getMcCompound());
        }
        nbt.setList("children", childrenList);

        return nbt;
    }

    public static FamilyTreeEntry fromCBNT(CNBT nbt) {
        FamilyTreeEntry e = new FamilyTreeEntry(
                nbt.getString("name"),
                nbt.getBoolean("isPlayer"),
                Gender.byId(nbt.getInteger("gender")),
                nbt.getUUID("father"),
                nbt.getUUID("mother")
        );

        NbtList childrenList = nbt.getCompoundList("children");
        for (int i = 0; i < childrenList.size(); i++) {
            NbtCompound c = childrenList.getCompound(i);
            e.children.add(c.getUuid("uuid"));
        }

        return e;
    }

}
