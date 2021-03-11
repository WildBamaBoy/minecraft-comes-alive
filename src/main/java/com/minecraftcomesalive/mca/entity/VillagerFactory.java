package com.minecraftcomesalive.mca.entity;

import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.merchant.villager.CVillagerProfession;
import cobalt.minecraft.util.math.CPos;
import cobalt.minecraft.world.CWorld;
import com.minecraftcomesalive.mca.api.API;
import com.minecraftcomesalive.mca.core.MCA;
import com.minecraftcomesalive.mca.entity.data.ParentPair;
import com.minecraftcomesalive.mca.enums.EnumAgeState;
import com.minecraftcomesalive.mca.enums.EnumGender;
import net.minecraft.entity.merchant.villager.VillagerData;

public class VillagerFactory {
    private final CWorld world;
    private final EntityVillagerMCA villager;
    private boolean isNameSet;
    private boolean isProfessionSet;
    private boolean isTextureSet;
    private boolean isGenderSet;
    private boolean isPositionSet;
    private boolean isAgeSet;
    private boolean isLevelSet;

    private VillagerFactory(CWorld world) {
        this.world = world;
        this.villager = new EntityVillagerMCA(MCA.ENTITYTYPE_VILLAGER.get(), world.getMcWorld());
    }

    public static VillagerFactory newVillager(CWorld world) {
        return new VillagerFactory(world);
    }

    public VillagerFactory withGender(EnumGender gender) {
        villager.setGender(gender);
        isGenderSet = true;
        return this;
    }

    public VillagerFactory withProfession(CVillagerProfession prof) {
        VillagerData data = villager.getVillagerData();
        villager.setVillagerData(new VillagerData(data.getType(), prof.getMcProfession(), 0));
        isProfessionSet = true;
        return this;
    }

    public VillagerFactory withProfession(CVillagerProfession prof, int level) {
        VillagerData data = villager.getVillagerData();
        villager.setVillagerData(new VillagerData(data.getType(), prof.getMcProfession(), level));
        isProfessionSet = true;
        isLevelSet = true;
        return this;
    }
    public VillagerFactory withName(String name) {
        villager.setVillagerName(name);
        isNameSet = true;
        return this;
    }

    public VillagerFactory withParents(ParentPair parents) {
        villager.setParents(parents);
        return this;
    }

    public VillagerFactory withPosition(double posX, double posY, double posZ) {
        isPositionSet = true;
        villager.setPosition(posX, posY, posZ);
        return this;
    }

    public VillagerFactory withPosition(CEntity entity) {
        isPositionSet = true;
        villager.setPosition(entity.getPosX(), entity.getPosY(), entity.getPosZ());
        return this;
    }

    public VillagerFactory withPosition(CPos pos) {
        isPositionSet = true;
        villager.setPosition(pos.getX(), pos.getY(), pos.getZ());
        return this;
    }

    public VillagerFactory withAge(EnumAgeState age) {
        villager.setAgeState(age);
        isAgeSet = true;
        return this;
    }

    public VillagerFactory withTexture(String texture) {
        villager.setTexture(texture);
        isTextureSet = true;
        return this;
    }

    public VillagerFactory spawn() {
        if (!isPositionSet) {
            MCA.log("Attempted to spawn villager without a position being set!");
        }

        world.spawnEntity(CEntity.fromMC(build()));
        return this;
    }

    public EntityVillagerMCA build() {
        if (!isGenderSet) {
            villager.setGender(EnumGender.getRandom(world.rand));
        }

        if (!isNameSet) {
            villager.setVillagerName(API.getRandomName(villager.getGender()));
        }

        if (!isProfessionSet) {
            VillagerData data = villager.getVillagerData();
            villager.setVillagerData(new VillagerData(data.getType(), API.randomProfession().getMcProfession(), data.getLevel()));
        }

        if (!isLevelSet) {
            VillagerData data = villager.getVillagerData();
            villager.setVillagerData(new VillagerData(data.getType(), data.getProfession(), 0));
        }

        if (!isTextureSet) {
            villager.setTexture(API.getRandomSkin(villager));
        }

        if (!isAgeSet) {
            villager.setAgeState(EnumAgeState.ADULT);
        }

        return villager;
    }
}