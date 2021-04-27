package cobalt.mod.forge;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderHandler<T extends Entity> {
    @Getter private final EntityType<T> entityClass;
    @Getter private final IRenderFactory<? super T> renderFactory;

    public RenderHandler(EntityType<T> entityClass, IRenderFactory<? super T> renderFactory) {
        this.entityClass = entityClass;
        this.renderFactory = renderFactory;
    }
}
