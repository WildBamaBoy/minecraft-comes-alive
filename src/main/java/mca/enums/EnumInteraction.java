package mca.enums;

public enum EnumInteraction 
{
	INTERACT(1, "interact"),
	FOLLOW(2, "follow"),
	STAY(3, "stay"),
	MOVE(4, "move"),
	TRADE(5, "trade"),
	SET_HOME(6, "sethome"),
	RIDE_HORSE(7, "ridehorse"),
	SPECIAL(8, "special"),
	PROCREATE(9, "procreate"),
	PICK_UP(10, "pickup"),
	TAKE_GIFT(11, "takegift"),
	
	CHAT(101, "chat", 1, 60, 5),
	JOKE(102, "joke", 1, 60, 5),
	GIFT(103, "gift"),
	SHAKE_HAND(104, "shakehand", 1, 80, 3),
	TELL_STORY(105, "tellstory", 1, 50, 7),
	FLIRT(106, "flirt", 2, 50, 8),
	HUG(107, "hug", 3, 30, 9),
	KISS(108, "kiss", 3, 15, 10);
	
	private int id;
	private String name;
	private int relationshipLevel;
	private int basePoints;
	private int baseChance;

	EnumInteraction(int id, String name)
	{
		this.id = id;
		this.name = name;
		this.relationshipLevel = 0;
		this.baseChance = 0;
		this.basePoints = 0;
	}
	
	EnumInteraction(int id, String name, int relationshipLevel, int baseChance, int basePoints)
	{
		this(id, name);
		this.relationshipLevel = relationshipLevel;
		this.baseChance = baseChance;
		this.basePoints = basePoints;
	}
	
	public static EnumInteraction fromId(int id)
	{
		for (EnumInteraction interaction : EnumInteraction.values())
		{
			if (interaction.id == id)
			{
				return interaction;
			}
		}
		
		return null;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getBasePoints()
	{
		return basePoints;
	}
	
	public int getBaseChance()
	{
		return baseChance;
	}

	public int getRelationshipLevel()
	{
		return relationshipLevel;
	}
	
	public int getBonusChanceForCurrentPoints(int hearts) 
	{
		int returnAmount = 0;

		switch (this)
		{
		case FLIRT: returnAmount = hearts >= 60 ? 5 : hearts >= 70 ? 10 : hearts >= 80 ? 15 : hearts >= 90 ? 20 : hearts >= 100 ? 25 : 0; break;
		case HUG: returnAmount = hearts >= 60 ? 10 : hearts >= 70 ? 15 : hearts >= 80 ? 20 : hearts >= 90 ? 25 : hearts >= 100 ? 30 : 0; break;
		case KISS: returnAmount = hearts >= 60 ? 20 : hearts >= 70 ? 30 : hearts >= 80 ? 40 : hearts >= 90 ? 50 : hearts >= 100 ? 60 : 0; break;
		}
		
		return returnAmount;
	}
}
