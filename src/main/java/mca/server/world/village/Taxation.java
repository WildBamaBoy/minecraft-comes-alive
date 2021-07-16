package mca.server.world.village;

import mca.entity.ai.Messenger;
import mca.server.world.data.Village;
import mca.server.world.data.VillageManagerData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;

class Taxation {
    static void updateTaxes(ServerWorld world) {
        //TODO: Implement taxes
        // WIP and nobody can stop me implementing them hehe

        for (Village village : VillageManagerData.get(world)) {
            int taxes = village.getPopulation() * village.getTaxes() + world.random.nextInt(100);
            int emeraldValue = 100;
            int emeraldCount = taxes / emeraldValue;

            village.storageBuffer.add(new ItemStack(Items.EMERALD, emeraldCount));
            deliverTaxes(village, world);

            Messenger.sendEventMessage(world, new TranslatableText("gui.village.taxes", village.getName()));
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
