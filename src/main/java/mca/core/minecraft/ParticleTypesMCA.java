package mca.core.minecraft;

import mca.core.forge.Registration;
import net.minecraft.particle.DefaultParticleType;
import net.minecraftforge.fml.RegistryObject;

public class ParticleTypesMCA {
    public static final RegistryObject<DefaultParticleType> POS_INTERACTION = Registration.PARTICLE_TYPES.register("pos_interaction", () -> new BasicParticleType(false));
    public static final RegistryObject<DefaultParticleType> NEG_INTERACTION = Registration.PARTICLE_TYPES.register("neg_interaction", () -> new BasicParticleType(false));

    public static void init() {
    }
}
