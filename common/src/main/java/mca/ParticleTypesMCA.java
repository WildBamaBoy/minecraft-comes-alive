package mca;

import mca.cobalt.registration.Registration;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface ParticleTypesMCA {
    DefaultParticleType POS_INTERACTION = register("pos_interaction", Registration.ObjectBuilders.Particles.simpleParticle());
    DefaultParticleType NEG_INTERACTION = register("neg_interaction", Registration.ObjectBuilders.Particles.simpleParticle());

    static void bootstrap() { }

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registration.register(Registry.PARTICLE_TYPE, new Identifier(MCA.MOD_ID, name), type);
    }
}
