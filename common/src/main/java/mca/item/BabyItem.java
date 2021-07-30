package mca.item;

import com.google.common.base.Strings;
import mca.ClientProxy;
import mca.Config;
import mca.advancement.criterion.CriterionMCA;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerFactory;
import mca.entity.VillagerLike;
import mca.entity.ai.DialogueType;
import mca.entity.ai.Memories;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.network.client.OpenGuiRequest;
import mca.server.world.data.PlayerSaveData;
import mca.util.WorldUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class BabyItem extends Item {

    private final Gender gender;

    public BabyItem(Gender gender, Item.Settings properties) {
        super(properties);
        this.gender = gender;
    }

    public Gender getGender() {
        return gender;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    public boolean onDropped(ItemStack stack, PlayerEntity player) {
        player.sendMessage(new TranslatableText("item.mca.baby.no_drop"), true);
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int pos, boolean selected) {
        if (!world.isClient) {
            if (!stack.hasTag()) {
                NbtCompound compound = stack.getOrCreateTag();

                compound.putString("name", "");
                compound.putInt("age", 0);
                compound.putUuid("ownerUUID", entity.getUuid());
                compound.putString("ownerName", entity.getName().getString());
                compound.putBoolean("isInfected", false);
            }

            if (world.getTime() % 1200 == 0) {
                stack.getTag().putInt("age", stack.getTag().getInt("age") + 1);
            }
        }
    }

    @Override
    public final TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getStackInHand(hand);

        // Right-clicking an unnamed baby allows you to name it
        if (getBabyName(stack).equals("")) {
            if (player instanceof ServerPlayerEntity) {
                NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.BABY_NAME), (ServerPlayerEntity) player);
            }

            return TypedActionResult.pass(stack);
        }

        if (world.isClient || !isReadyToGrowUp(stack)) {
            return TypedActionResult.pass(stack);
        }

        // Name is good and we're ready to grow
        birthChild(getBabyName(stack), (ServerWorld)world, player, getSpouse(stack, Gender.MALE), getSpouse(stack, Gender.FEMALE));
        stack.decrement(1);

        return TypedActionResult.success(stack);
    }

    private void birthChild(String babyName, ServerWorld world, PlayerEntity player, Optional<UUID> fatherId, Optional<UUID> motherId) {

        VillagerEntityMCA child = VillagerFactory.newVillager(world)
                .withName(babyName)
                .withPosition(player.getPos())
                .withGender(gender)
                .withProfession(ProfessionsMCA.CHILD)
                .withAge(AgeState.startingAge)
                .build();

        Optional<Entity> mother = motherId.map(world::getEntity);
        Optional<Entity> father = fatherId.map(world::getEntity);

        // combine genes
        child.getGenetics().combine(
                mother.map(VillagerLike::toVillager).map(VillagerLike::getGenetics),
                father.map(VillagerLike::toVillager).map(VillagerLike::getGenetics)
        );

        // assign parents
        FamilyTreeNode family = PlayerSaveData.get(world, player.getUuid()).getFamilyEntry();

        fatherId.flatMap(family.getRoot()::getOrEmpty).ifPresent(parent -> {
            child.getRelationships().getFamilyEntry().assignParent(parent);
        });
        motherId.flatMap(family.getRoot()::getOrEmpty).ifPresent(parent -> {
            child.getRelationships().getFamilyEntry().assignParent(parent);
        });
        // in case one of the above was not found
        child.getRelationships().getFamilyEntry().assignParent(family);

        WorldUtils.spawnEntity(world, child, SpawnReason.BREEDING);
        // notify parents
        Stream.concat(Stream.of(mother, father).filter(Optional::isPresent).map(Optional::get), Stream.of(player))
                .filter(e -> e instanceof ServerPlayerEntity)
                .distinct()
                .forEach(ply -> {
            // advancement
            CriterionMCA.FAMILY.trigger((ServerPlayerEntity)ply);
            PlayerSaveData.get(world, ply.getUuid()).setBabyPresent(false);

            // set proper dialogue type
            Memories memories = child.getVillagerBrain().getMemoriesForPlayer(player);
            memories.setDialogueType(DialogueType.CHILDP);
            memories.setHearts(Config.getInstance().childInitialHearts);
        });
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext flag) {
        if (stack.hasTag()) {
            PlayerEntity player = ClientProxy.getClientPlayer();
            NbtCompound nbt = stack.getTag();

            String babyName = getBabyName(stack);

            if (Strings.isNullOrEmpty(babyName)) {
                tooltip.add(new TranslatableText("item.mca.baby.give_name").formatted(Formatting.YELLOW));
            } else {
                tooltip.add(new TranslatableText("item.mca.baby.name", new LiteralText(babyName)).formatted(gender.getColor()));
            }

            tooltip.add(LiteralText.EMPTY);

            int ageInMinutes = nbt.getInt("age");
            tooltip.add(new TranslatableText("item.mca.baby.age" + (ageInMinutes == 1 ? ".plural" : ""), ageInMinutes).formatted(Formatting.GRAY));

            tooltip.add(new TranslatableText("item.mca.baby.owner", player != null && nbt.getUuid("ownerUUID").equals(player.getUuid())
                    ? new TranslatableText("item.mca.baby.owner.you")
                    : nbt.getString("ownerName")
            ).formatted(Formatting.GRAY));

            if (nbt.getBoolean("isInfected")) {
                tooltip.add(new TranslatableText("item.mca.baby.state.infected").formatted(Formatting.DARK_GREEN));
            }

            if (isReadyToGrowUp(stack)) {
                tooltip.add(new TranslatableText("item.mca.baby.state.ready").formatted(Formatting.DARK_GREEN));
            }
        }
    }

    public static Optional<UUID> getSpouse(ItemStack stack, Gender gender) {
        String key = gender.binary() == Gender.MALE ? "father" : "mother";
        return stack.hasTag() && stack.getTag().contains(key) ? Optional.of(stack.getSubTag(key).getUuid("id")) : Optional.empty();
    }

    public static void setSpouse(ItemStack stack, Optional<UUID> spouse, Gender gender) {
        String key = gender.binary() == Gender.MALE ? "father" : "mother";
        if (spouse.isPresent()) {
            stack.getOrCreateSubTag(key).putUuid("id", spouse.get());
        } else if (stack.hasTag() && stack.getTag().containsUuid(key)) {
            stack.removeSubTag(key);
        }
    }

    private static boolean isReadyToGrowUp(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getInt("age") >= Config.getInstance().babyGrowUpTime;
    }

    public static String getBabyName(ItemStack stack) {
        String name = stack.hasTag() ? stack.getTag().getString("name") : "";
        if (Language.getInstance().get("gui.label.unnamed").equals(name)) {
            return "";
        }
        return name;
    }

    public static void setBabyName(ItemStack stack, String name) {
        stack.getOrCreateTag().putString("name", name);
    }
}