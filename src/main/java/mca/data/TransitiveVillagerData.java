package mca.data;

import java.util.UUID;

import mca.entity.VillagerAttributes;
import mca.enums.EnumBabyState;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMovementState;
import mca.enums.EnumPersonality;
import mca.enums.EnumProfession;

/*
 * Used to carry around villager attributes without using the data manager, ex. in memorial objects.
 */
public class TransitiveVillagerData 
{
	private final String name;
	private final String headTexture;
	private final String clothesTexture;
	private final Integer profession;
	private final Integer personality;
	private final Integer gender;
	private final UUID spouseUUID;
	private final Integer spouseGender;
	private final String motherName;
	private final UUID motherUUID;
	private final Integer motherGender;
	private final String fatherName;
	private final String fatherUUID;
	private final Integer fatherGender;
	private final Integer babyState;
	private final Integer movementState;
	private final Boolean isChild;
	private final Integer age;
	private final Float scaleHeight;
	private final Float scaleWidth;
	private final Boolean doDisplay;
	private final Boolean isSwinging;
	private final Integer heldItemSlot;
	private final Boolean isInfected;
	private final Boolean doOpenInventory;
	private final Integer marriageState;
	
	private TransitiveVillagerData() 
	{
		
	}
	
	public TransitiveVillagerData(VillagerAttributes attributes)
	{
		
	}

	public String getName() 
	{
		return name;
	}

	public String getHeadTexture() 
	{
		return headTexture;
	}

	public String getClothesTexture() 
	{
		return clothesTexture;
	}

	public EnumProfession getProfession() 
	{
		return EnumProfession.getProfessionById(profession);
	}

	public EnumPersonality getPersonality() 
	{
		return EnumPersonality.getById(personality);
	}

	public EnumGender getGender() 
	{
		return EnumGender.byId(gender);
	}

	public UUID getSpouseUUID() 
	{
		return spouseUUID;
	}

	public EnumGender getSpouseGender() 
	{
		return EnumGender.byId(spouseGender);
	}

	public String getMotherName() 
	{
		return motherName;
	}

	public UUID getMotherUUID() 
	{
		return motherUUID;
	}

	public EnumGender getMotherGender() 
	{
		return EnumGender.byId(motherGender);
	}

	public String getFatherName() 
	{
		return fatherName;
	}

	public String getFatherUUID() 
	{
		return fatherUUID;
	}

	public EnumGender getFatherGender() 
	{
		return EnumGender.byId(fatherGender);
	}

	public EnumBabyState getBabyState() 
	{
		return EnumBabyState.fromId(babyState);
	}

	public EnumMovementState getMovementState() 
	{
		return EnumMovementState.fromId(movementState);
	}

	public Boolean getIsChild() 
	{
		return isChild;
	}

	public Integer getAge() 
	{
		return age;
	}

	public Float getScaleHeight() 
	{
		return scaleHeight;
	}

	public Float getScaleWidth() 
	{
		return scaleWidth;
	}

	public Boolean getDoDisplay() 
	{
		return doDisplay;
	}

	public Boolean getIsSwinging() 
	{
		return isSwinging;
	}

	public Integer getHeldItemSlot() 
	{
		return heldItemSlot;
	}

	public Boolean getIsInfected() 
	{
		return isInfected;
	}

	public Boolean getDoOpenInventory() 
	{
		return doOpenInventory;
	}

	public EnumMarriageState getMarriageState() 
	{
		return EnumMarriageState.byId(marriageState);
	}
}
