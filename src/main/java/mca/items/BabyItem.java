package mca.items;

import mca.cobalt.localizer.Localizer;
import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.NetworkHandler;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.FamilyTree;
import mca.entity.data.FamilyTreeEntry;
import mca.entity.data.Memories;
import mca.entity.data.PlayerSaveData;
import mca.enums.DialogueType;
import mca.enums.Gender;
import mca.network.OpenGuiRequest;
import mca.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class BabyItem extends Item {
    public int tick = -1;

    public BabyItem(Item.Settings properties) {
        super(properties);
    }

    public boolean onDropped(ItemStack stack, PlayerEntity player) {
        player.getInventory().insertStack(stack);
        return false;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World world, Entity entity, int pos, boolean selected) {
        if (!world.isClient) {
            if (!itemStack.hasTag()) {
                CNBT compound = CNBT.createNew();

                compound.setString("name", "");
                compound.setInteger("age", 0);
                compound.setUUID("ownerUUID", entity.getUuid());
                compound.setString("ownerName", entity.getName().getString());
                compound.setBoolean("isInfected", false);

                itemStack.setTag(compound.getMcCompound());
            } else {
                updateBabyGrowth(itemStack);
            }
            tick++;
        }
    }

    @Override
    public final TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        BlockPos pos = player.getBlockPos();
        ItemStack stack = player.getStackInHand(hand);
        int posX = pos.getX();
        int posY = pos.getY() + 1;
        int posZ = pos.getZ();

        // Right-clicking an unnamed baby allows you to name it
        if (getBabyName(stack).equals("")) {
            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.gui.BABY_NAME), (ServerPlayerEntity) player);
            }
        }

        if (!world.isClient && isReadyToGrowUp(stack) && !getBabyName(stack).equals("")) { // Name is good and we're ready to grow
            VillagerEntityMCA child = new VillagerEntityMCA(world);

            child.getGenetics().setGender(getGender());
            child.setProfession(ProfessionsMCA.CHILD);
            child.villagerName.set(getBabyName(stack));
            child.setBaby(true);
            child.setPosition(posX, posY, posZ);

            PlayerSaveData playerData = PlayerSaveData.get(world, player.getUuid());

            //make sure both parents are registered in the family tree
            FamilyTree familyTree = child.getFamilyTree();
            familyTree.addEntry(player);

            //assumes your child is from the players current spouse
            //as the father does not have any genes it just takes the one from the mother
            Entity spouse = ((ServerWorld) world).getEntity(playerData.getSpouseUUID());
            if (spouse instanceof VillagerEntityMCA) {
                VillagerEntityMCA spouseVillager = (VillagerEntityMCA) spouse;
                familyTree.addEntry(spouseVillager);
                child.getGenetics().combine(spouseVillager.getGenetics(), spouseVillager.getGenetics());
            }

            //add the child to the family tree
            FamilyTreeEntry spouseEntry = familyTree.getEntry(playerData.getSpouseUUID());

            if (spouseEntry != null && spouseEntry.getGender() == Gender.FEMALE) {
                familyTree.addEntry(child, player.getUuid(), playerData.getSpouseUUID());
            } else {
                familyTree.addEntry(child, playerData.getSpouseUUID(), player.getUuid());
            }

            WorldUtils.spawnEntity(world, child);

            player.getStackInHand(hand).decrement(1);
            playerData.setBabyPresent(false);

            // set proper dialogue type
            Memories memories = child.getMemoriesForPlayer(player);
            memories.setDialogueType(DialogueType.CHILDP);
            memories.setHearts(MCA.getConfig().childInitialHearts);

            stack.decrement(1);

            return TypedActionResult.success(stack);
        } else {
            return TypedActionResult.pass(stack);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext flag) {
        if (stack.hasTag()) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            CNBT nbt = CNBT.fromMC(stack.getTag());

            String textColor = ((BabyItem) stack.getItem()).getGender() == Gender.MALE ? Constants.Color.AQUA : Constants.Color.LIGHTPURPLE;
            int ageInMinutes = nbt.getInteger("age");
            String ownerName = nbt.getUUID("ownerUUID").equals(player.getUuid()) ? Localizer.localize("gui.label.you") : nbt.getString("ownerName");

            if (getBabyName(stack).equals("")) {
                tooltip.add(new LiteralText(textColor + Localizer.localize("gui.label.name") + " " + Constants.Format.RESET + Localizer.localize("gui.label.unnamed")));
            } else {
                tooltip.add(new LiteralText(textColor + Localizer.localize("gui.label.name") + " " + Constants.Format.RESET + nbt.getString("name")));
            }

            tooltip.add(new LiteralText(Localizer.localize("gui.label.age") + " " + Constants.Format.RESET + ageInMinutes + " " + (ageInMinutes == 1 ? Localizer.localize("gui.label.minute") : Localizer.localize("gui.label.minutes"))));
            tooltip.add(new LiteralText(Localizer.localize("gui.label.parent") + " " + Constants.Format.RESET + ownerName));

            if (nbt.getBoolean("isInfected"))
                tooltip.add(new LiteralText(Constants.Color.GREEN + Localizer.localize("gui.label.infected")));

            if (isReadyToGrowUp(stack))
                tooltip.add(new LiteralText(Constants.Color.GREEN + Localizer.localize("gui.label.readytogrow")));

            if (nbt.getString("name").equals(Localizer.localize("gui.label.unnamed"))) {
                tooltip.add(new LiteralText(Constants.Color.YELLOW + Localizer.localize("gui.label.rightclicktoname")));
            }
        }
    }

    private void updateBabyGrowth(ItemStack itemStack) {
        CNBT tag = CNBT.fromMC(itemStack.getTag());
        if (tag != null && tick % 1200 == 0) {
            int age = tag.getInteger("age");
            tag.setInteger("age", age + 1);
        }
    }

    private static boolean isReadyToGrowUp(ItemStack stack) {
        CNBT tag = CNBT.fromMC(stack.getTag());
        return tag != null && tag.getInteger("age") >= MCA.getConfig().babyGrowUpTime;
    }

    public Gender getGender() {
        return this.equals(ItemsMCA.BABY_BOY) ? Gender.MALE : Gender.FEMALE;
    }

    public static String getBabyName(ItemStack stack) {
        return stack.getTag().getString("name");
    }

    public static void setBabyName(ItemStack stack, String name) {
        stack.getTag().putString("name", name);
    }

}