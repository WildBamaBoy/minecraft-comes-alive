package mca.entity.ai;

import mca.Config;
import mca.CriterionMCA;
import mca.entity.Status;
import mca.entity.VillagerEntityMCA;
import mca.item.ItemsMCA;
import mca.item.SpecialCaseGift;
import mca.resources.API;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CParameter;
import net.minecraft.block.SpongeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import java.util.*;

/**
 * I know you, you know me, we're all a big happy family.
 */
public class BreedableRelationship extends Relationship<VillagerEntityMCA> {

    private static final CDataParameter<Boolean> IS_PROCREATING = CParameter.create("isProcreating", false);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        return Relationship.createTrackedData(builder)
                .addAll(IS_PROCREATING)
                .add(Pregnancy::createTrackedData);
    }

    private int procreateTick = -1;

    private final Pregnancy pregnancy;

    public BreedableRelationship(VillagerEntityMCA entity) {
        super(entity);
        pregnancy = new Pregnancy(entity);
    }

    public Pregnancy getPregnancy() {
        return pregnancy;
    }

    public boolean isProcreating() {
        return entity.getTrackedValue(IS_PROCREATING);
    }

    public void startProcreating() {
        procreateTick = 60;
        entity.setTrackedValue(IS_PROCREATING, true);
    }

    public void tick(int age) {
        if (age % 20 == 0) {
            pregnancy.tick();
        }

        if (!isProcreating()) {
            return;
        }

        Random random = entity.getRandom();
        if (procreateTick > 0) {
            procreateTick--;
            entity.getNavigation().stop();
            entity.world.sendEntityStatus(entity, Status.VILLAGER_HEARTS);
        } else {
            // TODO: Move this to the Pregnancy
            //make sure this villager is registered in the family tree
            getFamilyTree().getOrCreate(entity);
            getSpouse().ifPresent(spouse -> {
                boolean areTwins = random.nextInt(100) < Config.getInstance().chanceToHaveTwins;
                int count = areTwins ? 2 : 1;

                // advancement
                if (spouse instanceof ServerPlayerEntity) {
                    CriterionMCA.BABY_CRITERION.trigger((ServerPlayerEntity) spouse, count);
                }

                for (int i = 0; i < count; i++) {
                    ItemStack stack = (random.nextBoolean() ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL).getDefaultStack();
                    if (!(spouse instanceof PlayerEntity && ((PlayerEntity) spouse).giveItemStack(stack))) {
                        entity.getInventory().addStack(stack);
                    }
                }
            });

            entity.setTrackedValue(IS_PROCREATING, false);
        }
    }

    public void giveGift(ServerPlayerEntity player, Memories memory) {
        ItemStack stack = player.getMainHandStack();

        if (!stack.isEmpty()) {
            int giftValue = API.getGiftPool().getWorth(stack);
            if (!handleSpecialCaseGift(player, stack)) {
                if (stack.getItem() == Items.GOLDEN_APPLE) {
                    //TODO special
                    entity.setInfected(false);
                } else if (stack.getItem() instanceof DyeItem) {
                    //TODO special
                    DyeItem dye = (DyeItem) stack.getItem();
                    entity.setHairDye(dye.getColor());
                } else if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof SpongeBlock) {
                    //TODO special, also feels super hacky, probably a better way to check for blocks
                    entity.clearHairDye();
                } else {
                    // TODO: Don't use translation keys. Use identifiers.
                    String id = stack.getTranslationKey();
                    long occurrences = giftDesaturation.stream().filter(id::equals).count();



                    //check if desaturation fail happen
                    if (entity.getRandom().nextInt(100) < occurrences * Config.getInstance().giftDesaturationPenalty) {
                        giftValue = -giftValue / 2;
                        entity.sendChatMessage(player, API.getGiftPool().getResponseForSaturatedGift(stack));
                    } else {
                        entity.sendChatMessage(player, API.getGiftPool().getResponse(stack));
                    }

                    //modify mood and hearts
                    entity.getVillagerBrain().modifyMoodLevel(giftValue / 2 + 2 * MathHelper.sign(giftValue));
                    memory.modHearts(giftValue);
                }
            }

            //add to desaturation queue
            giftDesaturation.add(stack.getTranslationKey());
            while (giftDesaturation.size() > Config.getInstance().giftDesaturationQueueLength) {
                giftDesaturation.remove(0);
            }

            //particles
            if (giftValue > 0) {
                player.getMainHandStack().decrement(1);
                entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_POS_INTERACTION);
            } else {
                entity.world.sendEntityStatus(entity, Status.MCA_VILLAGER_NEG_INTERACTION);
            }
        }
    }

    private boolean handleSpecialCaseGift(ServerPlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof SpecialCaseGift) {
            if (((SpecialCaseGift) item).handle(player, entity)) {
                player.getMainHandStack().decrement(1);
            }
            return true;
        } else if (item == Items.CAKE) {
            if (isMarried() && !entity.isBaby()) {
                if (pregnancy.tryStartGestation()) {
                    ((ServerWorld) player.world).sendEntityStatus(entity, Status.VILLAGER_HEARTS);
                    entity.sendChatMessage(player, "gift.cake.success");
                } else {
                    entity.sendChatMessage(player, "gift.cake.fail");
                }
                return true;
            }
        } else if (item == Items.GOLDEN_APPLE && entity.isBaby()) {
            // increase age by 5 minutes
            entity.growUp(1200 * 5);
            return true;
        }

        return false;
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        super.readFromNbt(nbt);
        //load gift desaturation queue
        NbtList res = nbt.getList("giftDesaturation", 8);
        for (int i = 0; i < res.size(); i++) {
            String c = res.getString(i);
            giftDesaturation.add(c);
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        super.writeToNbt(nbt);
        //save gift desaturation queue
        NbtList giftDesaturationQueue = new NbtList();
        for (int i = 0; i < giftDesaturation.size(); i++) {
            giftDesaturationQueue.addElement(i, NbtString.of(giftDesaturation.get(i)));
        }
        nbt.put("giftDesaturation", giftDesaturationQueue);
    }
}
