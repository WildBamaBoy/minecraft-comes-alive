/*******************************************************************************
 * EnumMoodChangeContext.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

/**
 * Used to determine which mood is modified.
 */
public enum EnumMoodChangeContext 
{
	GoodInteraction,
	BadInteraction,
	HitByPlayer,
	SleepInterrupted,
	SleepCycle,
	Working,
	MoodCycle,
	WitnessDeath;
}
