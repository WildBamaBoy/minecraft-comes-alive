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
        //withExistingParent("villager_spawner", modLoc("block/villager_spawner"));
        withExistingParent("tombstone", modLoc("block/tombstone"));
        withExistingParent("jeweler_workbench", modLoc("block/jeweler_workbench"));

        ModelFile itemGenerated = getExistingFile(mcLoc("item/generated"));

        builder(itemGenerated, "rose_gold_ingot");
        builder(itemGenerated, "egg_female");
        builder(itemGenerated, "egg_male");
        builder(itemGenerated, "baby_boy");
        builder(itemGenerated, "baby_girl");
        builder(itemGenerated, "wedding_ring");
        builder(itemGenerated, "wedding_ring_rg");
        builder(itemGenerated, "engagement_ring");
        builder(itemGenerated, "engagement_ring_rg");
        builder(itemGenerated, "matchmakers_ring");
        builder(itemGenerated, "villager_editor");
        builder(itemGenerated, "staff_of_life");
        builder(itemGenerated, "whistle");
        builder(itemGenerated, "blueprint");
        builder(itemGenerated, "book_rose_gold");
        builder(itemGenerated, "book_death");
        builder(itemGenerated, "book_romance");
        builder(itemGenerated, "book_family");
        builder(itemGenerated, "book_infection");
        builder(itemGenerated, "gold_dust");
        builder(itemGenerated, "rose_gold_dust");
        builder(itemGenerated, "rose_gold_ingot");
    }

    private ItemModelBuilder builder(ModelFile itemGenerated, String name) {
        return getBuilder(name).parent(itemGenerated).texture("layer0", "items/" + name);
    }
}