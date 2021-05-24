package mca.data;

import mca.core.MCA;
import mca.data.client.BlockStateProviderMCA;
import mca.data.client.ItemModelProviderMCA;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = MCA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        gen.addProvider(new BlockStateProviderMCA(gen, existingFileHelper));
        gen.addProvider(new ItemModelProviderMCA(gen, existingFileHelper));

        BlockTagsProviderMCA blockTags = new BlockTagsProviderMCA(gen, existingFileHelper);
        gen.addProvider(blockTags);
        gen.addProvider(new ItemTagsProviderMCA(gen, blockTags, existingFileHelper));

        gen.addProvider(new LootTableProviderMCA(gen));
        gen.addProvider(new RecipeProviderMCA(gen));
    }
}
