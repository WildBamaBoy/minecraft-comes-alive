package mca.server.world.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mca.item.BabyItem;
import mca.util.NbtHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class BabyBunker {
    // Maps a player UUID to the itemstack of their held ItemBaby. Filled when a player dies so the baby is never lost.
    private final Map<UUID, List<ItemStack>> limbo;

    private final VillageManager manager;

    BabyBunker(VillageManager manager) {
        this.manager = manager;
        limbo = new HashMap<>();
    }

    BabyBunker(VillageManager manager, NbtCompound nbt) {
        this.manager = manager;
        limbo = NbtHelper.toMap(nbt, UUID::fromString, element -> NbtHelper.toList(element, i -> ItemStack.fromNbt((NbtCompound)i)));
    }

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
            manager.markDirty();
        }
    }

    public void push(PlayerEntity player) {
        // If a player dies while holding a baby, remember it until they respawn.
        List<ItemStack> babies = player.getInventory().main.stream()
                .filter(s -> s.getItem() instanceof BabyItem)
                .toList();

        if (!babies.isEmpty()) {
            limbo.put(player.getUuid(), babies);
            manager.markDirty();
        }
    }
}
