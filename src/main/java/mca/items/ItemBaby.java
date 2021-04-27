package mca.items;

import com.google.common.base.Optional;
import mca.core.Constants;
import mca.core.Localizer;
import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.ParentPair;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumAgeState;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import cobalt.minecraft.entity.player.CPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cobalt.minecraft.nbt.CNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.CEnumHand;
import cobalt.minecraft.util.math.CPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static mca.entity.EntityVillagerMCA.SPOUSE_UUID;

public class ItemBaby extends Item {
    private final boolean isMale;

    public ItemBaby(Properties properties) {
        super(properties);
        isMale = false;
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int unknownInt, boolean unknownBoolean) {
        super.onUpdate(itemStack, world, entity, unknownInt, unknownBoolean);

        if (!world.isRemote) {
            if (!itemStack.hasTagCompound()) {
                CNBT compound = new CNBT();

                compound.setString("name", "");
                compound.setInteger("age", 0);
                compound.setUUID("ownerUUID", entity.getUUID());
                compound.setString("ownerName", entity.getName().getString());
                compound.setBoolean("isInfected", false);

                itemStack.setTagCompound(compound);
            } else {
                updateBabyGrowth(itemStack);
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, CPlayer player, CEnumHand hand) {
        CPos pos = player.getPosition();
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
                child.set(EntityVillagerMCA.villagerName, getBabyName(stack));
                child.set(EntityVillagerMCA.ageState, EnumAgeState.BABY.getId());
                child.setStartingAge(MCA.getConfig().childGrowUpTime * 60 * 20 * -1);
                child.setScaleForAge(true);
                child.setPosition(posX, posY, posZ);

                PlayerSaveData playerData = PlayerSaveData.get(player);

                //assumes your child is from the players current spouse
                //as the father does not have any genes it just takes the one from the mother
                Optional<Entity> spouse = Util.getEntityByUUID(player.world, playerData.getSpouseUUID());
                if (spouse.isPresent() && spouse.get() instanceof EntityVillagerMCA) {
                    EntityVillagerMCA spouseVillager = (EntityVillagerMCA) spouse.get();
                    child.inheritGenes(spouseVillager, spouseVillager);
                }

                child.set(EntityVillagerMCA.parents, ParentPair.create(player.getUUID(), playerData.getSpouseUUID(), player.getName(), playerData.getSpouseName()).toNBT());

                world.spawnEntity(child);

                player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                playerData.setBabyPresent(false);

                // set proper dialogue type
                child.getMemoriesFor(player.getUUID()).setDialogueType(EnumDialogueType.CHILDP);
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        Localizer loc = MCA.getLocalizer();

        if (stack.hasTagCompound()) {
            CPlayer player = Minecraft.getMinecraft().player;
            CNBT nbt = stack.getTagCompound();
            String textColor = ((ItemBaby) stack.getItem()).isMale ? Constants.Color.AQUA : Constants.Color.LIGHTPURPLE;
            int ageInMinutes = nbt.getInteger("age");
            String ownerName = nbt.getUUID("ownerUUID").equals(player.getUUID()) ? MCA.localize("label.you") : nbt.getString("ownerName");

            if (getBabyName(stack).equals(""))
                tooltip.add(textColor + loc.localize("gui.label.name") + " " + Constants.Format.RESET + MCA.localize("label.unnamed"));
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