package mca.data;

import java.io.Serializable;
import java.util.UUID;

import mca.entity.EntityHuman;
import mca.enums.EnumBabyState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

@SuppressWarnings("serial")
public class VillagerSaveData implements Serializable
{
	public final UUID uuid;
	public final UUID ownerUUID;
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
	
	public static VillagerSaveData fromVillager(EntityHuman human, EntityPlayer requestingPlayer, UUID ownerUUID)
	{
		return new VillagerSaveData(human, requestingPlayer, ownerUUID);
	}
	
	public static VillagerSaveData fromNBT(NBTTagCompound nbt)
	{
		return new VillagerSaveData(nbt);
	}
	
	public EntityHuman applyToHuman(EntityHuman human)
	{
		human.setName(name);
		human.setPersonality(personalityId);
		human.setPermanentId(permanentId);
		human.setIsMale(isMale);
		human.setIsEngaged(isEngaged);
		human.setSpouseId(spouseId);
		human.setSpouseName(spouseName);
		human.setBabyState(EnumBabyState.fromId(babyState));
		human.setIsChild(isChild);
		human.setAge(age);
		human.setParentNames(parentNames);
		human.setParentIDs(parentIDs);
		human.setParentsGenders(parentsGenders);
		human.setHeight(scaleHeight);
		human.setGirth(scaleGirth);
		human.setIsInfected(isInfected);
		human.setPlayerSkin(playerSkinUsername);
		human.setProfessionId(professionId);
		human.setClothesTexture(clothesTexture);
		human.setHeadTexture(headTexture);
		
		return human;
	}
	
	private VillagerSaveData(EntityHuman human, EntityPlayer requestingPlayer, UUID ownerUUID)
	{
		this.uuid = human.getUniqueID();
		this.ownerUUID = ownerUUID != null ? ownerUUID : new UUID(0L, 0L);
		this.name = human.getName();
		this.headTexture = human.getHeadTexture();
		this.clothesTexture = human.getClothesTexture();
		this.professionId = human.getProfessionEnum().getId();
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
		this.displayTitle = requestingPlayer != null ? human.getTitle(requestingPlayer) : name;
	}

	private VillagerSaveData(NBTTagCompound nbt)
	{
		long msb = nbt.getLong("uuid-msb");
		long lsb = nbt.getLong("uuid-lsb");
		long ownerMSB = nbt.getLong("owner-uuid-msb");
		long ownerLSB = nbt.getLong("owner-uuid-lsb");
		
		uuid = new UUID(msb, lsb);
		ownerUUID = new UUID(ownerMSB, ownerLSB);
		name = nbt.getString("name");
		headTexture = nbt.getString("headTexture");
		clothesTexture = nbt.getString("clothesTexture");
		professionId = nbt.getInteger("professionId");
		personalityId = nbt.getInteger("personalityId");
		permanentId = nbt.getInteger("permanentId");
		isMale = nbt.getBoolean("isMale");
		isEngaged = nbt.getBoolean("isEngaged");
		spouseId = nbt.getInteger("spouseId");
		spouseName = nbt.getString("spouseName");
		babyState = nbt.getInteger("babyState");
		isChild = nbt.getBoolean("isChild");
		age = nbt.getInteger("age");
		parentNames = nbt.getString("parentNames");
		parentIDs = nbt.getString("parentIDs");
		parentsGenders = nbt.getString("parentsGenders");
		scaleHeight = nbt.getFloat("scaleHeight");
		scaleGirth = nbt.getFloat("scaleGirth");
		isInfected = nbt.getBoolean("isInfected");
		playerSkinUsername = nbt.getString("playerSkinUsername");
		displayTitle = nbt.getString("displayTitle");
	}
	
	public void writeDataToNBT(NBTTagCompound nbt) 
	{
		nbt.setLong("uuid-msb", uuid.getMostSignificantBits());
		nbt.setLong("uuid-lsb", uuid.getLeastSignificantBits());
		nbt.setLong("owner-uuid-msb", ownerUUID.getMostSignificantBits());
		nbt.setLong("owner-uuid-lsb", ownerUUID.getLeastSignificantBits());
		nbt.setString("name", name);
		nbt.setString("headTexture", headTexture);
		nbt.setString("clothesTexture", clothesTexture);
		nbt.setInteger("professionId", professionId);
		nbt.setInteger("personalityId", personalityId);
		nbt.setInteger("permanentId", permanentId);
		nbt.setBoolean("isMale", isMale);
		nbt.setBoolean("isEngaged", isEngaged);
		nbt.setInteger("spouseId", spouseId);
		nbt.setString("spouseName", spouseName);
		nbt.setInteger("babyState", babyState);
		nbt.setBoolean("isChild", isChild);
		nbt.setInteger("age", age);
		nbt.setString("parentNames", parentNames);
		nbt.setString("parentIDs", parentIDs);
		nbt.setString("parentsGenders", parentsGenders);
		nbt.setFloat("scaleHeight", scaleHeight);
		nbt.setFloat("scaleGirth", scaleGirth);
		nbt.setBoolean("isInfected", isInfected);
		nbt.setString("playerSkinUsername", playerSkinUsername);
		nbt.setString("displayTitle", displayTitle);
	}
}
