package mca.server.world.village;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mca.item.BabyItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class BabyBunker {
    // TODO: this should be stored with the world
    // Maps a player UUID to the itemstack of their held ItemBaby. Filled when a player dies so the baby is never lost.
    private static final Map<UUID, List<ItemStack>> LIMBO = new HashMap<>();

    public static void pop(PlayerEntity player) {
        // When players respawn check to see if their baby was saved in limbo. Add it back to their inventory.

        List<ItemStack> baby = LIMBO.remove(player.getUuid());

        if (baby != null) {
            baby.forEach(bab -> {
                if (!player.giveItemStack(bab)) {
                    player.dropStack(bab, 0);
                }
            });
        }
    }

    public static void push(PlayerEntity player) {
        // If a player dies while holding a baby, remember it until they respawn.
        List<ItemStack> babies = player.getInventory().main.stream()
                .filter(s -> s.getItem() instanceof BabyItem)
                .toList();

        if (!babies.isEmpty()) {
            LIMBO.put(player.getUuid(), babies);
        }
    }
}
