package mca.items;

import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.network.NetworkHandler;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.TagsMCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.data.ItemTagsProviderMCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.ParentPair;
import mca.entity.data.PlayerSaveData;
import mca.enums.DialogueType;
import mca.enums.Gender;
import mca.network.OpenGuiRequest;
import mca.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class BabyItem extends Item {
    public int tick = -1;

    public BabyItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World world, Entity entity, int pos, boolean selected) {
        if (!world.isClientSide) {
            if (!itemStack.hasTag()) {
                CNBT compound = CNBT.createNew();

                compound.setString("name", "");
                compound.setInteger("age", 0);
                compound.setUUID("ownerUUID", entity.getUUID());
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
    public final ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        BlockPos pos = player.blockPosition();
        ItemStack stack = player.getItemInHand(hand);
        int posX = pos.getX();
        int posY = pos.getY() + 1;
        int posZ = pos.getZ();

        // Right-clicking an unnamed baby allows you to name it
        if (getBabyName(stack).equals("")) {
            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.gui.BABY_NAME), (ServerPlayerEntity) player);
            }
        }

        if (!world.isClientSide && isReadyToGrowUp(stack) && !getBabyName(stack).equals("")) { // Name is good and we're ready to grow
            VillagerEntityMCA child = new VillagerEntityMCA(world);
            child.gender.set((getGender()).getId());
            child.setProfession(ProfessionsMCA.CHILD);
            child.villagerName.set(getBabyName(stack));
            child.setBaby(true);
            child.setPos(posX, posY, posZ);

            PlayerSaveData playerData = PlayerSaveData.get(world, player.getUUID());

            //assumes your child is from the players current spouse
            //as the father does not have any genes it just takes the one from the mother
            Entity spouse = ((ServerWorld) world).getEntity(playerData.getSpouseUUID());
            if (spouse instanceof VillagerEntityMCA) {
                VillagerEntityMCA spouseVillager = (VillagerEntityMCA) spouse;
                child.inheritGenes(spouseVillager, spouseVillager);
            }

            child.parents.set(ParentPair.create(player.getUUID(), playerData.getSpouseUUID(), player.getName().getString(), playerData.getSpouseName()).toNBT());

            WorldUtils.spawnEntity(world, child);

            player.getItemInHand(hand).shrink(1);
            playerData.setBabyPresent(false);

            // set proper dialogue type
            child.getMemoriesForPlayer(player).setDialogueType(DialogueType.CHILDP);

            stack.shrink(1);

            return ActionResult.success(stack);
        } else {
            return ActionResult.pass(stack);
        }
    }

    public String getBabyName(ItemStack stack) {
        return stack.getTag().getString("name");
    }

    public void setBabyName(ItemStack stack, String name) {
        stack.getTag().putString("name", name);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (stack.hasTag()) {
            PlayerEntity player = Minecraft.getInstance().player;
            CNBT nbt = CNBT.fromMC(stack.getTag());

            String textColor = ((BabyItem) stack.getItem()).getGender() == Gender.MALE ? Constants.Color.AQUA : Constants.Color.LIGHTPURPLE;
            int ageInMinutes = nbt.getInteger("age");
            String ownerName = nbt.getUUID("ownerUUID").equals(player.getUUID()) ? MCA.localize("gui.label.you") : nbt.getString("ownerName");

            if (getBabyName(stack).equals(""))
                tooltip.add(new StringTextComponent(textColor + MCA.localize("gui.label.name") + " " + Constants.Format.RESET + MCA.localize("gui.label.unnamed")));
            else
                tooltip.add(new StringTextComponent(textColor + MCA.localize("gui.label.name") + " " + Constants.Format.RESET + nbt.getString("name")));

            tooltip.add(new StringTextComponent(MCA.localize("gui.label.age") + " " + Constants.Format.RESET + ageInMinutes + " " + (ageInMinutes == 1 ? MCA.localize("gui.label.minute") : MCA.localize("gui.label.minutes"))));
            tooltip.add(new StringTextComponent(MCA.localize("gui.label.parent") + " " + Constants.Format.RESET + ownerName));

            if (nbt.getBoolean("isInfected"))
                tooltip.add(new StringTextComponent(Constants.Color.GREEN + MCA.localize("gui.label.infected")));

            if (isReadyToGrowUp(stack))
                tooltip.add(new StringTextComponent(Constants.Color.GREEN + MCA.localize("gui.label.readytogrow")));

            if (nbt.getString("name").equals(MCA.localize("gui.label.unnamed"))) {
                tooltip.add(new StringTextComponent(Constants.Color.YELLOW + MCA.localize("gui.label.rightclicktoname")));
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

    private boolean isReadyToGrowUp(ItemStack stack) {
        CNBT tag = CNBT.fromMC(stack.getTag());
        return tag != null && tag.getInteger("age") >= MCA.getConfig().babyGrowUpTime;
    }

    public Gender getGender() {
        return this.equals(ItemsMCA.BABY_BOY.get()) ? Gender.MALE : Gender.FEMALE;
    }
}