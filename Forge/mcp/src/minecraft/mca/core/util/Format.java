/*******************************************************************************
 * Format.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

/**
 * Contains format codes for messages displayed on screen.
 */
public final class Format
{
	private static final String SIGN = "\u00a7";
	
	public static final String OBFUSCATED = SIGN + "k";
	public static final String BOLD = SIGN + "l";
	public static final String STRIKETHROUGH = SIGN + "m";
	public static final String UNDERLINE = SIGN + "n";
	public static final String ITALIC = SIGN + "o";
	public static final String RESET = SIGN + "r";
}
