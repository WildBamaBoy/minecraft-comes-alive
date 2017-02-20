package mca.entity;

import java.io.Serializable;
import java.util.UUID;

import mca.enums.EnumBabyState;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumPersonality;
import mca.enums.EnumProfession;
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
	public final EnumProfession professionId;
	public final EnumPersonality personalityId;
	public final EnumGender gender;
	public final EnumMarriageState marriageState;
	public final UUID spouseUUID;
	public final String spouseName;
	public final EnumGender spouseGender;
	public final EnumBabyState babyState;
	public final boolean isChild;
	public final int age;
	public final String motherName;
	public final String fatherName;
	public final UUID motherUUID;
	public final UUID fatherUUID;
	public final EnumGender motherGender;
	public final EnumGender fatherGender;
	public final float scaleHeight;
	public final float scaleWidth;
	public final boolean isInfected;
	public final String displayTitle;
	
	public static VillagerSaveData fromVillager(EntityVillagerMCA human, EntityPlayer requestingPlayer, UUID ownerUUID)
	{
		return new VillagerSaveData(human, requestingPlayer, ownerUUID);
	}
	
	public static VillagerSaveData fromNBT(NBTTagCompound nbt)
	{
		return new VillagerSaveData(nbt);
	}
	
	public EntityVillagerMCA applyToHuman(EntityVillagerMCA human)
	{
		human.setName(name);
		human.setHeadTexture(headTexture);
		human.setClothesTexture(clothesTexture);
		human.setProfession(professionId);
		human.setPersonality(personalityId);
		human.setGender(gender);
		human.setMarriageState(marriageState);
		human.setSpouseUUID(spouseUUID);
		human.setSpouseName(spouseName);
		human.setSpouseGender(spouseGender);
		human.setParentName(true, motherName);
		human.setParentUUID(true, motherUUID);
		human.setParentGender(true, motherGender);
		human.setParentName(false, fatherName);
		human.setParentUUID(false, fatherUUID);
		human.setParentGender(false, fatherGender);
		human.setScaleHeight(scaleHeight);
		human.setScaleWidth(scaleWidth);
		human.setIsInfected(isInfected);
		
		return human;
	}
	
	private VillagerSaveData(EntityVillagerMCA human, EntityPlayer requestingPlayer, UUID ownerUUID)
	{
		this.uuid = human.getUniqueID();
		this.ownerUUID = ownerUUID != null ? ownerUUID : new UUID(0L, 0L);
		this.name = human.getName();
		this.headTexture = human.getHeadTexture();
		this.clothesTexture = human.getClothesTexture();
		this.professionId = human.getProfessionEnum();
		this.personalityId = human.getPersonality();
		this.gender = human.getGender();
		this.marriageState = human.getMarriageState();
		this.spouseUUID = human.getSpouseUUID();
		this.spouseName = human.getSpouseName();
		this.spouseGender = human.getSpouseGender();
		this.babyState = human.getBabyState();
		this.isChild = human.getIsChild();
		this.age = human.getAge();
		this.motherName = human.getMotherName();
		this.motherGender = human.getMotherGender();
		this.motherUUID = human.getMotherUUID();
		this.fatherName = human.getFatherName();
		this.fatherGender = human.getFatherGender();
		this.fatherUUID = human.getFatherUUID();
		this.scaleHeight = human.getScaleHeight();
		this.scaleWidth = human.getScaleWidth();
		this.isInfected = human.getIsInfected();
		
		this.displayTitle = requestingPlayer != null ? human.getTitle(requestingPlayer) : name;
	}
	
	private VillagerSaveData(NBTTagCompound nbt)
	{
		this.uuid = nbt.getUniqueId("uuid");
		this.ownerUUID = nbt.getUniqueId("ownerUUID");
		this.name = nbt.getString("name");
		this.headTexture = nbt.getString("headTexture");
		this.clothesTexture = nbt.getString("clothesTexture");
		this.professionId = EnumProfession.getProfessionById(nbt.getInteger("professionId"));
		this.personalityId = EnumPersonality.getById(nbt.getInteger("personalityId"));
		this.gender = EnumGender.byId(nbt.getInteger("gender"));
		this.marriageState = EnumMarriageState.byId(nbt.getInteger("marriageState"));
		this.spouseUUID = nbt.getUniqueId("spouseUUID");
		this.spouseName = nbt.getString("spouseName");
		this.spouseGender = EnumGender.byId(nbt.getInteger("spouseGender"));
		this.babyState = EnumBabyState.fromId(nbt.getInteger("babyState"));
		this.isChild = nbt.getBoolean("isChild");
		this.age = nbt.getInteger("age");
		this.motherName = nbt.getString("motherName");
		this.fatherName = nbt.getString("fatherName");
		this.motherUUID = nbt.getUniqueId("motherUUID");
		this.fatherUUID = nbt.getUniqueId("fatherUUID");
		this.motherGender = EnumGender.byId(nbt.getInteger("motherGender"));
		this.fatherGender = EnumGender.byId(nbt.getInteger("fatherGender"));
		this.scaleHeight = nbt.getFloat("scaleHeight");
		this.scaleWidth = nbt.getFloat("scaleWidth");
		this.isInfected = nbt.getBoolean("isInfected");
		this.displayTitle = nbt.getString("displayTitle");
	}
	
	public void writeDataToNBT(NBTTagCompound nbt) 
	{
		nbt.setUniqueId("uuid", uuid);
		nbt.setUniqueId("ownerUUID", ownerUUID);
		nbt.setString("name", name);
		nbt.setString("headTexture", headTexture);
		nbt.setString("clothesTexture", clothesTexture);
		nbt.setInteger("professionId", professionId.getId());
		nbt.setInteger("personalityId", personalityId.getId());
		nbt.setInteger("gender", gender.getId());
		nbt.setInteger("marriageState", marriageState.getId());
		nbt.setUniqueId("spouseUUID", spouseUUID);
		nbt.setString("spouseName", spouseName);
		nbt.setInteger("spouseGender", spouseGender.getId());
		nbt.setInteger("babyState", babyState.getId());
		nbt.setBoolean("isChild", isChild);
		nbt.setInteger("age", age);
		nbt.setString("motherName", motherName);
		nbt.setString("fatherName", fatherName);
		nbt.setUniqueId("motherUUID", motherUUID);
		nbt.setUniqueId("fatherUUID", fatherUUID);
		nbt.setInteger("motherGender", motherGender.getId());
		nbt.setInteger("fatherGender", fatherGender.getId());
		nbt.setFloat("scaleHeight", scaleHeight);
		nbt.setFloat("scaleWidth", scaleWidth);
		nbt.setBoolean("isInfected", isInfected);
		nbt.setString("displayTitle", displayTitle);
	}
}
