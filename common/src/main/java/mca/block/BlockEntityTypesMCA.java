package mca.block;

import mca.MCA;
import mca.cobalt.registration.Registration;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public interface BlockEntityTypesMCA {

    BlockEntityType<TombstoneBlock.Data> TOMBSTONE = register("tombstone", BlockEntityType.Builder.create(TombstoneBlock.Data.constructor, BlocksMCA.GRAVELLING_HEADSTONE, BlocksMCA.UPRIGHT_HEADSTONE, BlocksMCA.SLANTED_HEADSTONE, BlocksMCA.CROSS_HEADSTONE, BlocksMCA.WALL_HEADSTONE));

    static void bootstrap() {
    }

    static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
        Identifier id = new Identifier(MCA.MOD_ID, name);
        return Registration.register(Registry.BLOCK_ENTITY_TYPE, id, builder.build(Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id.toString())));
    }
}
