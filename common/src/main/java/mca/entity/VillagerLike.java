package mca.entity;

import java.util.Optional;

import mca.entity.ai.DialogueType;
import mca.entity.ai.Genetics;
import mca.entity.ai.Infectable;
import mca.entity.ai.Messenger;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.ai.relationship.AgeState;
import mca.resources.API;
import mca.resources.ClothingList;
import mca.resources.data.Hair;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CEnumParameter;
import mca.util.network.datasync.CParameter;
import mca.util.network.datasync.CTrackedEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.village.VillagerDataContainer;

public interface VillagerLike<E extends Entity & VillagerLike<E>> extends CTrackedEntity<E>, VillagerDataContainer, Infectable, Messenger {
    CDataParameter<String> VILLAGER_NAME = CParameter.create("villagerName", "");
    CDataParameter<String> CLOTHES = CParameter.create("clothes", "");
    CDataParameter<String> HAIR = CParameter.create("hair", "");
    CDataParameter<String> HAIR_OVERLAY = CParameter.create("hairOverlay", "");
    CEnumParameter<DyeColor> HAIR_COLOR = CParameter.create("hairColor", DyeColor.class);
    CEnumParameter<AgeState> AGE_STATE = CParameter.create("ageState", AgeState.UNASSIGNED);

    static <E extends Entity> CDataManager.Builder<E> createTrackedData(Class<E> type) {
        return new CDataManager.Builder<>(type)
                .addAll(VILLAGER_NAME, CLOTHES, HAIR, HAIR_OVERLAY, HAIR_COLOR, AGE_STATE)
                .add(Genetics::createTrackedData)
                .add(VillagerBrain::createTrackedData);
    }

    Genetics getGenetics();

    VillagerBrain<?> getVillagerBrain();

    @Override
    default boolean isSpeechImpaired() {
        return isInfected();
    }

    default void setName(String name) {
        setTrackedValue(VILLAGER_NAME, name);
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

    default float getHorizontalScaleFactor() {
        return getGenetics().getVerticalScaleFactor() * getAgeState().getHeight() * getAgeState().getWidth();
    }

    @Override
    default DialogueType getDialogueType(PlayerEntity receiver) {
        return getVillagerBrain().getMemoriesForPlayer(receiver).getDialogueType();
    }

    default void initializeSkin() {
        setClothes(ClothingList.getInstance().getPool(this).pickOne());
        setHair(API.getHairPool().pickOne(this));
    }

    @SuppressWarnings("unchecked")
    default NbtCompound toNbtForConversion(EntityType<?> convertingTo) {
        NbtCompound output = new NbtCompound();
        ((CTrackedEntity<Entity>)this).getTypeDataManager().save(asEntity(), output);
        return output;
    }

    @SuppressWarnings("unchecked")
    default void readNbtForConversion(EntityType<?> convertingFrom, NbtCompound input) {
        ((CTrackedEntity<Entity>)this).getTypeDataManager().load(asEntity(), input);
    }

    default void copyVillagerAttributesFrom(VillagerLike<?> other) {
        readNbtForConversion(other.asEntity().getType(), other.toNbtForConversion(asEntity().getType()));
    }
}
