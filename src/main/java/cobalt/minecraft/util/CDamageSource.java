package cobalt.minecraft.util;

import cobalt.minecraft.entity.player.CPlayer;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;

import java.util.Optional;

public class CDamageSource {
    @Getter DamageSource mcSource;

    private CDamageSource(DamageSource source) {
        this.mcSource = source;
    }

    public static CDamageSource fromMC(DamageSource source) {
        return new CDamageSource(source);
    }

    public String getCauseName(LivingEntity entity) {
        return mcSource.getDeathMessage(entity).getString();
    }

    public Optional<CPlayer> getPlayer() {
        if (mcSource.getTrueSource() instanceof PlayerEntity) {
            return Optional.of(CPlayer.fromMC((PlayerEntity)mcSource.getTrueSource()));
        } else {
            return Optional.empty();
        }
    }

    public boolean isZombie() {
        return mcSource.getImmediateSource() instanceof ZombieEntity;
    }
}
