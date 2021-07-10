package mca.core.forge;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;

public class TileEntityTypesMCA {

    static void register() {
    }

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, Supplier<T> factory, RegistryObject<? extends Block> block) {
        return Registration.TILE_ENTITIES.register(name, () -> {
            //noinspection ConstantConditions - null in build
            return TileEntityType.Builder.of(factory, block.get()).build(null);
        });
    }
}
