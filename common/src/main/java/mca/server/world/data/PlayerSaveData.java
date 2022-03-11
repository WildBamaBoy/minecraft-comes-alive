package mca.server.world.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import mca.Config;
import mca.advancement.criterion.CriterionMCA;
import mca.cobalt.network.NetworkHandler;
import mca.entity.EntitiesMCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.EntityRelationship;
import mca.entity.ai.relationship.MarriageState;
import mca.entity.ai.relationship.RelationshipType;
import mca.entity.ai.relationship.family.FamilyTree;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.item.ItemsMCA;
import mca.network.client.ShowToastRequest;
import mca.resources.API;
import mca.resources.Rank;
import mca.resources.Tasks;
import mca.util.NbtElementCompat;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import mca.util.compat.OptionalCompat;
import mca.util.compat.PersistentStateCompat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PlayerSaveData extends PersistentStateCompat implements EntityRelationship {
    private final UUID playerId;

    private Optional<UUID> spouseUUID = Optional.empty();

    private Optional<Text> spouseName = Optional.empty();

    private MarriageState marriageState;

    private final ServerWorld world;

    private Optional<Integer> lastSeenVillage = Optional.empty();

    private boolean entityDataSet;
    private NbtCompound entityData;

    private final List<NbtCompound> inbox = new LinkedList<>();

    public static PlayerSaveData get(ServerWorld world, UUID uuid) {
        return WorldUtils.loadData(world.getServer().getOverworld(), nbt -> new PlayerSaveData(world, nbt), w -> new PlayerSaveData(w, uuid), "mca_player_" + uuid.toString());
    }

    PlayerSaveData(ServerWorld world, UUID playerId) {
        assert playerId != null;
        this.world = world;
        this.playerId = playerId;
        resetEntityData();
    }

    PlayerSaveData(ServerWorld world, NbtCompound nbt) {
        this.world = world;
        playerId = nbt.getUuid("playerId");
        lastSeenVillage = nbt.contains("lastSeenVillage", NbtElementCompat.INT_TYPE) ? Optional.of(nbt.getInt("lastSeenVillage")) : Optional.empty();
        spouseUUID = nbt.contains("spouseUUID", NbtElementCompat.INT_TYPE) ? Optional.of(nbt.getUuid("spouseUUID")) : Optional.empty();
        spouseName = nbt.contains("spouseName") ? Optional.of(new LiteralText(nbt.getString("spouseName"))) : Optional.empty();
        entityDataSet = nbt.contains("entityDataSet") && nbt.getBoolean("entityDataSet");

        if (nbt.contains("entityData")) {
            entityData = nbt.getCompound("entityData");
        } else {
            resetEntityData();
        }

        NbtList inbox = nbt.getList("inbox", NbtElementCompat.COMPOUND_TYPE);
        if (inbox != null) {
            this.inbox.clear();
            for (int i = 0; i < inbox.size(); i++) {
                this.inbox.add(inbox.getCompound(i));
            }
        }
    }

    private void resetEntityData() {
        entityData = new NbtCompound();

        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(world);
        assert villager != null;
        villager.initializeSkin();
        villager.getGenetics().randomize();
        villager.getTraits().randomize();
        villager.getVillagerBrain().randomize();
        ((MobEntity)villager).writeCustomDataToNbt(entityData);
    }

    public boolean isEntityDataSet() {
        return entityDataSet;
    }

    public NbtCompound getEntityData() {
        return entityData;
    }

    public void setEntityDataSet(boolean entityDataSet) {
        this.entityDataSet = entityDataSet;
    }

    public void setEntityData(NbtCompound entityData) {
        this.entityData = entityData;
    }

    @Override
    public void onTragedy(DamageSource cause, @Nullable BlockPos burialSite, RelationshipType type, Entity victim) {
        if (playerId == null) {
            return; // legacy: old saves will not have this
        }

        EntityRelationship.super.onTragedy(cause, burialSite, type, victim);

        // send letter of condolence
        if (victim instanceof VillagerEntityMCA) {
            VillagerEntityMCA victimVillager = (VillagerEntityMCA)victim;
            sendLetterOfCondolence((ServerPlayerEntity)world.getEntity(playerId),
                    victimVillager.getName().getString(),
                    victimVillager.getResidency().getHomeVillage().map(Village::getName).orElse(API.getVillagePool().pickVillageName("village")));
        }
    }

    public void updateLastSeenVillage(VillageManager manager, ServerPlayerEntity self) {
        Optional<Village> prevVillage = getLastSeenVillage(manager);
        Optional<Village> nextVillage = OptionalCompat.or(prevVillage
                        .filter(v -> v.isWithinBorder(self))
                , () -> manager.findNearestVillage(self));

        setLastSeenVillage(self, prevVillage.orElse(null), nextVillage.orElse(null));

        // village rank advancement
        if (nextVillage.isPresent()) {
            Rank rank = Tasks.getRank(nextVillage.get(), self);
            CriterionMCA.RANK.trigger(self, rank);
        }
    }

    public void setLastSeenVillage(ServerPlayerEntity self, Village oldVillage, @Nullable Village newVillage) {
        lastSeenVillage = Optional.ofNullable(newVillage).map(Village::getId);
        markDirty();

        if (oldVillage != newVillage) {
            if (oldVillage != null) {
                onLeave(self, oldVillage);
            }
            if (newVillage != null) {
                onEnter(self, newVillage);
            }
        }
    }

    public Optional<Village> getLastSeenVillage(VillageManager manager) {
        return lastSeenVillage.flatMap(manager::getOrEmpty);
    }

    public Optional<Integer> getLastSeenVillageId() {
        return lastSeenVillage;
    }

    protected void onLeave(PlayerEntity self, Village village) {
        if (Config.getInstance().enterVillageNotification) {
            self.sendMessage(new TranslatableText("gui.village.left", village.getName()).formatted(Formatting.GOLD), true);
        }
    }

    protected void onEnter(PlayerEntity self, Village village) {
        if (Config.getInstance().enterVillageNotification) {
            self.sendMessage(new TranslatableText("gui.village.welcome", village.getName()).formatted(Formatting.GOLD), true);
        }
        village.deliverTaxes(world);
    }

    @Override
    public Optional<UUID> getSpouseUuid() {
        return spouseUUID;
    }

    @Override
    public void marry(Entity spouse) {
        MarriageState marriageState = spouse instanceof PlayerEntity ? MarriageState.MARRIED_TO_PLAYER : MarriageState.MARRIED_TO_VILLAGER;
        this.spouseUUID = Optional.of(spouse.getUuid());
        this.spouseName = Optional.of(spouse.getName());
        this.marriageState = marriageState;
        getFamilyEntry().updateMarriage(spouse, marriageState);
        markDirty();
    }

    @Override
    public void endMarriage(MarriageState newState) {
        spouseUUID = Optional.empty();
        spouseName = Optional.empty();
        marriageState = newState;
        getFamilyEntry().setMarriageState(newState);
        markDirty();
    }

    @Override
    public MarriageState getMarriageState() {
        return marriageState;
    }

    @Override
    public Optional<Text> getSpouseName() {
        return isMarried() ? spouseName : Optional.empty();
    }

    @Override
    public FamilyTree getFamilyTree() {
        return FamilyTree.get(world);
    }

    @Override
    public Stream<Entity> getFamily(int parents, int children) {
        return getFamilyEntry()
                .getRelatives(parents, children)
                .map(world::getEntity)
                .filter(Objects::nonNull)
                .filter(e -> !e.getUuid().equals(playerId));
    }

    @Override
    public FamilyTreeNode getFamilyEntry() {
        return getFamilyTree().getOrCreate(world.getEntity(playerId));
    }

    @Override
    public Stream<Entity> getParents() {
        return getFamilyEntry().streamParents().map(world::getEntity).filter(Objects::nonNull);
    }

    @Override
    public Stream<Entity> getSiblings() {
        return getFamilyEntry()
                .siblings()
                .stream()
                .map(world::getEntity)
                .filter(Objects::nonNull)
                .filter(e -> !e.getUuid().equals(playerId)); // we exclude ourselves from the list of siblings
    }

    @Override
    public Optional<Entity> getSpouse() {
        return spouseUUID.map(world::getEntity);
    }

    public void reset() {
        endMarriage(MarriageState.SINGLE);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putUuid("playerId", playerId);
        spouseUUID.ifPresent(id -> nbt.putUuid("spouseUUID", id));
        lastSeenVillage.ifPresent(id -> nbt.putInt("lastSeenVillage", id));
        spouseName.ifPresent(n -> nbt.putString("spouseName", n.getString()));
        nbt.put("entityData", entityData);
        nbt.putBoolean("entityDataSet", entityDataSet);
        nbt.put("inbox", NbtHelper.fromList(inbox, v -> v));
        return nbt;
    }

    public void sendMail(NbtCompound nbt) {
        inbox.add(nbt);
        markDirty();
    }

    public boolean hasMail() {
        return inbox.size() > 0;
    }

    public ItemStack getMail() {
        if (hasMail()) {
            NbtCompound nbt = inbox.remove(0);
            ItemStack stack = new ItemStack(ItemsMCA.LETTER, 1);
            stack.setTag(nbt);
            return stack;
        } else {
            return null;
        }
    }

    public void sendLetterOfCondolence(ServerPlayerEntity player, String name, String village) {
        NbtList l = new NbtList();
        l.add(0, NbtString.of(String.format("{ \"translate\": \"mca.letter.condolence\", \"with\": [\"%s\", \"%s\", \"%s\"] }",
                getFamilyEntry().getName(), name, village)));
        NbtCompound nbt = new NbtCompound();
        nbt.put("pages", l);
        inbox.add(nbt);
        showMailNotification(player);
    }

    public void showMailNotification(ServerPlayerEntity player) {
        NetworkHandler.sendToPlayer(new ShowToastRequest(
                "server.mail.title",
                "server.mail.description"
        ), player);
    }
}
