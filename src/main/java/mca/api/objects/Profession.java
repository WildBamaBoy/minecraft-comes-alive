package mca.api.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

@AllArgsConstructor
public class Profession {
	@Getter private VillagerProfession vanillaProfession;

	public ResourceLocation getResourceName() {
		return this.vanillaProfession.getRegistryName();
	}
}
