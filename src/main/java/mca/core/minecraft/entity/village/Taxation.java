package mca.core.minecraft.entity.village;

import mca.cobalt.localizer.Localizer;
import mca.entity.data.Village;
import mca.entity.data.VillageManagerData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class Taxation {

    static void updateTaxes(World world) {
        //TODO: Implement taxes
        // WIP and nobody can stop me implementing them hehe

        for (Village village : VillageManagerData.get(world)) {
            int taxes = village.getPopulation() * village.getTaxes() + world.random.nextInt(100);
            int emeraldValue = 100;
            int emeraldCount = taxes / emeraldValue;

            village.storageBuffer.add(new ItemStack(Items.EMERALD, emeraldCount));
            deliverTaxes(village, (ServerWorld) world);

            world.getPlayers().forEach((player) -> player.sendSystemMessage(Localizer.localizeText("gui.village.taxes", village.getName()), player.getUuid()));
        }
    }

    static void deliverTaxes(Village village, ServerWorld world) {
        if (village.hasStoredResource()) {
            village.getBuildings().values()
                .stream()
                .filter(b -> b.getType().equals("inn") && world.canSetBlock(b.getCenter()))
                .forEach(building -> {
                    // TODO: noop
            });
        }
    }

}
