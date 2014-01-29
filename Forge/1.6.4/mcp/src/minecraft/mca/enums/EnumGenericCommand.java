package mca.enums;

public enum EnumGenericCommand 
{
	SetTexture("SetTexture"),
	SwingArm("SwingArm"),
	SyncEditorSettings("SyncEditorSettings"),
	AddAI("AddAI"),
	NotifyPlayer("NotifyPlayer"),
	StartTrade("StartTrade"), 
	KillEntity("KillEntity"),
	BroadcastKillEntity("BroadcastKillEntity"),
	SetPosition("SetPosition"),
	StopJumping("StopJumping"),
	ArrangedMarriageParticles("ArrangedMarriageParticles");

	/** The actual string value of the enum constant */
	private String value;
	
	private EnumGenericCommand(String value)
	{
		this.value = value;
	}
	
	public String getValue()
	{
		return this.value;
	}
	
	/**
	 * Gets the enum constant from a provided value.
	 * 
	 * @param 	value	The value to compare against all enum constants.
	 * 
	 * @return	EnumGenericCommand whose value equals the provided value.
	 */
	public static EnumGenericCommand getEnum(String value) 
	{
		if (value != null)
		{
			for (final EnumGenericCommand command : EnumGenericCommand.values())
			{
				if (command.getValue().equals(value))
				{
					return command;
				}
			}
		}
		
		return null;
	}
}
