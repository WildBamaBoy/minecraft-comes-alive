package mca.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net/minecraft/client/font/TextRenderer$Drawer")
public interface MixinTextRenderer_Drawer {
    @Accessor
    float getX();
    @Accessor
    float getY();
    @Accessor
    void setX(float x);
    @Accessor
    void setY(float y);
}
