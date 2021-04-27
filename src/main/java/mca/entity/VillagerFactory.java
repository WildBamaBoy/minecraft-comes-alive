package mca.entity;

import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.merchant.villager.CVillagerProfession;
import cobalt.minecraft.util.math.CPos;
import cobalt.minecraft.world.CWorld;
import mca.api.API;
import mca.core.MCA;
import mca.entity.data.ParentPair;
import mca.enums.EnumAgeState;
import mca.enums.EnumGender;
import net.minecraft.entity.merchant.villager.VillagerData;

public class VillagerFactory {
    private final CWorld world;
    private final EntityVillagerMCA villager;
    private boolean isNameSet;
    private boolean isProfessionSet;
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
        villager.gender.set(gender.getId());
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
        villager.villagerName.set(name);
        isNameSet = true;
        return this;
    }

    public VillagerFactory withParents(ParentPair parents) {
        villager.parents.set(parents.toNBT());
        return this;
    }

    public VillagerFactory withPosition(double posX, double posY, double posZ) {
        isPositionSet = true;
        villager.setPos(posX, posY, posZ);
        return this;
    }

    public VillagerFactory withPosition(CEntity entity) {
        isPositionSet = true;
        villager.setPos(entity.getPosX(), entity.getPosY(), entity.getPosZ());
        return this;
    }

    public VillagerFactory withPosition(CPos pos) {
        isPositionSet = true;
        villager.setPos(pos.getX(), pos.getY(), pos.getZ());
        return this;
    }

    public VillagerFactory withAge(EnumAgeState age) {
        villager.ageState.set(age.getId());
        isAgeSet = true;
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
            villager.gender.set(EnumGender.getRandom().getId());
        }

        if (!isNameSet) {
            villager.villagerName.set(API.getRandomName(EnumGender.byId(villager.gender.get())));
        }

        if (!isProfessionSet) {
            VillagerData data = villager.getVillagerData();
            villager.setVillagerData(new VillagerData(data.getType(), API.randomProfession().getMcProfession(), data.getLevel()));
        }

        if (!isLevelSet) {
            VillagerData data = villager.getVillagerData();
            villager.setVillagerData(new VillagerData(data.getType(), data.getProfession(), 0));
        }

        if (!isAgeSet) {
            villager.ageState.set(EnumAgeState.ADULT.getId());
        }

        return villager;
    }
}