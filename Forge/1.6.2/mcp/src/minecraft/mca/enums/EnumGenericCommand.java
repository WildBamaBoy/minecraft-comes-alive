/*******************************************************************************
 * EnumGenericCommand.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.enums;

/**
 * Enums stored here are all sent in the generic packet. Very little processing is involved.
 */
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
	ArrangedMarriageParticles("ArrangedMarriageParticles"),
	UpdateFurnace("UpdateFurnace"),
	MountHorse("MountHorse"),
	ClientSideCommand("ClientSideCommand"),
	ClientAddMarriageRequest("ClientAddMarriageRequest"),
	ClientAddBabyRequest("ClientAddBabyRequest"),
	ClientRemoveMarriageRequest("ClientRemoveMarriageRequest"),
	ClientRemoveBabyRequest("ClientRemoveBabyRequest");
	
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
