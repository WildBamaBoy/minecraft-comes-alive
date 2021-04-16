package mca.items;

import com.google.common.base.Optional;
import mca.api.API;
import mca.core.Constants;
import mca.core.Localizer;
import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.ParentData;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumAgeState;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemBaby extends Item {
    private final boolean isMale;

    public ItemBaby(boolean isMale) {
        this.isMale = isMale;
        this.setMaxStackSize(1);
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int unknownInt, boolean unknownBoolean) {
        super.onUpdate(itemStack, world, entity, unknownInt, unknownBoolean);

        if (!world.isRemote) {
            if (!itemStack.hasTagCompound()) {
                NBTTagCompound compound = new NBTTagCompound();

                compound.setString("name", "");
                compound.setInteger("age", 0);
                compound.setUniqueId("ownerUUID", entity.getUniqueID());
                compound.setString("ownerName", entity.getName());
                compound.setBoolean("isInfected", false);

                itemStack.setTagCompound(compound);
            } else {
                updateBabyGrowth(itemStack);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        BlockPos pos = player.getPosition();
        ItemStack stack = player.getHeldItem(hand);
        int posX = pos.getX();
        int posY = pos.getY() + 1;
        int posZ = pos.getZ();

        // Right-clicking an unnamed baby allows you to name it
        if (world.isRemote && getBabyName(stack).equals(""))
            player.openGui(MCA.getInstance(), Constants.GUI_ID_NAMEBABY, player.world, player.getEntityId(), 0, 0);

        if (!world.isRemote) {
            if (isReadyToGrowUp(stack) && !getBabyName(stack).equals("")) { // Name is good and we're ready to grow
                EntityVillagerMCA child = new EntityVillagerMCA(world, Optional.of(ProfessionsMCA.child), Optional.of(this.isMale ? EnumGender.MALE : EnumGender.FEMALE));
                child.set(EntityVillagerMCA.VILLAGER_NAME, getBabyName(stack));
                child.set(EntityVillagerMCA.TEXTURE, API.getRandomSkin(child)); // allow for special-case skins to be applied with the proper name attached to the child at this point
                child.set(EntityVillagerMCA.AGE_STATE, EnumAgeState.BABY.getId());
                child.setStartingAge(MCA.getConfig().childGrowUpTime * 60 * 20 * -1);
                child.setScaleForAge(true);
                child.setPosition(posX, posY, posZ);
                world.spawnEntity(child);

                PlayerSaveData playerData = PlayerSaveData.get(player);
                child.set(EntityVillagerMCA.PARENTS, ParentData.create(player.getUniqueID(), playerData.getSpouseUUID(), player.getName(), playerData.getSpouseName()).toNBT());
                player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                playerData.setBabyPresent(false);

                // set proper dialogue type
                child.getPlayerHistoryFor(player.getUniqueID()).setDialogueType(EnumDialogueType.CHILDP);
            }
        }

        return super.onItemRightClick(world, player, hand);
    }

    private String getBabyName(ItemStack stack) {
        return stack.getTagCompound().getString("name");
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if (!entityItem.world.isRemote) {
            updateBabyGrowth(entityItem.getItem());
        }

        return super.onEntityItemUpdate(entityItem);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        Localizer loc = MCA.getLocalizer();

        if (stack.hasTagCompound()) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            NBTTagCompound nbt = stack.getTagCompound();
            String textColor = ((ItemBaby) stack.getItem()).isMale ? Constants.Color.AQUA : Constants.Color.LIGHTPURPLE;
            int ageInMinutes = nbt.getInteger("age");
            String ownerName = nbt.getUniqueId("ownerUUID").equals(player.getUniqueID()) ? MCA.getLocalizer().localize("label.you") : nbt.getString("ownerName");

            if (getBabyName(stack).equals(""))
                tooltip.add(textColor + loc.localize("gui.label.name") + " " + Constants.Format.RESET + MCA.getLocalizer().localize("label.unnamed"));
            else
                tooltip.add(textColor + loc.localize("gui.label.name") + " " + Constants.Format.RESET + nbt.getString("name"));

            tooltip.add(textColor + loc.localize("gui.label.age") + " " + Constants.Format.RESET + ageInMinutes + " " + (ageInMinutes == 1 ? loc.localize("gui.label.minute") : loc.localize("gui.label.minutes")));
            tooltip.add(textColor + loc.localize("gui.label.parent") + " " + Constants.Format.RESET + ownerName);

            if (nbt.getBoolean("isInfected")) tooltip.add(Constants.Color.GREEN + loc.localize("gui.label.infected"));
            if (isReadyToGrowUp(stack)) tooltip.add(Constants.Color.GREEN + loc.localize("gui.label.readytogrow"));
            if (nbt.getString("name").equals(loc.localize("gui.label.unnamed"))) tooltip.add(Constants.Color.YELLOW + loc.localize("gui.label.rightclicktoname"));
        }
    }

    private void updateBabyGrowth(ItemStack itemStack) {
        if (itemStack.hasTagCompound() && FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % 1200 == 0) {
            int age = itemStack.getTagCompound().getInteger("age");
            age++;
            itemStack.getTagCompound().setInteger("age", age);
        }
    }

    private boolean isReadyToGrowUp(ItemStack itemStack) {
        return itemStack.getTagCompound().getInteger("age") >= MCA.getConfig().babyGrowUpTime;
    }

    public EnumGender getGender() {
        return isMale ? EnumGender.MALE : EnumGender.FEMALE;
    }
}