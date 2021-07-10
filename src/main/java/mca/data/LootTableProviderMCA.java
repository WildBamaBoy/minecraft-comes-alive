package mca.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import mca.core.forge.Registration;
import mca.core.minecraft.BlocksMCA;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.server.BlockLootTableGenerator;
import net.minecraft.data.server.LootTablesProvider;
import net.minecraft.loot.*;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LootTableProviderMCA extends LootTablesProvider {
    public LootTableProviderMCA(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<Identifier, LootTable.Builder>>>, LootContextType>> getTables() {
        return ImmutableList.of(
                Pair.of(ModBlockLootTables::new, LootContextTypes.BLOCK)
        );
    }

    @Override
    protected void validate(Map<Identifier, LootTable> map, LootTableReporter validationtracker) {
        map.forEach((p_218436_2_, p_218436_3_) -> LootManager.validate(validationtracker, p_218436_2_, p_218436_3_));
    }

    public static class ModBlockLootTables extends BlockLootTableGenerator {
        @Override
        protected void addTables() {
            addDrop(BlocksMCA.ROSE_GOLD_BLOCK.get());
            addDrop(BlocksMCA.ROSE_GOLD_ORE.get());
            //dropSelf(BlocksMCA.JEWELER_WORKBENCH.get()); //TODO BlocksMCA.JEWELER_WORKBENCH
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return Registration.BLOCKS.getEntries().stream()
                    .map(RegistryObject::get)
                    .collect(Collectors.toList());
        }
    }
}
