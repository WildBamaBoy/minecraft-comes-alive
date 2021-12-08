package mca.entity;

import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import java.util.Map;
import mca.Config;
import mca.entity.ai.DialogueType;
import mca.entity.ai.Genetics;
import mca.entity.ai.Messenger;
import mca.entity.ai.Traits;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.EntityRelationship;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.VillagerDimensions;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.entity.interaction.EntityCommandHandler;
import mca.resources.API;
import mca.resources.ClothingList;
import mca.resources.HairList;
import mca.resources.data.Hair;
import mca.server.world.data.PlayerSaveData;
import mca.util.network.datasync.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.village.VillagerDataContainer;

import java.util.Optional;

public interface VillagerLike<E extends Entity & VillagerLike<E>> extends CTrackedEntity<E>, VillagerDataContainer, Infectable, Messenger {
    CDataParameter<String> VILLAGER_NAME = CParameter.create("villagerName", "");
    CDataParameter<String> CUSTOM_SKIN = CParameter.create("custom_skin", "");
    CDataParameter<String> CLOTHES = CParameter.create("clothes", "");
    CDataParameter<String> HAIR = CParameter.create("hair", "");
    CDataParameter<String> HAIR_OVERLAY = CParameter.create("hairOverlay", "");
    CEnumParameter<DyeColor> HAIR_COLOR = CParameter.create("hairColor", DyeColor.class);
    CEnumParameter<AgeState> AGE_STATE = CParameter.create("ageState", AgeState.UNASSIGNED);

    static <E extends Entity> CDataManager.Builder<E> createTrackedData(Class<E> type) {
        return new CDataManager.Builder<>(type)
                .addAll(VILLAGER_NAME, CUSTOM_SKIN, CLOTHES, HAIR, HAIR_OVERLAY, HAIR_COLOR, AGE_STATE)
                .add(Genetics::createTrackedData)
                .add(Traits::createTrackedData)
                .add(VillagerBrain::createTrackedData);
    }

    Genetics getGenetics();

    Traits getTraits();

    VillagerBrain<?> getVillagerBrain();

    EntityCommandHandler<?> getInteractions();

    default void initialize(SpawnReason spawnReason) {
        if (spawnReason != SpawnReason.CONVERSION) {
            if (spawnReason != SpawnReason.BREEDING) {
                getGenetics().randomize();
                getTraits().randomize();
            }

            if (getGenetics().getGender() == Gender.UNASSIGNED) {
                getGenetics().setGender(Gender.getRandom());
            }

            if (Strings.isNullOrEmpty(getTrackedValue(VILLAGER_NAME))) {
                setName(API.getVillagePool().pickCitizenName(getGenetics().getGender()));
            }

            initializeSkin();

            getVillagerBrain().randomize();
        }

        asEntity().calculateDimensions();
    }

    @Override
    default boolean isSpeechImpaired() {
        return getInfectionProgress() > BABBLING_THRESHOLD;
    }

    @Override
    default boolean isToYoungToSpeak() {
        return getAgeState() == AgeState.BABY;
    }

    default void setName(String name) {
        setTrackedValue(VILLAGER_NAME, name);
        EntityRelationship.of(asEntity()).ifPresent(relationship -> relationship.getFamilyEntry().setName(name));
    }

    default void setCustomSkin(String name) {
        setTrackedValue(CUSTOM_SKIN, name);
    }

    default void updateCustomSkin() {

    }

    default GameProfile getGameProfile() {
        return null;
    }

    default boolean hasCustomSkin() {
        if (!getTrackedValue(CUSTOM_SKIN).isEmpty() && getGameProfile() != null) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraftClient.getSkinProvider().getTextures(getGameProfile());
            return map.containsKey(MinecraftProfileTexture.Type.SKIN);
        } else {
            return false;
        }
    }

    default float getRawScaleFactor() {
        if (getGenetics() == null) {
            return 1.0f;
        } else {
            return getGenetics().getVerticalScaleFactor() * getTraits().getVerticalScaleFactor() * getVillagerDimensions().getHeight() * Config.getInstance().villagerHeight;
        }
    }

    default String getClothes() {
        return getTrackedValue(CLOTHES);
    }

    default void setClothes(String clothes) {
        setTrackedValue(CLOTHES, clothes);
    }

    default Hair getHair() {
        return new Hair(getTrackedValue(HAIR), getTrackedValue(HAIR_OVERLAY));
    }

    default void setHair(Hair hair) {
        setTrackedValue(HAIR, hair.texture());
        setTrackedValue(HAIR_OVERLAY, hair.overlay());
    }

    default void setHairDye(DyeColor color) {
        setTrackedValue(HAIR_COLOR, color);
    }

    default void clearHairDye() {
        setTrackedValue(HAIR_COLOR, null);
    }

    default Optional<DyeColor> getHairDye() {
        return Optional.ofNullable(getTrackedValue(HAIR_COLOR));
    }

    default AgeState getAgeState() {
        return getTrackedValue(AGE_STATE);
    }

    default VillagerDimensions getVillagerDimensions() {
        return getAgeState();
    }

    default boolean setAgeState(AgeState state) {
        AgeState old = getAgeState();
        if (state == old) {
            return false;
        }

        setTrackedValue(AGE_STATE, state);
        asEntity().calculateDimensions();
        return old != AgeState.UNASSIGNED;
    }

    default float getHorizontalScaleFactor() {
        if (getGenetics() == null) {
            return 1.0f;
        } else {
            return Math.min(0.999f, getGenetics().getHorizontalScaleFactor() * getTraits().getHorizontalScaleFactor() * getVillagerDimensions().getWidth());
        }
    }

    @Override
    default DialogueType getDialogueType(PlayerEntity receiver) {
        if (!receiver.world.isClient) {
            // age specific
            DialogueType type = DialogueType.fromAge(getAgeState());

            // relationship specific
            if (!receiver.world.isClient) {
                Optional<EntityRelationship> r = EntityRelationship.of(asEntity());
                if (r.isPresent()) {
                    FamilyTreeNode relationship = r.get().getFamilyEntry();
                    if (relationship.spouse().equals(receiver.getUuid())) {
                        return DialogueType.SPOUSE;
                    } else if (relationship.isParent(receiver.getUuid())) {
                        return type.toChild();
                    }
                }
            }

            // also sync with client
            getVillagerBrain().getMemoriesForPlayer(receiver).setDialogueType(type);
        }

        return getVillagerBrain().getMemoriesForPlayer(receiver).getDialogueType();
    }

    default void initializeSkin() {
        setClothes(ClothingList.getInstance().getPool(this).pickOne());
        setHair(HairList.getInstance().pickOne(this));
    }

    @SuppressWarnings("unchecked")
    default NbtCompound toNbtForConversion(EntityType<?> convertingTo) {
        NbtCompound output = new NbtCompound();
        this.getTypeDataManager().save((E)asEntity(), output);
        return output;
    }

    @SuppressWarnings("unchecked")
    default void readNbtForConversion(EntityType<?> convertingFrom, NbtCompound input) {
        this.getTypeDataManager().load((E)asEntity(), input);
    }

    default void copyVillagerAttributesFrom(VillagerLike<?> other) {
        readNbtForConversion(other.asEntity().getType(), other.toNbtForConversion(asEntity().getType()));
    }

    static VillagerLike<?> toVillager(Entity entity) {
        if (entity instanceof VillagerLike<?>) {
            return (VillagerLike<?>)entity;
        } else if (entity instanceof PlayerEntity) {
            NbtCompound villagerData = PlayerSaveData.get((ServerWorld)entity.world, entity.getUuid()).getEntityData();
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(entity.world);
            assert villager != null;
            villager.readCustomDataFromNbt(villagerData);
            return villager;
        } else {
            return null;
        }
    }

    default boolean isHostile() {
        return false;
    }
}
