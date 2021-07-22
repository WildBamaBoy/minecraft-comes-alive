package mca.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class InteractionParticle extends SpriteBillboardParticle {
    protected InteractionParticle(ClientWorld p_i232447_1_, double p_i232447_2_, double p_i232447_4_, double p_i232447_6_) {
        super(p_i232447_1_, p_i232447_2_, p_i232447_4_, p_i232447_6_);
        this.velocityX *= 0.01F;
        this.velocityY *= 0.01F;
        this.velocityZ *= 0.01F;
        this.velocityY += 0.1D;
        this.scale *= 1.5F;
        this.maxAge = 20;
        this.collidesWithWorld = false;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    public float getSize(float p_217561_1_) {
        return 0.3F;
    }

    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            //this.move(this.xd, this.yd, this.zd);
            if (this.y == this.prevPosY) {
                this.velocityX *= 1.1D;
                this.velocityZ *= 1.1D;
            }

            this.velocityX *= 0.86F;
            this.velocityY *= 0.86F;
            this.velocityZ *= 0.86F;
            if (this.onGround) {
                this.velocityX *= 0.7F;
                this.velocityZ *= 0.7F;
            }

        }
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprite;

        public Factory(SpriteProvider p_i50748_1_) {
            this.sprite = p_i50748_1_;
        }

        public Particle createParticle(DefaultParticleType particleType, ClientWorld world, double p_199234_3_, double p_199234_5_, double p_199234_7_, double p_199234_9_, double p_199234_11_, double p_199234_13_) {
            InteractionParticle heartparticle = new InteractionParticle(world, p_199234_3_, p_199234_5_ + 0.5D, p_199234_7_);
            heartparticle.setSprite(this.sprite);
            heartparticle.setColor(1.0F, 1.0F, 1.0F);
            return heartparticle;
        }
    }
}
