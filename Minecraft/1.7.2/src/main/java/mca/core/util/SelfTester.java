/*******************************************************************************
 * SelfTester.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
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

import com.radixshock.radixcore.core.RadixCore;

/**
 * Reads MCA's source code to find potential errors.
 */
public final class SelfTester 
{
	private final List<String> declaredVariables = new ArrayList<String>();

	private List<String> privateVariablesInPacketFile = new ArrayList<String>();
	private String lastPacketFile = "";
	private boolean startTestingPacketLine = false;

	/**
	 * Runs the self tester.
	 */
	public void doSelfTest()
	{
		String runningDirectory = RadixCore.getInstance().runningDirectory.replace("eclipse", "");

		declaredVariables.add("playerMemoryMap");

		MCA.getInstance().getLogger().log("-------------- Beginning self-test --------------");

		final File[] sourceDirs = new File[]
				{
				new File(runningDirectory + "/src/main/java/mca/api/chores"),
				new File(runningDirectory + "/src/main/java/mca/api/enums"),
				new File(runningDirectory + "/src/main/java/mca/api/registries"),
				new File(runningDirectory + "/src/main/java/mca/api/villagers"),
				new File(runningDirectory + "/src/main/java/mca/block"),
				new File(runningDirectory + "/src/main/java/mca/chore"),
				new File(runningDirectory + "/src/main/java/mca/client/gui"),
				new File(runningDirectory + "/src/main/java/mca/client/model"),
				new File(runningDirectory + "/src/main/java/mca/client/render"),
				new File(runningDirectory + "/src/main/java/mca/command"),
				new File(runningDirectory + "/src/main/java/mca/core"),
				new File(runningDirectory + "/src/main/java/mca/core/forge"),
				new File(runningDirectory + "/src/main/java/mca/core/util"),
				new File(runningDirectory + "/src/main/java/mca/core/util/object"),
				new File(runningDirectory + "/src/main/java/mca/entity"),
				new File(runningDirectory + "/src/main/java/mca/enums"),
				new File(runningDirectory + "/src/main/java/mca/inventory"),
				new File(runningDirectory + "/src/main/java/mca/item"),
				new File(runningDirectory + "/src/main/java/mca/lang"),
				new File(runningDirectory + "/src/main/java/mca/network"),
				new File(runningDirectory + "/src/main/java/mca/network/packets"),
				new File(runningDirectory + "/src/main/java/mca/tileentity")
				};

		FileInputStream fileStream;
		DataInputStream dataStream;
		BufferedReader reader;

		for (int loops = 0; loops < 4; loops++)
		{
			switch (loops)
			{
			case 0: MCA.getInstance().getLogger().log("Building list of declared variables in entity and gui files..."); break;
			case 1: MCA.getInstance().getLogger().log("Testing calls to language system for validity..."); break;
			case 2: MCA.getInstance().getLogger().log("Testing calls for field update validity..."); break;
			case 3:	MCA.getInstance().getLogger().log("Testing for calls to packet data without packet reference..."); break;
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

								try
								{
									switch (loops)
									{
									case 0: tryAddLineToDeclaredFields(readString); break;
									case 1: testLineInLanguageSystem(readString, sourceFile.getName(), lineNumber); break;
									case 2:	testLineForFieldUpdateValidity(readString, sourceFile.getName(), lineNumber); break;
									case 3: testLineForPacketReference(readString, sourceFile.getName(), lineNumber); break;
									}
								}

								catch (StringIndexOutOfBoundsException e)
								{
									continue;
								}

								catch (NullPointerException e)
								{
									continue;
								}
							}

							reader.close();
						}

						catch (Exception e)
						{
							if (loops == 2)
							{
								MCA.getInstance().getLogger().log(e);
							}

							continue;
						}
					}
				}
			}
			MCA.getInstance().getLogger().log("   Done.");
		}
		MCA.getInstance().getLogger().log("-------------- End self-test --------------");
	}

	private void testLineForPacketReference(String line, String fileName, int lineNumber) 
	{
		if (fileName.contains("Packet"))
		{
			if (!lastPacketFile.equals(fileName))
			{
				lastPacketFile = fileName;
				startTestingPacketLine = false;
				privateVariablesInPacketFile.clear();
			}

			if (!startTestingPacketLine)
			{
				if (line.contains(";") && (line.contains("private int") || line.contains("private boolean") || line.contains("private byte") || line.contains("private String") || line.contains("private double") || line.contains("private float")))
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

					line = line.replace("boolean ", "");
					line = line.replace("int ", "");
					line = line.replace("byte ", "");
					line = line.replace("String ", "");
					line = line.replace("double ", "");
					line = line.replace("float ", "");
					line = line.trim();
					
					privateVariablesInPacketFile.add(line);
				}
			}

			if (line.contains("onMessage"))
			{
				startTestingPacketLine = true;
			}
			
			if (startTestingPacketLine)
			{
				for (String variable : privateVariablesInPacketFile)
				{
					if (line.contains(variable) && !line.contains("packet." + variable))
					{
						MCA.getInstance().getLogger().log("\tReference to <" + variable + "> does not use a packet reference. Located at (" + fileName + ":" + lineNumber + ")");
					}
				}
			}
		}
	}

	private void testLineInLanguageSystem(String line, String fileName, int lineNumber)
	{
		if (line.contains("MCA.getInstance().getLanguageLoader().getString(") && !line.contains("%"))
		{
			line = line.trim();

			final boolean useCharacterType = line.contains("true");
			final int firstQuoteIndex = line.indexOf('"');
			final int nextQuoteIndex = line.indexOf('"', firstQuoteIndex + 1);
			final String phraseId = line.substring(firstQuoteIndex, nextQuoteIndex).replaceAll("\"", "");
			final String result = MCA.getInstance().getLanguageLoader().getString(null, null, phraseId, useCharacterType);

			if (result.contains("not found"))
			{
				MCA.getInstance().getLogger().log("\tPhrase <" + phraseId + "> at (" + fileName + ":" + lineNumber + ") was not found.");
			}
		}
	}

	private void testLineForFieldUpdateValidity(String line, String fileName, int lineNumber)
	{
		if (line.contains("new PacketSetFieldValue(") && !line.contains("line") && !line.contains("fieldName"))
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
				MCA.getInstance().getLogger().log("\tReference to <" + fieldName + "> cannot be found. Method called at (" + fileName + ":" + lineNumber + ")");
			}

			else if (wasFound && possiblyInvalid)
			{
				MCA.getInstance().getLogger().log("\tPossible invalid assignment value. <" + fieldName + "> provided as name, <" + providedFieldName + "> provided as assignment value. Method called at (" + fileName + ":" + lineNumber + ")");
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
