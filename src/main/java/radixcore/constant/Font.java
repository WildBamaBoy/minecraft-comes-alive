/*******************************************************************************
 * Font.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package radixcore.constant;

/**
 * Defines font constants such as colors and formatting.
 */
public final class Font
{
	public static final String SECTION_SIGN = "\u00a7";

	private Font()
	{
	}

	/** All possible font colors */
	public final class Color
	{
		public static final String BLACK = SECTION_SIGN + "0";
		public static final String DARKBLUE = SECTION_SIGN + "1";
		public static final String DARKGREEN = SECTION_SIGN + "2";
		public static final String DARKAQUA = SECTION_SIGN + "3";
		public static final String DARKRED = SECTION_SIGN + "4";
		public static final String PURPLE = SECTION_SIGN + "5";
		public static final String GOLD = SECTION_SIGN + "6";
		public static final String GRAY = SECTION_SIGN + "7";
		public static final String DARKGRAY = SECTION_SIGN + "8";
		public static final String BLUE = SECTION_SIGN + "9";
		public static final String GREEN = SECTION_SIGN + "A";
		public static final String AQUA = SECTION_SIGN + "B";
		public static final String RED = SECTION_SIGN + "C";
		public static final String LIGHTPURPLE = SECTION_SIGN + "D";
		public static final String YELLOW = SECTION_SIGN + "E";
		public static final String WHITE = SECTION_SIGN + "F";
	}

	/** All possible font formats. */
	public final class Format
	{
		public static final String OBFUSCATED = SECTION_SIGN + "k";
		public static final String BOLD = SECTION_SIGN + "l";
		public static final String STRIKE = SECTION_SIGN + "m";
		public static final String UNDERLINE = SECTION_SIGN + "n";
		public static final String ITALIC = SECTION_SIGN + "o";
		public static final String RESET = SECTION_SIGN + "r";
	}
}
