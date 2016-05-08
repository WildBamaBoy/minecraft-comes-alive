package mca.enums;

import java.util.ArrayList;
import java.util.List;

import mca.core.MCA;
import radixcore.util.RadixMath;

public enum EnumProfession 
{
	Farmer       (0,  EnumProfessionGroup.Farmer,   1),
	Fisherman    (1,  EnumProfessionGroup.Farmer,   2),
	Shepherd     (2,  EnumProfessionGroup.Farmer,   3),
	Fletcher     (3,  EnumProfessionGroup.Farmer,   4),
	Librarian    (4,  EnumProfessionGroup.Librarian,1),
	Cleric       (5,  EnumProfessionGroup.Priest,   1),
	Armorer      (6,  EnumProfessionGroup.Smith,    1),
	WeaponSmith  (7,  EnumProfessionGroup.Smith,    2),
	ToolSmith    (8,  EnumProfessionGroup.Smith,    3),
	Butcher      (9,  EnumProfessionGroup.Butcher,  1),
	Baker        (10, EnumProfessionGroup.Baker,    1),
	Leatherworker(11, EnumProfessionGroup.Butcher,  2),
	Guard        (12, EnumProfessionGroup.Guard,    1),
	Archer       (13, EnumProfessionGroup.Guard,    1),
	Miner        (14, EnumProfessionGroup.Miner,    1),
	Mason        (15, EnumProfessionGroup.Miner,    1),
	Child        (16, EnumProfessionGroup.Child,    1);

	private int id;
	private int vanillaCareerId;
	private EnumProfessionGroup skinGroup;

	private EnumProfession(int id, EnumProfessionGroup skinGroup, int vanillaCareerId)
	{
		this.id = id;
		this.skinGroup = skinGroup;
		this.vanillaCareerId = vanillaCareerId;
	}

	public int getId()
	{
		return id;
	}

	public static EnumProfession getNewProfessionFromVanilla(int id)
	{
		switch (id)
		{
		case 0: return getRandomByGroup(EnumProfessionGroup.Farmer, EnumProfessionGroup.Miner);
		case 1: return getRandomByGroup(EnumProfessionGroup.Librarian);
		case 2: return getRandomByGroup(EnumProfessionGroup.Priest);
		case 3: return getRandomByGroup(EnumProfessionGroup.Smith);
		case 4: return getRandomByGroup(EnumProfessionGroup.Butcher, EnumProfessionGroup.Baker);
		default:
			return getRandomByGroup(EnumProfessionGroup.Farmer);
		}
	}

	public static EnumProfession getRandomByGroup(EnumProfessionGroup... groups)
	{
		List<EnumProfession> groupProfessions = new ArrayList<EnumProfession>();

		for (EnumProfession profession : EnumProfession.values())
		{
			for (EnumProfessionGroup group : groups)
			{
				if (profession.skinGroup == group)
				{
					groupProfessions.add(profession);
				}
			}
		}

		return groupProfessions.get(RadixMath.getNumberInRange(0, groupProfessions.size() - 1));
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

	public EnumProfessionGroup getSkinGroup()
	{
		return skinGroup;
	}

	public static List<Integer> getListOfIds()
	{
		List<Integer> returnList = new ArrayList<Integer>();
		
		for (EnumProfession profession : EnumProfession.values())
		{
			returnList.add(profession.id);
		}
		
		return returnList;
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

	public int getVanillaCareerId() {
		return vanillaCareerId;
	}
}
