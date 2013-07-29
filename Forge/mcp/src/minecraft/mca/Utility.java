/*******************************************************************************
 * Utility.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Contains various utility methods.
 */
public final class Utility 
{
	/**
	 * Deflates a byte array.
	 * 
	 * @param 	input	The byte array to be deflated.
	 * 
	 * @return	Deflated byte array.
	 */
	public static byte[] compressBytes(byte[] input)
	{
		try
		{
			Deflater deflater = new Deflater();
			deflater.setLevel(Deflater.BEST_COMPRESSION);
			deflater.setInput(input);

			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(input.length);
			deflater.finish();

			byte[] buffer = new byte[1024];

			while(!deflater.finished())
			{
				int count = deflater.deflate(buffer);
				byteOutput.write(buffer, 0, count);
			}

			byteOutput.close();
			return byteOutput.toByteArray();
		}

		catch (Throwable e)
		{
			MCA.instance.quitWithError("Error compressing byte array.", e);
			return null;
		}
	}

	/**
	 * Inflates a deflated byte array.
	 * 
	 * @param 	input	The byte array to be deflated.
	 * 
	 * @return	Decompressed byte array.
	 */
	public static byte[] decompressBytes(byte[] input)
	{
		try
		{
			Inflater inflater = new Inflater();
			inflater.setInput(input);

			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(input.length);

			byte[] buffer = new byte[1024];

			while(!inflater.finished())
			{
				int count = inflater.inflate(buffer);
				byteOutput.write(buffer, 0, count);
			}

			byteOutput.close();
			return byteOutput.toByteArray();
		}

		catch (Throwable e)
		{
			MCA.instance.quitWithError("Error decompressing byte array.", e);
			return null;
		}
	}

	/**
	 * Deletes a path and all files and folders within.
	 * 
	 * @param 	file	The path to delete.
	 */
	public static void deletePath(File file)
	{
		if (file.isDirectory())
		{
			if (file.list().length == 0)
			{
				file.delete();
			}

			else
			{
				String files[] = file.list();

				for (String temp : files)
				{
					File fileDelete = new File(file, temp);
					deletePath(fileDelete);
				}

				if (file.list().length == 0)
				{
					file.delete();
				}
			}
		}

		else
		{
			file.delete();
		}
	}

	/**
	 * Counts lines of code in source distribution.
	 */
	public static void countLinesOfCode()
	{
		int lines = 0;
		FileInputStream fileStream;
		DataInputStream inputStream;
		BufferedReader bufferedreader;

		String sourceDir = "D:/Programming/Minecraft Comes Alive/Forge/mcp/src/minecraft/mods/MCA/";

		for (File file : new File(sourceDir).listFiles())
		{
			if (file.isFile())
			{
				try
				{
					String readString = "";

					fileStream = new FileInputStream(file);
					inputStream = new DataInputStream(fileStream);
					bufferedreader = new BufferedReader(new InputStreamReader(inputStream));

					while ((readString = bufferedreader.readLine()) != null)  
					{
						lines++;
					}
				}

				catch (Throwable e)
				{
					MCA.instance.log(e);
					continue;
				}
			}
		}

		System.out.println("Lines of code: " + lines);
	}

	/**
	 * Tests all valid lines within the source for errors when requested from the language system.
	 */
	public static void testLanguage()
	{
		EntityBase dummyEntity = new EntityVillagerAdult();
		String sourceDir = "D:/Programming/Minecraft Comes Alive/Development/src/minecraft/mca/";
		FileInputStream fileStream;;
		DataInputStream in;
		BufferedReader br;
		int lineNumber = 0;
		System.out.println(sourceDir);

		for (File file : new File(sourceDir).listFiles())
		{
			lineNumber = 0;
			String readString = "";

			try
			{
				fileStream = new FileInputStream(file);
				in = new DataInputStream(fileStream);
				br = new BufferedReader(new InputStreamReader(in));

				while ((readString = br.readLine()) != null)  
				{
					lineNumber++;

					if (readString.contains("Localization.getString(") && readString.contains("static") == false)
					{
						readString = readString.trim();

						boolean useCharacterType = readString.contains("true");
						int firstQuoteIndex = readString.indexOf('"');
						int nextQuoteIndex = readString.indexOf('"', firstQuoteIndex + 1);

						String validString = readString.substring(firstQuoteIndex, nextQuoteIndex).replaceAll("\"", "");
						String result = Localization.getString(null, dummyEntity, validString, useCharacterType);

						if (result.contains("not found") || result.contains("(Parsing error)"))
						{
							System.out.println(result + " in " + file.getName() + " line " + lineNumber);
						}

						//System.out.println("Result of: " + validString + " in " + file.getName() + " = \t\t\t" + getString(null, validString));
					}
				}
			}

			catch (Throwable e)
			{
				System.out.println("Error on " + readString);
				continue;
			}
		}
	}
}
