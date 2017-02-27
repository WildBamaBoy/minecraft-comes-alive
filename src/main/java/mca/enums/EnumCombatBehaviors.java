package mca.enums;

import mca.core.MCA;

public enum EnumCombatBehaviors
{
	METHOD_MELEE_AND_RANGED(100, "combat.meleeandranged"),
	METHOD_MELEE_ONLY(101, "combat.meleeonly"),
	METHOD_RANGED_ONLY(102, "combat.rangedonly"),
	METHOD_DO_NOT_FIGHT(103, "combat.donotfight"),
	TRIGGER_ALWAYS(201, "combat.trigger.always"),
	TRIGGER_PLAYER_TAKE_DAMAGE(202, "combat.trigger.playertakedamage"),
	TRIGGER_PLAYER_DEAL_DAMAGE(203, "combat.trigger.playerattacks"),
	TARGET_PASSIVE_MOBS(301, "combat.target.passive"),
	TARGET_HOSTILE_MOBS(302, "combat.target.hostile"),
	TARGET_PASSIVE_OR_HOSTILE_MOBS(303, "combat.target.either");

	private int numericId;
	private String parserId;

	EnumCombatBehaviors(int numericId, String parserId)
	{
		this.numericId = numericId;
		this.parserId = parserId;
	}

	public int getNumericId()
	{
		return numericId;
	}

	public String getParsedText()
	{
		return MCA.getLocalizer().getString(parserId);
	}

	public static EnumCombatBehaviors getById(int id)
	{
		for (EnumCombatBehaviors method : values())
		{
			if (method.getNumericId() == id)
			{
				return method;
			}
		}

		return null;
	}
}