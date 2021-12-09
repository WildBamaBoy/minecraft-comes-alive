package mca.network;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerLike;
import mca.entity.ai.Messenger;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
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
                    villagerData.putString("hair", hair.texture());
                    villagerData.putString("hairOverlay", hair.overlay());
                    saveEntity(e, entity, villagerData);
                }
                break;
            case "clothing":
                villagerData = GetVillagerRequest.getVillagerData(entity);
                if (villagerData != null) {
                    String clothes = "";
                    if (entity instanceof PlayerEntity) {
                        clothes = ClothingList.getInstance().getPool(getGender(villagerData), VillagerProfession.NONE).pickNext(villagerData.getString("clothes"), getData().getInt("offset"));
                    } else if (entity instanceof VillagerLike) {
                        VillagerLike<?> villager = (VillagerLike<?>)entity;
                        clothes = ClothingList.getInstance().getPool(villager).pickNext(villager.getClothes(), getData().getInt("offset"));
                    }
                    villagerData.putString("clothes", clothes);
                    saveEntity(e, entity, villagerData);
                }
                break;
            case "sync":
                saveEntity(e, entity, getData());
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

    private void saveEntity(PlayerEntity e, Entity entity, NbtCompound villagerData) {
        if (entity instanceof PlayerEntity) {
            PlayerSaveData data = PlayerSaveData.get((ServerWorld)entity.world, uuid);
            data.setEntityData(villagerData);
            data.setEntityDataSet(true);
            syncFamilyTree(e, entity, villagerData);
        } else if (entity instanceof VillagerLike) {
            ((LivingEntity)entity).readCustomDataFromNbt(villagerData);
            entity.calculateDimensions();
            syncFamilyTree(e, entity, villagerData);
        }
    }

    private Gender getGender(NbtCompound villagerData) {
        return Gender.byId(villagerData.getInt("gender"));
    }

    private void syncFamilyTree(PlayerEntity e, Entity entity, NbtCompound villagerData) {
        FamilyTree tree = FamilyTree.get((ServerWorld)entity.world);
        FamilyTreeNode entry = tree.getOrCreate(entity);
        entry.setGender(getGender(getData()));
        entry.setName(getData().getString("villagerName"));

        //todo convert that to getUUID and make a simple UUID setter for parents (since they are enforced anyways)
        for (String who : new String[] {"father", "mother"}) {
            String name = villagerData.getString("tree_" + who + "_new");
            if (villagerData.contains("tree_" + who + "_new")) {
                try {
                    UUID uuid = UUID.fromString(name);
                    Optional<FamilyTreeNode> father = tree.getOrEmpty(uuid);
                    if (father.isPresent()) {
                        entry.assignParent(father.get());
                    } else {
                        e.sendMessage(new TranslatableText("gui.villager_editor.uuid_unknown", name), false);
                    }
                } catch (IllegalArgumentException exception) {
                    List<FamilyTreeNode> nodes = tree.getAllWithName(name).collect(Collectors.toList());
                    if (nodes.isEmpty()) {
                        //todo create a new entry
                        e.sendMessage(new TranslatableText("gui.villager_editor.name_unknown", name), false);
                    } else {
                        entry.assignParent(nodes.get(0));

                        if (nodes.size() > 1) {
                            e.sendMessage(new TranslatableText("gui.villager_editor.name_not_unique", name), false);
                        }
                    }
                }
            }
        }
    }
}
