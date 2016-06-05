package mca.data;

import java.io.Serializable;
import java.util.UUID;

import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;

@SuppressWarnings("serial")
public class VillagerSaveData implements Serializable
{
	public final UUID uuid;
	public final String name;
	public final String headTexture;
	public final String clothesTexture;
	public final int professionId;
	public final int personalityId;
	public final int permanentId;
	public final boolean isMale;
	public final boolean isEngaged;
	public final int spouseId;
	public final String spouseName;
	public final int babyState;
	public final boolean isChild;
	public final int age;
	public final String parentNames;
	public final String parentIDs;
	public final String parentsGenders;
	public final float scaleHeight;
	public final float scaleGirth;
	public final boolean isInfected;
	public final String playerSkinUsername;
	public final String displayTitle;
	
	public static VillagerSaveData fromVillager(EntityHuman human, EntityPlayer requestingPlayer)
	{
		return new VillagerSaveData(human, requestingPlayer);
	}
	
	private VillagerSaveData(EntityHuman human, EntityPlayer requestingPlayer)
	{
		this.uuid = human.getUniqueID();
		this.name = human.getName();
		this.headTexture = human.getHeadTexture();
		this.clothesTexture = human.getClothesTexture();
		this.professionId = human.getProfession();
		this.personalityId = human.getPersonality().getId();
		this.permanentId = human.getPermanentId();
		this.isMale = human.getIsMale();
		this.isEngaged = human.getIsEngaged();
		this.spouseId = human.getSpouseId();
		this.spouseName = human.getSpouseName();
		this.babyState = human.getBabyState().getId();
		this.isChild = human.getIsChild();
		this.age = human.getAge();
		this.parentNames = human.getParentNames();
		this.parentIDs = human.getParentIds();
		this.parentsGenders = human.getParentsGenders();
		this.scaleHeight = human.getHeight();
		this.scaleGirth = human.getGirth();
		this.isInfected = human.getIsInfected();
		this.playerSkinUsername = human.getPlayerSkinUsername();
		this.displayTitle = human.getTitle(requestingPlayer);
	}
}
