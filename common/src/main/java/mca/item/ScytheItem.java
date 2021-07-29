package mca.item;

import java.util.Random;

import mca.SoundsMCA;
import mca.TagsMCA;
import mca.block.TombstoneBlock;
import mca.entity.EntitiesMCA;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ScytheItem extends SwordItem {

    public ScytheItem(Settings settings) {
        super(ToolMaterials.GOLD, 10, -2.4F, settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        LivingEntity living = (LivingEntity)entity;
        boolean active = stack.getOrCreateTag().getBoolean("active");

        Random r = entity.world.random;

        if (active != selected) {
            stack.getOrCreateTag().putBoolean("active", selected);

            float baseVolume = selected ? 0.75F : 0.25F;
            entity.world.playSound(null, entity.getBlockPos(), SoundsMCA.reaper_scythe_out, entity.getSoundCategory(),
                    baseVolume + r.nextFloat() / 2F,
                    0.65F + r.nextFloat() / 10F
            );
        }

        if (selected) {
            if (living.handSwingTicks == -1) {
                entity.world.playSound(null, entity.getBlockPos(), SoundsMCA.reaper_scythe_swing, entity.getSoundCategory(), 0.25F, 1);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (hasSoul(context.getStack())) {
            setSoul(context.getStack(), false);
            ActionResult result = use(context, false);
            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return super.hasGlint(stack) || hasSoul(stack);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.world.random.nextInt(50) > 40) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 1000, 1));
        }

        SoundEvent sound = SoundsMCA.reaper_scythe_out;

        if (!hasSoul(stack) && target.isDead() && (target.getType() == EntitiesMCA.MALE_VILLAGER || target.getType() == EntitiesMCA.FEMALE_VILLAGER)) {
            setSoul(stack, true);
            sound = SoundEvents.BLOCK_BELL_RESONATE;
        }

        Random r = attacker.world.random;
        attacker.world.playSound(null, attacker.getBlockPos(), sound, attacker.getSoundCategory(),
                0.75F + r.nextFloat() / 2F,
                0.75F + r.nextFloat() / 2F
        );

        return super.postHit(stack, target, attacker);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return stack.getItem() == ingredient.getItem();
    }

    public static void setSoul(ItemStack stack, boolean soul) {
        stack.getOrCreateTag().putBoolean("hasSoul", soul);
    }

    public static boolean hasSoul(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("hasSoul");
    }

    public static ActionResult use(ItemUsageContext context, boolean cure) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.isIn(TagsMCA.Blocks.TOMBSTONES)) {
            return TombstoneBlock.Data.of(world.getBlockEntity(pos)).filter(TombstoneBlock.Data::hasEntity).map(data -> {
                if (!world.isClient) {
                    data.startResurrecting(cure);
                }
                return ActionResult.SUCCESS;
            }).orElse(ActionResult.FAIL);
        }
        return ActionResult.PASS;
    }
}
