package mca.core.minecraft;

import mca.core.MCA;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface ParticleTypesMCA {
    DefaultParticleType POS_INTERACTION = register("pos_interaction", FabricParticleTypes.simple());
    DefaultParticleType NEG_INTERACTION = register("neg_interaction", FabricParticleTypes.simple());

    static void bootstrap() { }

    private static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registry.PARTICLE_TYPE, new Identifier(MCA.MOD_ID, name), type);
    }
}
