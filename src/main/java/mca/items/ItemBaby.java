package mca.items;

import cobalt.enums.CEnumHand;
import cobalt.items.CItem;
import cobalt.minecraft.entity.CEntity;
import cobalt.minecraft.entity.player.CPlayer;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.item.CItemUseContext;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.util.math.CPos;
import cobalt.minecraft.world.CWorld;
import com.google.common.base.Optional;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.ParentPair;
import mca.entity.data.PlayerSaveData;
import mca.enums.EnumAgeState;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;

public class ItemBaby extends CItem {
    private final boolean isMale;

    public ItemBaby(Item.Properties properties) {
        super(properties);
        isMale = false;
    }

    @Override
    public ActionResultType update(CItemStack itemStack, CWorld world, CEntity entity) {
        if (!world.isRemote) {
            if (!itemStack.hasTag()) {
                CNBT compound = CNBT.createNew();

                compound.setString("name", "");
                compound.setInteger("age", 0);
                compound.setUUID("ownerUUID", entity.getUUID());
                compound.setString("ownerName", entity.getName());
                compound.setBoolean("isInfected", false);

                itemStack.setTag(compound);
            } else {
                updateBabyGrowth(itemStack);
            }
        }
        return null;
    }

    @Override
    public ActionResult<ItemStack> handleRightClick(CWorld world, CPlayer player, CEnumHand hand) {
        CPos pos = player.getPosition();
        CItemStack stack = player.getHeldItem(hand);
        int posX = pos.getX();
        int posY = pos.getY() + 1;
        int posZ = pos.getZ();

        // Right-clicking an unnamed baby allows you to name it
        if (world.isRemote && getBabyName(stack).equals("")) {
//            player.openGui(MCA.getInstance(), Constants.GUI_ID_NAMEBABY, player.world, player.getEntityId(), 0, 0);
        }
        if (!world.isRemote) {
            if (isReadyToGrowUp(stack) && !getBabyName(stack).equals("")) { // Name is good and we're ready to grow
                EntityVillagerMCA child = new EntityVillagerMCA(MCA.ENTITYTYPE_VILLAGER.get(), world);
                child.gender.set((this.isMale ? EnumGender.MALE : EnumGender.FEMALE).getId());
                child.setProfession(MCA.PROFESSION_CHILD.get());
                child.villagerName.set(getBabyName(stack));
                child.setBaby(true);
                child.setPos(posX, posY, posZ);

                PlayerSaveData playerData = PlayerSaveData.get(world, player.getUUID());

                //assumes your child is from the players current spouse
                //as the father does not have any genes it just takes the one from the mother
                Optional<Entity> spouse = Util.getEntityByUUID(player.getWorld(), playerData.getSpouseUUID());
                if (spouse.isPresent() && spouse.get() instanceof EntityVillagerMCA) {
                    EntityVillagerMCA spouseVillager = (EntityVillagerMCA) spouse.get();
                    child.inheritGenes(spouseVillager, spouseVillager);
                }

                child.parents.set(ParentPair.create(player.getUUID(), playerData.getSpouseUUID(), player.getName(), playerData.getSpouseName()).toNBT());

                world.spawnEntity(CEntity.fromMC(child));

                player.getHeldItem(hand).decrStackSize();
                playerData.setBabyPresent(false);

                // set proper dialogue type
                child.getMemoriesForPlayer(player).setDialogueType(EnumDialogueType.CHILDP);
            }
        }

        return null;
    }

    @Override
    public ActionResultType handleUseOnBlock(CItemUseContext context) {
        return null;
    }

    private String getBabyName(CItemStack stack) {
        return stack.getTag().getString("name");
    }

//    @OnlyIn(Dist.CLIENT)
//    @Override
//    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
//        super.addInformation(stack, worldIn, tooltip, flagIn);
//        Localizer loc = MCA.getLocalizer();
//
//        if (stack.hasTagCompound()) {
//            CPlayer player = Minecraft.getMinecraft().player;
//            CNBT nbt = stack.getTagCompound();
//            String textColor = ((ItemBaby) stack.getItem()).isMale ? Constants.Color.AQUA : Constants.Color.LIGHTPURPLE;
//            int ageInMinutes = nbt.getInteger("age");
//            String ownerName = nbt.getUUID("ownerUUID").equals(player.getUUID()) ? MCA.localize("label.you") : nbt.getString("ownerName");
//
//            if (getBabyName(stack).equals(""))
//                tooltip.add(textColor + loc.localize("gui.label.name") + " " + Constants.Format.RESET + MCA.localize("label.unnamed"));
//            else
//                tooltip.add(textColor + loc.localize("gui.label.name") + " " + Constants.Format.RESET + nbt.getString("name"));
//
//            tooltip.add(textColor + loc.localize("gui.label.age") + " " + Constants.Format.RESET + ageInMinutes + " " + (ageInMinutes == 1 ? loc.localize("gui.label.minute") : loc.localize("gui.label.minutes")));
//            tooltip.add(textColor + loc.localize("gui.label.parent") + " " + Constants.Format.RESET + ownerName);
//
//            if (nbt.getBoolean("isInfected")) tooltip.add(Constants.Color.GREEN + loc.localize("gui.label.infected"));
//            if (isReadyToGrowUp(stack)) tooltip.add(Constants.Color.GREEN + loc.localize("gui.label.readytogrow"));
//            if (nbt.getString("name").equals(loc.localize("gui.label.unnamed")))
//                tooltip.add(Constants.Color.YELLOW + loc.localize("gui.label.rightclicktoname"));
//        }
//    }

    private void updateBabyGrowth(CItemStack itemStack) {
        int tick = 1;
        if (itemStack.hasTag() && tick % 1200 == 0) {
            int age = itemStack.getTag().getInteger("age");
            age++;
            itemStack.getTag().setInteger("age", age);
        }
    }

    private boolean isReadyToGrowUp(CItemStack itemStack) {
        return itemStack.getTag().getInteger("age") >= MCA.getConfig().babyGrowUpTime;
    }

    public EnumGender getGender() {
        return isMale ? EnumGender.MALE : EnumGender.FEMALE;
    }
}