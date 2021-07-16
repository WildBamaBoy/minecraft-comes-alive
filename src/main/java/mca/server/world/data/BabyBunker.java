package mca.server.world.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mca.item.BabyItem;
import mca.util.NbtHelper;
import mca.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class BabyBunker extends PersistentState {
    private static final String DATA_ID = "MCA-BabyBunker";

    // Maps a player UUID to the itemstack of their held ItemBaby. Filled when a player dies so the baby is never lost.
    private final Map<UUID, List<ItemStack>> limbo;

    public static BabyBunker get(ServerWorld world) {
        return WorldUtils.loadData(world, BabyBunker::new, BabyBunker::new, DATA_ID);
    }

    BabyBunker(ServerWorld world) {
        limbo = new HashMap<>();
    }

    BabyBunker(NbtCompound nbt) {
        limbo = NbtHelper.toMap(nbt, UUID::fromString, element -> NbtHelper.toList(element, i -> ItemStack.fromNbt((NbtCompound)i)));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return NbtHelper.fromMap(nbt, limbo, UUID::toString, stacks -> NbtHelper.fromList(stacks, stack -> stack.writeNbt(new NbtCompound())));
    }

    public void pop(PlayerEntity player) {
        // When players respawn check to see if their baby was saved in limbo. Add it back to their inventory.

        List<ItemStack> baby = limbo.remove(player.getUuid());

        if (baby != null) {
            baby.forEach(bab -> {
                if (!player.giveItemStack(bab)) {
                    player.dropStack(bab, 0);
                }
            });
        }
    }

    public void push(PlayerEntity player) {
        // If a player dies while holding a baby, remember it until they respawn.
        List<ItemStack> babies = player.getInventory().main.stream()
                .filter(s -> s.getItem() instanceof BabyItem)
                .toList();

        if (!babies.isEmpty()) {
            limbo.put(player.getUuid(), babies);
        }
    }
}
