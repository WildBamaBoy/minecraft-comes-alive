package com.minecraftcomesalive.mca.api.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.ResourceLocation;

@AllArgsConstructor
public class Profession {
	@Getter private VillagerProfession vanillaProfession;

	public ResourceLocation getResourceName() {
		return this.vanillaProfession.getRegistryName();
	}
}
