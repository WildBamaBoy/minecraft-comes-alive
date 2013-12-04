/*******************************************************************************
 * Constants.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core;

import net.minecraft.item.Item;

/**
 * Defines constants used by MCA.
 */
public final class Constants 
{
	/** MCA's current version. */
	public static final String VERSION = "3.6.0";
	
	//Ticks per second, minute, and hour.
	public static final int TICKS_SECOND = 20;
	public static final int TICKS_MINUTE = 1200;
	public static final int TICKS_HOUR   = 72000;
	
	//Animal IDs for hunting chore.
	public static final byte ID_ANIMAL_SHEEP = 0;
	public static final byte ID_ANIMAL_COW = 1;
	public static final byte ID_ANIMAL_PIG = 2;
	public static final byte ID_ANIMAL_CHICKEN = 3;
	public static final byte ID_ANIMAL_WOLF = 4;
	
	/**Animal data for hunting chore.
	 * Index zero contains the animal's ID.
	 * Index one contains the item ID used to tame the animal.
	 * Index two contains the probability of successful taming.*/
	public static final int[][] ANIMAL_DATA = 
		{
		{Constants.ID_ANIMAL_SHEEP, Item.wheat.itemID, 50},
		{Constants.ID_ANIMAL_COW, Item.wheat.itemID, 40},
		{Constants.ID_ANIMAL_PIG, Item.carrot.itemID, 70},
		{Constants.ID_ANIMAL_CHICKEN, Item.seeds.itemID, 70},
		{Constants.ID_ANIMAL_WOLF, Item.bone.itemID, 33},
		};
	
	//Gui IDs
	public static final byte ID_GUI_INVENTORY = 0;
	public static final byte ID_GUI_GAMEOVER = 1;
	public static final byte ID_GUI_PLAYERCHILD = 2;
	public static final byte ID_GUI_SPOUSE = 3;
	public static final byte ID_GUI_ADULT = 4;
	public static final byte ID_GUI_CHILD = 5;
	public static final byte ID_GUI_NAMECHILD = 7;
	public static final byte ID_GUI_SETUP = 8;
	public static final byte ID_GUI_DIVORCECOUPLE = 9;
	public static final byte ID_GUI_TOMBSTONE = 10;
	public static final byte ID_GUI_EDITOR = 11;
	public static final byte ID_GUI_LOSTRELATIVE = 12;

	//Movement speeds.
	public static final float SPEED_SNEAK = 0.4F;
	public static final float SPEED_WALK = 0.6F;
	public static final float SPEED_RUN = 0.7F;
	public static final float SPEED_SPRINT = 0.8F;

	//Hitbox sizes.
	public static final float HEIGHT_ADULT = 1.8F;
	public static final float WIDTH_ADULT = 0.6F;
	
	//Model sizes.
	public static final float SCALE_MALE_ADULT = 0.9375F;
	public static final float SCALE_FEMALE_ADULT = 0.915F;
	public static final float SCALE_MAX = 1.1F;
	public static final float SCALE_MIN = 0.85F;
	
	//Colors & formatting
	private static final char SECTION_SIGN = '§';
	
	public static final String COLOR_BLACK = SECTION_SIGN + "0";
	public static final String COLOR_DARKBLUE = SECTION_SIGN + "1";
	public static final String COLOR_DARKGREEN = SECTION_SIGN + "2";
	public static final String COLOR_DARKAQUA = SECTION_SIGN + "3";
	public static final String COLOR_DARKRED = SECTION_SIGN + "4";
	public static final String COLOR_PURPLE = SECTION_SIGN + "5";
	public static final String COLOR_GOLD = SECTION_SIGN + "6";
	public static final String COLOR_GRAY = SECTION_SIGN + "7";
	public static final String COLOR_DARKGRAY = SECTION_SIGN + "8";
	public static final String COLOR_BLUE = SECTION_SIGN + "9";
	public static final String COLOR_GREEN = SECTION_SIGN + "A";
	public static final String COLOR_AQUA = SECTION_SIGN + "B";
	public static final String COLOR_RED = SECTION_SIGN + "C";
	public static final String COLOR_LIGHTPURPLE = SECTION_SIGN + "D";
	public static final String COLOR_YELLOW = SECTION_SIGN + "E";
	public static final String COLOR_WHITE = SECTION_SIGN + "F";
	
	public static final String FORMAT_OBFUSCATED = SECTION_SIGN + "k";
	public static final String FORMAT_BOLD = SECTION_SIGN + "l";
	public static final String FORMAT_STRIKETHROUGH = SECTION_SIGN + "m";
	public static final String FORMAT_UNDERLINE = SECTION_SIGN + "n";
	public static final String FORMAT_ITALIC = SECTION_SIGN + "o";
	public static final String FORMAT_RESET = SECTION_SIGN + "r";
	
	private Constants() { }
}
