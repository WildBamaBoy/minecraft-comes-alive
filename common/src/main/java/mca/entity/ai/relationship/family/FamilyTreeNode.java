package mca.entity.ai.relationship.family;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import mca.entity.ai.relationship.EntityRelationship;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.MarriageState;
import mca.util.NbtElementCompat;
import mca.util.NbtHelper;

public final class FamilyTreeNode implements Serializable {
    private static final long serialVersionUID = -7307057982785253721L;

    private final boolean isPlayer;

    private final Gender gender;

    private String name;
    private String profession = Registry.VILLAGER_PROFESSION.getId(VillagerProfession.NONE).toString();

    private final UUID id;

    private UUID father;
    private UUID mother;

    private UUID spouse = Util.NIL_UUID;
    private MarriageState marriageState = MarriageState.SINGLE;

    private boolean deceased;

    private final Set<UUID> children = new HashSet<>();

    private transient final FamilyTree rootNode;

    public FamilyTreeNode(FamilyTree rootNode, UUID id, String name, boolean isPlayer, Gender gender, UUID father, UUID mother) {
        this.rootNode = rootNode;
        this.id = id;
        this.name = name;
        this.isPlayer = isPlayer;
        this.gender = gender;
        this.father = father;
        this.mother = mother;
    }

    public FamilyTreeNode(FamilyTree rootNode, UUID id, NbtCompound nbt) {
        this(
            rootNode,
            id,
            nbt.getString("name"),
            nbt.getBoolean("isPlayer"),
            Gender.byId(nbt.getInt("gender")),
            nbt.getUuid("father"),
            nbt.getUuid("mother")
        );
        children.addAll(NbtHelper.toList(nbt.getList("children", NbtElementCompat.COMPOUND_TYPE), c -> ((NbtCompound)c).getUuid("uuid")));
        profession = nbt.getString("profession");
        deceased = nbt.getBoolean("isDeceased");
        if (nbt.containsUuid("spouse")) {
            spouse = nbt.getUuid("spouse");
        }
        marriageState = MarriageState.byId(nbt.getInt("marriageState"));
    }

    public UUID id() {
        return id;
    }

    public boolean isDeceased() {
        return deceased;
    }

    public void setDeceased(boolean deceased) {
        this.deceased = deceased;
        if (rootNode != null) {
            rootNode.markDirty();
        }
    }

    public void setName(String name) {
        this.name = name;
        if (rootNode != null) {
            rootNode.markDirty();
        }
    }

    public String getName() {
        return name;
    }

    public void setProfession(VillagerProfession profession) {
        this.profession = Registry.VILLAGER_PROFESSION.getId(profession).toString();
        if (rootNode != null) {
            rootNode.markDirty();
        }
    }

    public VillagerProfession getProfession() {
        return Registry.VILLAGER_PROFESSION.get(Identifier.tryParse(profession));
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public Gender gender() {
        return gender;
    }

    public UUID father() {
        return father;
    }

    public UUID mother() {
        return mother;
    }

    /**
     * Id of the last this entity's most recent spouse.
     */
    public UUID spouse() {
        return spouse;
    }

    public MarriageState getMarriageState() {
        return marriageState;
    }

    public void setMarriageState(MarriageState state) {
        this.marriageState = state;
        if (rootNode != null) {
            rootNode.markDirty();
        }
    }

    public void updateMarriage(Entity spouse, MarriageState state) {
        this.spouse = spouse == null ? null : spouse.getUuid();
        this.marriageState = spouse == null ? MarriageState.SINGLE : state;
        if (rootNode != null) {
            if (spouse != null) {
                // ensure the family tree has an entry
                rootNode.getOrCreate(spouse);
            }
            rootNode.markDirty();
        }
    }

    public Set<UUID> children() {
        return children;
    }

    public Stream<UUID> streamChildren() {
        return children.stream().filter(FamilyTreeNode::isValid);
    }

    public Stream<UUID> parents() {
        return Stream.of(father(), mother()).filter(FamilyTreeNode::isValid);
    }

    /**
     * All persons who share at least one common parent
     */
    public Set<UUID> siblings() {
        Set<UUID> siblings = new HashSet<>();

        parents().forEach(parent -> getRoot().getOrEmpty(parent).ifPresent(p -> gatherChildren(p, siblings, 1)));

        return siblings;
    }

    public Stream<UUID> getRelatives() {
        return getRelatives(3);
    }

    public Stream<UUID> getRelatives(int depth) {
        return getRelatives(depth, depth);
    }

    public Stream<UUID> getRelatives(int parentDepth, int childrenDepth) {
        Set<UUID> family = new HashSet<>();

        //fetch parents and children
        gatherParents(this, family, parentDepth);
        gatherChildren(this, family, childrenDepth);

        //and the caller is not meant either
        family.remove(id);

        return family.stream();
    }

    public boolean isRelative(UUID with) {
        return getRelatives().anyMatch(with::equals);
    }

    public Stream<FamilyTreeNode> getParents() {
        return lookup(parents());
    }

    /**
     * All persons who share at least one common parent
     */
    public Stream<FamilyTreeNode> getSiblings() {
        return lookup(siblings().stream());
    }

    public Stream<FamilyTreeNode> lookup(Stream<UUID> uuids) {
        return uuids.map(getRoot()::getOrEmpty).filter(Optional::isPresent).map(Optional::get);
    }

    public boolean isParent(UUID id) {
        return parents().anyMatch(parent -> parent.equals(id));
    }

    public boolean isGrandParent(UUID id) {
        return getParents().anyMatch(parent -> parent.isParent(id));
    }

    public boolean isUncle(UUID id) {
        return getParents().flatMap(parent -> parent.siblings().stream()).distinct().anyMatch(id::equals);
    }

    public void addChild(UUID child) {
        children.add(child);
    }

    public FamilyTree getRoot() {
        return rootNode;
    }

    public boolean assignParents(EntityRelationship one, EntityRelationship two) {
        return assignParent(one.getFamilyEntry()) | assignParent(two.getFamilyEntry());
    }

    public boolean assignParent(FamilyTreeNode parent) {
        if (setParent(parent)) {
            parent.children().add(id);
            return true;
        }
        return false;
    }

    private boolean setParent(FamilyTreeNode parent) {
        Gender gender = parent.gender();

        if (gender == Gender.MALE || gender == Gender.NEUTRAL) {
            if (isValid(father)) {
                return false;
            }
            this.father = parent.id();
            if (rootNode != null) {
                rootNode.markDirty();
            }
            return true;
        }
        if (gender == Gender.FEMALE || gender == Gender.NEUTRAL) {
            if (isValid(mother)) {
                return false;
            }
            this.mother = parent.id();
            if (rootNode != null) {
                rootNode.markDirty();
            }
            return true;
        }
        return false;
    }

    public NbtCompound save() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putBoolean("isPlayer", isPlayer);
        nbt.putBoolean("isDeceased", deceased);
        nbt.putInt("gender", gender.getId());
        nbt.putUuid("father", father);
        nbt.putUuid("mother", mother);
        nbt.putUuid("spouse", spouse);
        nbt.putInt("marriageState", marriageState.ordinal());
        nbt.put("children", NbtHelper.fromList(children, child -> {
            NbtCompound n = new NbtCompound();
            n.putUuid("uuid", child);
            return n;
        }));
        return nbt;
    }

    private static boolean isValid(@Nullable UUID uuid) {
        return uuid != null && !Util.NIL_UUID.equals(uuid);
    }

    private static void gatherParents(FamilyTreeNode current, Set<UUID> family, int depth) {
        gather(current, family, depth, FamilyTreeNode::parents);
    }

    private static void gatherChildren(FamilyTreeNode current, Set<UUID> family, int depth) {
        gather(current, family, depth, FamilyTreeNode::streamChildren);
    }

    private static void gather(@Nullable FamilyTreeNode entry, Set<UUID> output, int depth, Function<FamilyTreeNode, Stream<UUID>> walker) {
        if (entry == null || depth <= 0) {
            return;
        }
        walker.apply(entry).forEach(id -> {
            if (!Util.NIL_UUID.equals(id)) {
                output.add(id); //zero UUIDs are no real members
            }
            entry.getRoot().getOrEmpty(id).ifPresent(e -> gather(e, output, depth - 1, walker));
        });
    }
}
