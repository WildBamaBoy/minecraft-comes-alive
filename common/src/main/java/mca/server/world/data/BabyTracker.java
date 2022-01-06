package mca.server.world.data;

import mca.entity.ai.relationship.Gender;
import mca.item.ItemsMCA;
import mca.util.InventoryUtils;
import mca.util.NbtElementCompat;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import mca.util.compat.PersistentStateCompat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

public class BabyTracker extends PersistentStateCompat {
    public static BabyTracker get(ServerWorld world) {
        return WorldUtils.loadData(world.getServer().getOverworld(), nbt -> new BabyTracker(world, nbt), BabyTracker::new, "mca_baby_tracker");
    }

    private final Map<Key, Pairing> pairings = new HashMap<>();

    BabyTracker(ServerWorld world) {
    }

    BabyTracker(ServerWorld world, NbtCompound nbt) {
        nbt.getList("pairings", NbtElementCompat.COMPOUND_TYPE).forEach(element -> {
            Pairing pairing = new Pairing((NbtCompound)element);
            pairings.put(pairing.key, pairing);
        });
    }

    public Optional<MutableChildSaveState> getSaveState(UUID id) {
        return pairings.values().stream()
                .flatMap(pairing -> pairing.children.stream().filter(s -> s.id.equals(id)))
                .findFirst();
    }

    public boolean hasActiveBaby(UUID mother, UUID father) {
        Key key = new Key(mother, father);
        return pairings.containsKey(key) && getPairing(key).getChildCount() > 0;
    }

    public Pairing getPairing(UUID mother, UUID father) {
        return getPairing(new Key(mother, father));
    }

    public Optional<Pairing> getPairingOrEmpty(UUID mother, UUID father) {
        return Optional.ofNullable(pairings.get(new Key(mother, father)));
    }

    public Pairing getPairing(ChildSaveState state) {
        return getPairing(state.key);
    }

    private Pairing getPairing(Key key) {
        return pairings.computeIfAbsent(key, Pairing::new);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        pairings.values().forEach(pairing -> list.add(pairing.toNbt()));
        nbt.put("pairings", list);
        return nbt;
    }

    private static class Key implements Comparable<Key> {
        private final Set<UUID> parents = new HashSet<>();

        public Key(UUID mother, UUID father) {
            parents.add(mother);
            parents.add(father);
        }

        public Key(NbtList nbt) {
            nbt.forEach(i -> parents.add(UUID.fromString(i.asString())));
        }

        public NbtList toNbt() {
            NbtList nbt = new NbtList();
            parents.forEach(parent -> {
                nbt.add(NbtString.of(parent.toString()));
            });
            return nbt;
        }

        @Override
        public int compareTo(Key o) {
            return equals(o) ? 0 : 1;
        }

        private boolean equals(Key o) {
            return o != null && o.parents.equals(parents);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key && equals((Key)o);
        }

        @Override
        public int hashCode() {
            return parents.hashCode();
        }
    }

    public final class Pairing {
        private final Key key;
        private final List<MutableChildSaveState> children;

        public Pairing(Key key) {
            this.key = key;
            children = new ArrayList<>();
            markDirty();
        }

        public Pairing(NbtCompound tag) {
            key = new Key(tag.getList("key", NbtElementCompat.STRING_TYPE));
            children = NbtHelper.toList(tag.getList("children", NbtElementCompat.COMPOUND_TYPE), c -> new MutableChildSaveState((NbtCompound)c));
        }

        public int getChildCount() {
            return children.size();
        }

        public void addChild(Consumer<MutableChildSaveState> factory) {
            MutableChildSaveState state = new MutableChildSaveState(key);
            factory.accept(state);
            children.add(state);
            markDirty();
        }

        public void removeChild(ChildSaveState state) {
            children.removeIf(o -> o.id.equals(state.id));
        }

        public List<ChildSaveState> getChildren() {
            return new ArrayList<>(children);
        }

        /**
         * Searches a player's inventories for a baby
         */
        public Pair<ItemStack, Placement> locateBaby(PlayerEntity player) {

            int slot = InventoryUtils.getFirstSlotContainingItem(player.inventory, stack -> {
                return getState(stack).filter(state -> state.key.equals(key)).isPresent();
            });

            if (slot >= 0) {
                return Pair.of(player.inventory.getStack(slot), Placement.INVENTORY);
            }

            slot = InventoryUtils.getFirstSlotContainingItem(player.getEnderChestInventory(), stack -> {
                return getState(stack).filter(state -> state.key.equals(key)).isPresent();
            });

            if (slot >= 0) {
                return Pair.of(player.getEnderChestInventory().getStack(slot), Placement.ENDER_CHEST);
            }

            return Pair.of(ItemStack.EMPTY, Placement.MISSING);
        }

        public NbtCompound toNbt() {
            NbtCompound tag = new NbtCompound();
            tag.put("key", key.toNbt());
            tag.put("children", NbtHelper.fromList(children, child -> child.writeToNbt(new NbtCompound())));
            return tag;
        }

        public void reconstructBaby(ServerPlayerEntity player) {
            getChildren().forEach(c -> {
                ItemStack stack = new ItemStack(c.getGender() == Gender.MALE ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
                NbtCompound data = stack.getOrCreateSubTag("childData");
                c.writeToNbt(data);
                player.inventory.insertStack(stack);
            });
        }
    }

    public static class ChildSaveState {
        UUID id;

        protected Gender gender;
        protected boolean infected;
        protected long seed;
        protected Optional<String> name = Optional.empty();

        private final Key key;

        protected Optional<Pair<UUID, String>> owner = Optional.empty();

        ChildSaveState(Key key) {
            id = UUID.randomUUID();
            this.key = key;
        }

        public ChildSaveState(NbtCompound tag) {
            id = tag.getUuid("id");
            gender = Gender.byName(tag.getString("gender"));
            infected = tag.getBoolean("infected");
            name = tag.contains("name") ? Optional.of(tag.getString("name")) : Optional.empty();
            seed = tag.getLong("seed");
            key = new Key(tag.getList("key", NbtElementCompat.STRING_TYPE));
        }

        public UUID getId() {
            return id;
        }

        public Stream<UUID> getParents() {
            return key.parents.stream();
        }

        public boolean isInfected() {
            return infected;
        }

        public Optional<String> getName() {
            return name;
        }

        public Optional<Pair<UUID, String>> getOwner() {
            return owner;
        }

        public long getSeed() {
            return seed;
        }

        public Gender getGender() {
            return gender;
        }

        public NbtCompound writeToNbt(NbtCompound tag) {
            tag.putUuid("id", id);
            tag.putString("gender", gender.getStrName());
            tag.putBoolean("infected", infected);
            tag.put("key", key.toNbt());
            tag.putLong("seed", seed);
            name.ifPresent(n -> tag.putString("name", n));
            return tag;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        private boolean equals(ChildSaveState o) {
            return o == this || (o != null && o.id.equals(id));
        }

        @Override
        public boolean equals(Object o) {
            return o == this || (o instanceof ChildSaveState && equals((ChildSaveState)o));
        }
    }

    public class MutableChildSaveState extends ChildSaveState {
        MutableChildSaveState(Key key) {
            super(key);
        }

        public MutableChildSaveState(NbtCompound tag) {
            super(tag);
        }

        public ChildSaveState setGender(Gender gender) {
            this.gender = gender;
            markDirty();
            return this;
        }

        public ChildSaveState setInfected(boolean infected) {
            this.infected = infected;
            markDirty();
            return this;
        }

        public ChildSaveState setName(String name) {
            this.name = Optional.ofNullable(name);
            markDirty();
            return this;
        }

        public ChildSaveState setOwner(Entity entity) {
            owner = Optional.of(Pair.of(entity.getUuid(), entity.getName().getString()));
            markDirty();
            return this;
        }

        public ChildSaveState setSeed(long seed) {
            this.seed = seed;
            markDirty();
            return this;
        }

        public ItemStack createItem() {
            return writeToItem((gender.binary() == Gender.MALE ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL).getDefaultStack());
        }

        public ItemStack writeToItem(ItemStack stack) {
            writeToNbt(stack.getOrCreateSubTag("childData"));
            return stack;
        }
    }

    public enum Placement {
        INVENTORY,
        ENDER_CHEST,
        MISSING;

        public boolean wasFound() {
            return this != MISSING;
        }
    }

    public static Optional<MutableChildSaveState> getState(ItemStack stack, ServerWorld world) {
        if (!hasState(stack)) {
            return Optional.empty();
        }
        ChildSaveState state = new ChildSaveState(stack.getSubTag("childData"));
        return get(world).getPairing(state).children.stream().filter(s -> s.id.equals(state.id)).findAny();
    }

    public static Optional<ChildSaveState> getState(ItemStack stack) {
        return hasState(stack) ? Optional.of(new ChildSaveState(stack.getSubTag("childData"))) : Optional.empty();
    }

    public static Optional<UUID> getStateId(ItemStack stack) {
        return hasState(stack) ? Optional.of(stack.getSubTag("childData").getUuid("id")) : Optional.empty();
    }

    public static boolean hasState(ItemStack stack) {
        return stack.hasTag()
                && !stack.getTag().getBoolean("invalidated")
                && stack.getTag().contains("childData", NbtElementCompat.COMPOUND_TYPE)
                && stack.getSubTag("childData").containsUuid("id");
    }

    public static void invalidate(ItemStack stack) {
        stack.removeSubTag("childData");
        stack.getOrCreateTag().putBoolean("invalidated", true);
    }
}
