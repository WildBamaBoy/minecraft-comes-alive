package mca.api.objects;

import java.util.Optional;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

@AllArgsConstructor
public class NPC {
	@Getter private Entity entity;
	
	public double getPosX() {
		return entity.posX;
	}
	
	public double getPosY() {
		return entity.posY;		
	}
	
	public double getPosZ() {
		return entity.posZ;
	}
	
	public Pos getPosition() {
		return new Pos(entity.getPosition());
	}
	
	public void sendMessage(String message) {
		entity.sendMessage(new TextComponentString(message));
	}
	
	public boolean attackFrom(DamageSource source, float amount) {
		return entity.attackEntityFrom(source, amount);
	}
	
	public String getName() {
		return entity.getName();
	}
	
	public UUID getUniqueID() {
		return entity.getUniqueID();
	}

	public EntityVillagerMCA asVillager() throws ClassCastException {
		return (EntityVillagerMCA)entity;
	}
}
