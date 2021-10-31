package mca.network;

import java.util.UUID;
import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerLike;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.family.FamilyTree;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.resources.ClothingList;
import mca.resources.HairList;
import mca.resources.data.Hair;
import mca.server.world.data.PlayerSaveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;

public class VillagerEditorSyncRequest extends S2CNbtDataMessage {
    private final String command;
    private final UUID uuid;

    public VillagerEditorSyncRequest(String command, UUID uuid, NbtCompound data) {
        super(data);
        this.command = command;
        this.uuid = uuid;
    }

    @Override
    public void receive(PlayerEntity e) {
        Entity entity = ((ServerWorld)e.world).getEntity(uuid);
        NbtCompound villagerData;
        switch (command) {
            case "hair":
                villagerData = GetVillagerRequest.getVillagerData(entity);
                if (villagerData != null) {
                    // fetch hair
                    Hair hair = HairList.getInstance().pickNext(getGender(villagerData), new Hair(
                            villagerData.getString("hair"),
                            villagerData.getString("hairOverlay")
                    ), getData().getInt("offset"));

                    // set
                    if (entity instanceof PlayerEntity) {
                        villagerData.putString("hair", hair.texture());
                        villagerData.putString("hairOverlay", hair.overlay());
                        saveEntity(entity, villagerData);
                    } else if (entity instanceof VillagerLike) {
                        VillagerLike<?> villager = (VillagerLike<?>)entity;
                        villager.setHair(hair);
                    }
                }
                break;
            case "clothing":
                villagerData = GetVillagerRequest.getVillagerData(entity);
                if (villagerData != null) {
                    if (entity instanceof PlayerEntity) {
                        String clothes = ClothingList.getInstance().getPool(getGender(villagerData), VillagerProfession.NONE).pickNext(villagerData.getString("clothes"), getData().getInt("offset"));
                        villagerData.putString("clothes", clothes);
                        saveEntity(entity, villagerData);
                    } else if (entity instanceof VillagerLike) {
                        VillagerLike<?> villager = (VillagerLike<?>)entity;
                        String clothes = ClothingList.getInstance().getPool(villager).pickNext(villager.getClothes(), getData().getInt("offset"));
                        villager.setClothes(clothes);
                    }
                }
                break;
            case "sync":
                saveEntity(entity, getData());
                break;
            case "profession":
                if (entity instanceof VillagerEntityMCA) {
                    VillagerLike<?> villager = (VillagerLike<?>)entity;
                    VillagerProfession profession = Registry.VILLAGER_PROFESSION.get(new Identifier(getData().getString("profession")));
                    ((VillagerEntityMCA)villager).setProfession(profession);
                }
                break;
        }
        getData();
    }

    private void saveEntity(Entity entity, NbtCompound villagerData) {
        if (entity instanceof PlayerEntity) {
            PlayerSaveData data = PlayerSaveData.get((ServerWorld)entity.world, uuid);
            data.setEntityData(villagerData);
            data.setEntityDataSet(true);
            syncFamilyTree(entity);
        } else if (entity instanceof VillagerLike) {
            ((LivingEntity)entity).readCustomDataFromNbt(villagerData);
            entity.calculateDimensions();
            syncFamilyTree(entity);
        }
    }

    private Gender getGender(NbtCompound villagerData) {
        return Gender.byId(villagerData.getInt("gender"));
    }

    private void syncFamilyTree(Entity entity) {
        FamilyTreeNode entry = FamilyTree.get((ServerWorld)entity.world).getOrCreate(entity);
        entry.setGender(getGender(getData()));
        entry.setName(getData().getString("villagerName"));
    }
}
