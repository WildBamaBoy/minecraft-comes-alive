package mca.enums;

import mca.core.MCA;
import radixcore.util.RadixMath;

public enum EnumProfession 
{
	Farmer(1, EnumProfessionGroup.Farmer),
	Fisherman(2, EnumProfessionGroup.Farmer),
	Shepherd(3, EnumProfessionGroup.Farmer),
	Fletcher(4, EnumProfessionGroup.Farmer),
	Librarian(5, EnumProfessionGroup.Librarian),
	Cleric(6, EnumProfessionGroup.Priest),
	Armorer(7, EnumProfessionGroup.Smith),
	WeaponSmith(8, EnumProfessionGroup.Smith),
	ToolSmith(9, EnumProfessionGroup.Smith),
	Butcher(10, EnumProfessionGroup.Butcher),
	Baker(11, EnumProfessionGroup.Baker),
	Leatherworker(12, EnumProfessionGroup.Butcher),
	Guard(13, EnumProfessionGroup.Guard),
	Archer(14, EnumProfessionGroup.Guard),
	Miner(15, EnumProfessionGroup.Miner),
	Spouse(16, EnumProfessionGroup.Farmer),
	Mason(17, EnumProfessionGroup.Miner),
	Child(18, EnumProfessionGroup.Child);

	private int id;
	private EnumProfessionGroup skinGroup;
	
	private EnumProfession(int id, EnumProfessionGroup skinGroup)
	{
		this.id = id;
		this.skinGroup = skinGroup;
	}

	public int getId()
	{
		return id;
	}

	public static EnumProfession getProfessionById(int id)
	{
		for (EnumProfession profession : EnumProfession.values())
		{
			if (profession.getId() == id)
			{
				return profession;
			}
		}

		return null;
	}

	public static EnumProfession getAtRandom()
	{
		EnumProfession[] values = EnumProfession.values();
		int returnIndex = RadixMath.getNumberInRange(0, EnumProfession.values().length - 1);
		return values[returnIndex];
	}

	public String getUserFriendlyForm() 
	{
		return MCA.getLanguageManager().getString(getLocalizationId());
	}
	
	private String getLocalizationId()
	{
		switch (this)
		{
		case Archer:
			return "profession.archer";
		case Armorer:
			return "profession.armorer";
		case Butcher:
			return "profession.butcher";
		case Cleric:
			return "profession.cleric";
		case Farmer:
			return "profession.farmer";
		case Fisherman:
			return "profession.fisherman";
		case Fletcher:
			return "profession.fletcher";
		case Guard:
			return "profession.guard";
		case Spouse:
			return "profession.househusband";
		case Leatherworker:
			return "profession.leatherworker";
		case Librarian:
			return "profession.librarian";
		case Shepherd:
			return "profession.shepherd";
		case ToolSmith:
			return "profession.toolsmith";
		case WeaponSmith:
			return "profession.weaponsmith";
		case Miner:
			return "profession.miner";
		case Mason:
			return "profession.mason";
		case Baker:
			return "profession.baker";
		case Child:
			return "profession.child";
		}
		
		return "";
	}
	
	public EnumProfessionGroup getSkinGroup()
	{
		return skinGroup;
	}
}
