package mca.block;

import mca.MCA;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public interface BlockEntityTypesMCA {

    BlockEntityType<TombstoneBlock.Data> TOMBSTONE = register("tombstone", FabricBlockEntityTypeBuilder.create(TombstoneBlock.Data::new, BlocksMCA.UPRIGHT_HEADSTONE, BlocksMCA.SLANTED_HEADSTONE, BlocksMCA.CROSS_HEADSTONE));

    static void bootstrap() { }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder<T> builder) {
        Identifier id = new Identifier(MCA.MOD_ID, name);
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, id, builder.build(Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id.toString())));
    }
}
