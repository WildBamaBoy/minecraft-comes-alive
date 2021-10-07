package mca.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net/minecraft/client/font/TextRenderer$Drawer")
public interface MixinTextRenderer_DrawerGetter {
    @Accessor("x")
    float getX();
    @Accessor("y")
    float getY();
}
