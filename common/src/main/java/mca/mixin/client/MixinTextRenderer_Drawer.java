package mca.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net/minecraft/client/font/TextRenderer$Drawer")
public interface MixinTextRenderer_Drawer {
    @Accessor("x")
    float getX();
    @Accessor("y")
    float getY();
    @Accessor("x")
    void setX(float x);
    @Accessor("y")
    void setY(float y);
}
