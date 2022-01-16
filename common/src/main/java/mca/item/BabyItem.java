package mca.item;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mca.ClientProxy;
import mca.Config;
import mca.advancement.criterion.CriterionMCA;
import mca.cobalt.network.NetworkHandler;
import mca.entity.Status;
import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerFactory;
import mca.entity.VillagerLike;
import mca.entity.ai.Memories;
import mca.entity.ai.relationship.AgeState;
import mca.entity.ai.relationship.Gender;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.network.GetChildDataRequest;
import mca.network.client.OpenGuiRequest;
import mca.server.world.data.BabyTracker;
import mca.server.world.data.BabyTracker.ChildSaveState;
import mca.server.world.data.BabyTracker.MutableChildSaveState;
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
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BabyItem extends Item {
    public static final LoadingCache<UUID, Optional<BabyTracker.ChildSaveState>> CLIENT_STATE_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(CacheLoader.from(id -> {
                NetworkHandler.sendToServer(new GetChildDataRequest(id));
                return Optional.empty();
            }));

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
        if (!hasBeenInvalidated(stack)) {
            player.sendMessage(new TranslatableText("item.mca.baby.no_drop"), true);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient) {
            return;
        }

        // remove duplicates
        if (entity instanceof ServerPlayerEntity) {
            if (world.getTime() % 20 == 0) {
                Set<UUID> found = new HashSet<>();
                ServerPlayerEntity player = (ServerPlayerEntity)entity;
                for (int i = player.inventory.size() - 1; i >= 0; i--) {
                    ItemStack s = player.inventory.getStack(i);
                    Optional<UUID> id = BabyTracker.getStateId(s);
                    if (id.isPresent()) {
                        if (found.contains(id.get())) {
                            player.inventory.removeStack(i);
                        } else {
                            found.add(id.get());
                        }
                    }
                }
            }
        }

        // update
        if (BabyTracker.hasState(stack)) {
            Optional<MutableChildSaveState> state = BabyTracker.getState(stack, (ServerWorld)world);
            if (state.isPresent()) {

                // use an anvil to rename your baby (in case of typos like I did)
                if (stack.hasCustomName()) {
                    state.get().setName(stack.getName().getString());
                    state.get().writeToItem(stack);
                    stack.removeCustomName();

                    if (entity instanceof ServerPlayerEntity) {
                        CriterionMCA.GENERIC_EVENT_CRITERION.trigger((ServerPlayerEntity)entity, "rename_baby");
                    }
                }

                if (state.get().getName().isPresent() && world.getTime() % 1200 == 0) {
                    stack.getTag().putInt("age", stack.getTag().getInt("age") + 1);
                }
            } else {
                BabyTracker.invalidate(stack);
            }
        } else if (!stack.hasTag() || !stack.getTag().getBoolean("invalidated")) {
            // legacy and items obtained by creative
            BabyTracker.get((ServerWorld)world).getPairing(entity.getUuid(), entity.getUuid()).addChild(state -> {
                state.setGender(gender);
                state.setOwner(entity);
                state.writeToItem(stack);
            });
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        return getClientCheckedState(stack).flatMap(ChildSaveState::getName).map(s -> {
            return (Text)new TranslatableText(getTranslationKey(stack) + ".named", s);
        }).orElseGet(() -> super.getName(stack));
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (hasBeenInvalidated(stack)) {
            return super.getTranslationKey(stack) + ".blanket";
        }
        return super.getTranslationKey(stack);
    }

    @Override
    public final TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getStackInHand(hand);

        if (world.isClient) {
            return TypedActionResult.pass(stack);
        }

        return BabyTracker.getState(stack, (ServerWorld)world).map(state -> {
            // Right-clicking an unnamed baby allows you to name it
            if (!state.getName().isPresent()) {
                if (player instanceof ServerPlayerEntity) {
                    NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.BABY_NAME), (ServerPlayerEntity)player);
                }

                return TypedActionResult.pass(stack);
            }

            if (!isReadyToGrowUp(stack)) {
                return TypedActionResult.pass(stack);
            }

            if (player instanceof ServerPlayerEntity) {
                // Name is good and we're ready to grow
                birthChild(state, (ServerWorld)world, (ServerPlayerEntity)player);
            }
            stack.decrement(1);

            return TypedActionResult.success(stack);
        }).orElseGet(() -> {
            if (BabyTracker.getState(stack).isPresent()) {
                world.sendEntityStatus(player, Status.PLAYER_CLOUD_EFFECT);
                player.playSound(SoundEvents.UI_TOAST_OUT, 1, 1);
                BabyTracker.invalidate(stack);
                return TypedActionResult.fail(stack);
            }
            return TypedActionResult.fail(stack);
        });
    }

    private void birthChild(BabyTracker.ChildSaveState state, ServerWorld world, ServerPlayerEntity player) {

        VillagerEntityMCA child = VillagerFactory.newVillager(world)
                .withName(state.getName().orElse("Unnamed"))
                .withPosition(player.getPos())
                .withGender(gender)
                .withAge(-AgeState.getMaxAge())
                .build();

        List<Entity> parents = state.getParents().map(world::getEntity).filter(Objects::nonNull).collect(Collectors.toList());

        Optional<Entity> mother = parents.stream().findFirst();
        Optional<Entity> father = parents.stream().skip(1).findFirst();

        // combine genes
        child.getGenetics().combine(
                mother.map(VillagerLike::toVillager).map(VillagerLike::getGenetics),
                father.map(VillagerLike::toVillager).map(VillagerLike::getGenetics),
                state.getSeed()
        );

        // inherit traits
        mother.map(VillagerLike::toVillager).map(VillagerLike::getTraits).ifPresent(t -> child.getTraits().inherit(t, state.getSeed()));
        father.map(VillagerLike::toVillager).map(VillagerLike::getTraits).ifPresent(t -> child.getTraits().inherit(t, state.getSeed()));

        // assign parents
        FamilyTreeNode family = PlayerSaveData.get(world, player.getUuid()).getFamilyEntry();

        state.getParents().forEach(p -> {
            family.getRoot().getOrEmpty(p).ifPresent(parent -> {
                child.getRelationships().getFamilyEntry().assignParent(parent);
            });
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

                    // set proper dialogue type
                    Memories memories = child.getVillagerBrain().getMemoriesForPlayer(player);
                    memories.setHearts(Config.getInstance().childInitialHearts);
                });

        BabyTracker.get(world).getPairing(state).removeChild(state);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext flag) {

        getClientState(stack).ifPresent(state -> {
            PlayerEntity player = ClientProxy.getClientPlayer();
            NbtCompound nbt = stack.getTag();

            int age = nbt.getInt("age") * 1200 + (int)(world == null ? 0 : world.getTime() % 1200);

            if (!state.getName().isPresent()) {
                tooltip.add(new TranslatableText("item.mca.baby.give_name").formatted(Formatting.YELLOW));
            } else {
                tooltip.add(new TranslatableText("item.mca.baby.name", new LiteralText(state.getName().get()).formatted(gender.getColor())).formatted(Formatting.GRAY));

                if (age > 0) {
                    tooltip.add(new TranslatableText("item.mca.baby.age", ChatUtil.ticksToString(age)).formatted(Formatting.GRAY));
                }
            }

            tooltip.add(LiteralText.EMPTY);

            state.getOwner().ifPresent(owner -> {
                tooltip.add(new TranslatableText("item.mca.baby.owner", player != null && owner.getLeft().equals(player.getUuid())
                        ? new TranslatableText("item.mca.baby.owner.you")
                        : owner.getRight()
                ).formatted(Formatting.GRAY));
            });

            if (state.getName().isPresent() && canGrow(age)) {
                tooltip.add(new TranslatableText("item.mca.baby.state.ready").formatted(Formatting.DARK_GREEN));
            }

            if (state.isInfected()) {
                tooltip.add(new TranslatableText("item.mca.baby.state.infected").formatted(Formatting.DARK_GREEN));
            }
        });
    }

    /**
     * Callable on both sides. If a request is out for details, use that, otherwise keep using the stack's data.
     */
    private static Optional<ChildSaveState> getClientCheckedState(ItemStack stack) {
        return BabyTracker.getState(stack).map(state -> {
            Optional<ChildSaveState> loaded = CLIENT_STATE_CACHE.getIfPresent(state.getId());

            //noinspection OptionalAssignedToNull
            if (loaded == null) {
                return state;
            }

            if (loaded.isPresent()) {
                ChildSaveState l = loaded.get();
                if (
                        (state.getName().isPresent() && !l.getName().isPresent())
                                || (state.getName().isPresent() && l.getName().isPresent() && !state.getName().get().contentEquals(l.getName().get()))
                ) {
                    CLIENT_STATE_CACHE.refresh(state.getId());
                    return state;
                }
                return l;
            }
            return state;
        });
    }

    /**
     * Callable on the client only. Starts a request for the stack's data and returns an empty until resolution is complete.
     */
    private static Optional<ChildSaveState> getClientState(ItemStack stack) {
        return BabyTracker.getState(stack).flatMap(state -> {
            try {
                return CLIENT_STATE_CACHE.get(state.getId());
            } catch (ExecutionException e) {
                return Optional.of(state);
            }
        });
    }

    public static boolean hasBeenInvalidated(ItemStack stack) {
        return (stack.hasTag() && stack.getTag().getBoolean("invalidated")) || BabyTracker.getStateId(stack).map(id -> {
            Optional<ChildSaveState> loaded = CLIENT_STATE_CACHE.getIfPresent(id);

            return loaded != null && !loaded.isPresent();
        }).orElse(false);
    }

    private static boolean canGrow(int age) {
        return age / 1200 >= Config.getInstance().babyGrowUpTime;
    }

    private static boolean isReadyToGrowUp(ItemStack stack) {
        return stack.hasTag() && canGrow(stack.getTag().getInt("age") * 1200);
    }
}
