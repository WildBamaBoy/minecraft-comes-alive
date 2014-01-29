package mca.api;

public class VillagerInformation 
{
	/** The villager's name. */
	public String name;
	
	/** The villager's type, such as VillagerAdult, VillagerChild, or PlayerChild. */
	public EnumVillagerType type;
	
	/** 
	 * The villager's profession id. MCA reserves -1 through 7 by default.
	 * 
	 * -1 = Kid (Villager Child), 0 = Farmer, 1 = Librarian, 2 = Priest, 
	 * 3 = Smith, 4 = Butcher, 5 = Guard, 6 = Baker, 7 = Miner
	 */
	public int profession;

	/**
	 * True if this villager is actually holding a baby that is theirs.
	 * Will always be false for players' spouses.
	 */
	public boolean hasBaby;

	public boolean isMale;
	public boolean isEngaged;
	public boolean isMarriedToPlayer;
	public boolean isMarriedToVillager;
	
	/**
	 * Constructor
	 * 
	 * @param 	name
	 * @param 	type
	 * @param 	profession
	 * @param 	isMale
	 * @param 	isEngaged
	 * @param 	isMarriedToPlayer
	 * @param 	isMarriedToVillager
	 * @param 	hasBaby
	 */
	public VillagerInformation(String name, EnumVillagerType type, int profession, boolean isMale, 
			boolean isEngaged, boolean isMarriedToPlayer, boolean isMarriedToVillager, boolean hasBaby)
	{
		this.name = name;
		this.type = type;
		this.profession = profession;
		this.isMale = isMale;
		this.isEngaged = isEngaged;
		this.isMarriedToPlayer = isMarriedToPlayer;
		this.isMarriedToVillager = isMarriedToVillager;
		this.hasBaby = hasBaby;
	}
}
