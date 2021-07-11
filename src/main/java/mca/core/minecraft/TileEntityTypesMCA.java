package mca.core.minecraft;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import mca.core.MCA;

public interface TileEntityTypesMCA {
    static void bootstrap() { }

    private static <T extends BlockEntity> BlockEntityType<T> create(String name, BlockEntityType.Builder<T> builder) {
        Identifier id = new Identifier(MCA.MOD_ID, name);
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, id, builder.build(Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id.toString())));
     }
}
