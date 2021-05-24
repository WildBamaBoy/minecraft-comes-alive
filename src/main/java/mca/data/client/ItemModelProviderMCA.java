package mca.data.client;

import mca.core.MCA;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelProviderMCA extends ItemModelProvider {
    public ItemModelProviderMCA(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MCA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent("rose_gold_block", modLoc("block/rose_gold_block"));
        withExistingParent("rose_gold_ore", modLoc("block/rose_gold_ore"));
        withExistingParent("villager_spawner", modLoc("block/villager_spawner"));

        ModelFile itemGenerated = getExistingFile(mcLoc("item/generated"));

        builder(itemGenerated, "rose_gold_ingot");
    }

    private ItemModelBuilder builder(ModelFile itemGenerated, String name) {
        return getBuilder(name).parent(itemGenerated).texture("layer0", "items/" + name);
    }
}