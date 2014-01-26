/*******************************************************************************
 * SelfTester.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.core.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import mca.core.MCA;

/**
 * Reads MCA's source code to find potential errors.
 */
public class SelfTester 
{
	private final List<String> declaredVariables = new ArrayList<String>();

	public void doSelfTest()
	{
		declaredVariables.add("playerMemoryMap");

		MCA.getInstance().log("-------------- Beginning self-test --------------");

		final File[] sourceDirs = new File[]
				{
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/api"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/block"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/chore"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/client/gui"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/client/model"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/client/render"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/command"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/core"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/core/forge"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/core/io"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/core/util"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/entity"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/enums"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/inventory"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/item"),
				new File(MCA.getInstance().runningDirectory + "/../src/minecraft/mca/tileentity")
				};

		FileInputStream fileStream;
		DataInputStream dataStream;
		BufferedReader reader;

		for (int loops = 0; loops < 3; loops++)
		{
			switch (loops)
			{
			case 0: MCA.getInstance().log("Building list of declared variables in entity and gui files..."); break;
			case 1: MCA.getInstance().log("Testing calls to language system for validity..."); break;
			case 2:	MCA.getInstance().log("Testing for invalid calls to createFieldUpdatePacket..."); break;
			}

			int lineNumber = 0;

			for (final File sourceDir : sourceDirs)
			{
				for (final File sourceFile : sourceDir.listFiles())
				{
					if (sourceFile.isFile())
					{
						lineNumber = 0;
						String readString = "";

						try
						{
							fileStream = new FileInputStream(sourceFile);
							dataStream = new DataInputStream(fileStream);
							reader = new BufferedReader(new InputStreamReader(dataStream));

							while ((readString = reader.readLine()) != null)  
							{
								lineNumber++;

								switch (loops)
								{
								case 0: tryAddLineToDeclaredFields(readString); break;
								case 1: testLineInLanguageSystem(readString, sourceFile.getName(), lineNumber); break;
								case 2:	testLineForFieldUpdateValidity(readString, sourceFile.getName(), lineNumber); break;
								}
							}

							reader.close();
						}

						catch (Exception e)
						{
							if (loops == 2)
							{
								MCA.getInstance().log(e);
							}

							continue;
						}
					}
				}
			}
			MCA.getInstance().log("   Done.");
		}
		MCA.getInstance().log("-------------- End self-test --------------");
	}

	private void testLineInLanguageSystem(String line, String fileName, int lineNumber)
	{
		if (line.contains("LanguageHelper.getString(") && !line.contains("%"))
		{
			line = line.trim();

			final boolean useCharacterType = line.contains("true");
			final int firstQuoteIndex = line.indexOf('"');
			final int nextQuoteIndex = line.indexOf('"', firstQuoteIndex + 1);
			final String phraseId = line.substring(firstQuoteIndex, nextQuoteIndex).replaceAll("\"", "");
			final String result = LanguageHelper.getString(null, null, phraseId, useCharacterType);

			if (result.contains("not found"))
			{
				MCA.getInstance().log("\tPhrase <" + phraseId + "> at (" + fileName + ":" + lineNumber + ") was not found.");
			}
		}
	}

	private void testLineForFieldUpdateValidity(String line, String fileName, int lineNumber)
	{
		if (line.contains("PacketHandler.createFieldValuePacket(") && !line.contains("line") && !line.contains("fieldName"))
		{
			line = line.trim();
			final int firstQuoteIndex = line.indexOf('"');
			final int nextQuoteIndex = line.indexOf('"', firstQuoteIndex + 1);
			final int endParenthesisIndex = line.indexOf(')', nextQuoteIndex + 1);
			final String fieldName = line.substring(firstQuoteIndex, nextQuoteIndex).replaceAll("\"", "");
			final String providedFieldName = line.substring(nextQuoteIndex + 2, endParenthesisIndex).trim(); 

			boolean wasFound = false;
			boolean possiblyInvalid = true;
			for (final String string : declaredVariables)
			{
				if (string.equals(fieldName))
				{
					wasFound = true;

					if (providedFieldName.contains(fieldName))
					{
						possiblyInvalid = false;
					}

					break;
				}
			}

			if (!wasFound)
			{
				MCA.getInstance().log("\tReference to <" + fieldName + "> cannot be found. Method called at (" + fileName + ":" + lineNumber + ")");
			}

			else if (wasFound && possiblyInvalid)
			{
				MCA.getInstance().log("\tPossible invalid assignment value. <" + fieldName + "> provided as name, <" + providedFieldName + "> provided as assignment value. Method called at (" + fileName + ":" + lineNumber + ")");
			}
		}
	}

	private void tryAddLineToDeclaredFields(String line)
	{
		if (line.contains(";") && (line.contains("public int") || line.contains("public boolean") || line.contains("public byte") || line.contains("public String") || line.contains("public double") || line.contains("public float")))
		{
			line = line.trim().replace("public ", "").trim();

			if (line.contains("="))
			{
				line = line.substring(0, line.indexOf("=")).trim() + ";";
			}

			if (line.contains(";"))
			{
				final int spaceIndex = line.indexOf(' ');
				final int semicolonIndex = line.indexOf(';');
				line = line.substring(spaceIndex + 1, semicolonIndex);
			}

			declaredVariables.add(line);
		}
	}
}
