package mca.item;

import mca.MCA;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerFactory;
import mca.entity.ai.DialogueType;
import mca.entity.ai.Memories;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.relationship.Gender;
import mca.network.OpenGuiRequest;
import mca.server.world.data.FamilyTree;
import mca.server.world.data.FamilyTreeEntry;
import mca.server.world.data.PlayerSaveData;
import mca.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Language;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;

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
                NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.gui.BABY_NAME), (ServerPlayerEntity) player);
            }

            return TypedActionResult.pass(stack);
        }

        // Name is good and we're ready to grow
        if (world.isClient || !isReadyToGrowUp(stack)) {
            return TypedActionResult.pass(stack);
        }

        // assumes your child is from the player's current spouse
        // as the father does not have any genes it just takes the one from the mother

        PlayerSaveData playerData = PlayerSaveData.get((ServerWorld)world, player.getUuid());
        FamilyTree familyTree = playerData.getFamilyTree();

        VillagerEntityMCA child = VillagerFactory.newVillager(world)
                .withName(getBabyName(stack))
                .withPosition(player.getPos())
                .withGender(gender)
                .withProfession(ProfessionsMCA.CHILD)
                .build();

        Entity spouse = playerData.getSpouse().orElse(null);
        if (spouse instanceof VillagerEntityMCA) {
            VillagerEntityMCA spouseVillager = (VillagerEntityMCA) spouse;
            familyTree.getOrCreate(spouseVillager);
            child.getGenetics().combine(spouseVillager.getGenetics(), spouseVillager.getGenetics());
        } else {
            child.getGenetics().randomize(child);
        }

        //add the child to the family tree
        UUID spouseId = playerData.getSpouseUUID();
        FamilyTreeEntry spouseEntry = familyTree.getEntry(playerData.getSpouseUUID());

        if (spouseEntry != null && spouseEntry.gender() == Gender.FEMALE) {
            familyTree.addChild(player.getUuid(), spouseId, child);
        } else {
            familyTree.addChild(spouseId, player.getUuid(), child);
        }

        WorldUtils.spawnEntity(world, child, SpawnReason.BREEDING);

        player.getStackInHand(hand).decrement(1);
        playerData.setBabyPresent(false);

        // set proper dialogue type
        Memories memories = child.getVillagerBrain().getMemoriesForPlayer(player);
        memories.setDialogueType(DialogueType.CHILDP);
        memories.setHearts(MCA.getConfig().childInitialHearts);

        stack.decrement(1);

        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext flag) {
        if (stack.hasTag()) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            NbtCompound nbt = stack.getTag();

            String babyName = getBabyName(stack);
            boolean unnamed = Strings.isNullOrEmpty(babyName);

            tooltip.add(new TranslatableText("gui.label.name")
                    .formatted(gender.getColor())
                    .append(" ")
                    .append(unnamed ? new TranslatableText("gui.label.unnamed") : new LiteralText(babyName))
            );

            int ageInMinutes = nbt.getInt("age");
            tooltip.add(new TranslatableText("gui.label.age")
                    .append(" ").append(String.valueOf(ageInMinutes))
                    .append(" ").append(new TranslatableText("gui.label.minute" + (ageInMinutes == 1 ? "s" : "")))
            );

            tooltip.add(new TranslatableText("gui.label.parent")
                    .append(" ")
                    .append(nbt.getUuid("ownerUUID").equals(player.getUuid()) ? new TranslatableText("gui.label.you") : new LiteralText(nbt.getString("ownerName"))));

            if (nbt.getBoolean("isInfected")) {
                tooltip.add(new TranslatableText("gui.label.infected").formatted(Formatting.GREEN));
            }

            if (isReadyToGrowUp(stack)) {
                tooltip.add(new TranslatableText("gui.label.readytogrow").formatted(Formatting.GREEN));
            }

            if (unnamed) {
                tooltip.add(new TranslatableText("gui.label.rightclicktoname").formatted(Formatting.YELLOW));
            }
        }
    }

    private static boolean isReadyToGrowUp(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getInt("age") >= MCA.getConfig().babyGrowUpTime;
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