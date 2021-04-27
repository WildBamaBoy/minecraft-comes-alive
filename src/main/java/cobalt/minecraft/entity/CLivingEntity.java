package cobalt.minecraft.entity;

import cobalt.enums.CEnumHand;
import cobalt.minecraft.item.CItemStack;
import cobalt.minecraft.util.math.CPos;
import cobalt.minecraft.world.CWorld;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.UUID;

public class CLivingEntity extends CEntity {
    @Getter private final LivingEntity mcEntity;
    @Getter private final World mcWorld;

    protected CLivingEntity(LivingEntity entity) {
        super(entity);
        this.mcEntity = entity;
        this.mcWorld = entity.level;
    }

    public static CLivingEntity fromMC(LivingEntity entity) {
        return new CLivingEntity(entity);
    }
    public CItemStack getHeldItem(CEnumHand hand) {
        return CItemStack.fromMC(mcEntity.getMainHandItem());
    }
}
