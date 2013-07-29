/*******************************************************************************
 * ModPropertiesManager.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Handles reading and writing properties that effect how the entire mod operates.
 */
public class ModPropertiesManager implements Serializable
{
	private transient Properties properties = new Properties();
	private transient FileInputStream inputStream   = null;
	private transient FileOutputStream outputStream = null;
	private transient File modPropertiesFile = null;
	private transient File configFolder = null;

	/** The properties and values stored within the mod properties file. */
	public ModPropertiesList modProperties = new ModPropertiesList();

	/**
	 * Constructor
	 */
	public ModPropertiesManager()
	{
		//Assign the location of the mod properties file and config folder.
		configFolder = new File(MCA.instance.runningDirectory + "/config/MCA/");
		modPropertiesFile = new File(MCA.instance.runningDirectory + "/config/MCA/ModProps.properties");

		//Ensure the config folder exists.
		if (!configFolder.exists())
		{
			configFolder.mkdirs();
		}

		//Now check if the mod properties file must be created or should be loaded.
		if (!modPropertiesFile.exists())
		{
			MCA.instance.log("File not found: " + MCA.instance.runningDirectory + "/config/MCA/ModProps.properties. " + "Creating new mod properties file...");
			saveModProperties();
		}

		else
		{
			loadModProperties();
		}
	}

	/**
	 * Saves the current mod properties to file.
	 */
	public void saveModProperties()
	{
		try
		{
			//Clear the properties instance to avoid saving unwanted variables.
			properties.clear();

			//Use reflection to get all the fields in this class. Only work with the ones whose name is prefixed with setting_.
			for (Field f : ModPropertiesList.class.getFields())
			{
				String fieldType = f.getType().toString();

				if (fieldType.contains("int"))
				{
					properties.put(f.getName(), f.get(modProperties).toString());
				}

				else if (fieldType.contains("boolean"))
				{
					properties.put(f.getName(), f.get(modProperties).toString());
				}
			}

			//Store information in the properties instance to file.
			outputStream = new FileOutputStream(modPropertiesFile);
			properties.store(outputStream, "MCA Mod Properties File - Change Item IDs and server settings here.");
			outputStream.close();

			MCA.instance.log("Mod properties successfully saved.");
		}

		catch (FileNotFoundException e)
		{
			MCA.instance.quitWithError("FileNotFoundException occurred while creating a new mod properties file.", e);
		}

		catch (IllegalAccessException e)
		{
			MCA.instance.quitWithError("IllegalAccessException occurred while creating a new mod properties file.", e);
		}

		catch (IOException e)
		{
			MCA.instance.quitWithError("IOException occurred while creating a new mod properties file.", e);
		}
	}

	/**
	 * Loads each value from the mod properties file into memory.
	 */
	public void loadModProperties()
	{
		MCA.instance.log("Loading mod properties...");

		try
		{
			//Clear the properties instance and get the mod's properties file.
			properties.clear();

			//Make sure the file exists.
			if (modPropertiesFile.exists())
			{
				//Load its properties into the properties instance.
				inputStream = new FileInputStream(modPropertiesFile);
				properties.load(inputStream);
				inputStream.close();

				//Loop through each field and assign the value stored in the properties.
				for (Field f : ModPropertiesList.class.getFields())
				{
					String fieldType = f.getType().toString();

					if (fieldType.contains("int"))
					{
						f.set(modProperties, Integer.parseInt(properties.getProperty(f.getName())));
					}

					else if (fieldType.contains("boolean"))
					{
						f.set(modProperties, Boolean.parseBoolean(properties.getProperty(f.getName())));
					}
				}
			}

			else //The mod properties file does not exist. It was either deleted by the user or hasn't been created yet.
			{
				MCA.instance.log("Mod properties file was not found.");
				saveModProperties();
			}
		}

		//The user didn't edit the file correctly or assigned an invalid ID for an item or block. A new property could have also been added.
		catch (NumberFormatException e)
		{
			MCA.instance.log("NumberFormatException while reading mod properties. You edited the file incorrectly or a new property has been added to MCA.");
			resetModProperties();
			saveModProperties();
		}

		catch (FileNotFoundException e)
		{
			MCA.instance.quitWithError("MCA: FileNotFoundException occurred while loading the mod properties file.", e);
		}

		catch (IllegalAccessException e)
		{
			MCA.instance.quitWithError("MCA: IllegalAccessException occurred while loading the new mod properties file.", e);
		}

		catch (IOException e)
		{
			MCA.instance.quitWithError("MCA: IOException occurred while loading the new mod properties file.", e);
		}
	}

	/** Resets all mod properties back to their default values.*/
	public void resetModProperties()
	{
		modProperties = new ModPropertiesList();
	}

	@Override
	public boolean equals(Object obj)
	{
		try
		{
			if (obj instanceof ModPropertiesManager)
			{
				ModPropertiesManager modPropertiesManager = (ModPropertiesManager)obj;

				for (Field f : ModPropertiesList.class.getFields())
				{
					//Only check item and block IDs, ignore grow up times, etc.
					if (f.getName().contains("ID"))
					{
						int valueInMe = (Integer) f.get(this.modProperties);
						int valueInOther = (Integer) f.get(modPropertiesManager.modProperties);

						if (valueInMe != valueInOther)
						{
							MCA.instance.log("Mod property value mismatch! Client value: " + f.getName() + " = " + valueInOther + ". " +
									"Server value: " + f.getName() + " = " + valueInMe);
							return false;
						}
					}
				}

				return true;
			}

			else
			{
				return false;
			}
		}

		catch (Throwable e)
		{
			MCA.instance.log(e);
			return false;
		}
	}
}
