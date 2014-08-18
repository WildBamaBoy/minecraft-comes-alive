/*******************************************************************************
 * CommandModProps.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.command;

import java.lang.reflect.Field;

import mca.core.MCA;
import mca.core.ModPropertiesList;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

import com.radixshock.radixcore.constant.Font.Color;

/**
 * Defines the mod props command and what it does.
 */
public class CommandModProps extends AbstractCommand
{
	@Override
	public String getCommandUsage(ICommandSender sender) 
	{
		return "/mca.modprops <set/get> <name> <value (get only)>";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) 
	{
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() 
	{
		return 4;
	}

	@Override
	public String getCommandName() 
	{
		return "mca.modprops";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{
		if (arguments[0].trim().equalsIgnoreCase("set") || arguments[0].trim().equalsIgnoreCase("get") || arguments[0].trim().equalsIgnoreCase("list"))
		{
			final boolean doSet = arguments[0].trim().equalsIgnoreCase("set");
			final boolean doGet = arguments[0].trim().equalsIgnoreCase("get");
			final boolean doList = arguments[0].trim().equalsIgnoreCase("list");

			try
			{
				if (doSet)
				{
					setModProperty(sender, arguments);
				}

				else if (doGet)
				{
					getModProperty(sender, arguments);
				}
				
				else if (doList)
				{
					listModProperties(sender, arguments);
				}
			}

			catch (WrongUsageException e)
			{
				throw e;
			}
			
			catch (Exception e)
			{
				MCA.getInstance().getLogger().log(e);
				sender.addChatMessage(new ChatComponentText(Color.RED + "An unknown exception has occured."));
			}
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}

	private void setModProperty(ICommandSender sender, String[] arguments) throws IllegalArgumentException, IllegalAccessException
	{
		if (arguments.length == 3)
		{
			final String stringToFind = arguments[1].trim();

			for (final Field field : ModPropertiesList.class.getDeclaredFields())
			{
				if (field.getName().equalsIgnoreCase(stringToFind))
				{
					if (field.getType().toString().contains("int"))
					{
						field.set(MCA.getInstance().getModProperties(), Integer.parseInt(arguments[2]));
					}

					else if (field.getType().toString().contains("double"))
					{
						field.set(MCA.getInstance().getModProperties(), Double.parseDouble(arguments[2]));
					}

					else if (field.getType().toString().contains("float"))
					{
						field.set(MCA.getInstance().getModProperties(), Float.parseFloat(arguments[2]));
					}

					else if (field.getType().toString().contains("String"))
					{
						field.set(MCA.getInstance().getModProperties(), arguments[2]);
					}

					else if (field.getType().toString().contains("boolean"))
					{
						field.set(MCA.getInstance().getModProperties(), Boolean.parseBoolean(arguments[2]));
					}

					sender.addChatMessage(new ChatComponentText(Color.YELLOW + "Value of " + arguments[1] + " set to: " + arguments[2]));
					return;
				}
			}

			sender.addChatMessage(new ChatComponentText(Color.RED + "Mod property not found: " + arguments[1]));
			MCA.getInstance().modPropertiesManager.saveModProperties();
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}

	private void getModProperty(ICommandSender sender, String[] arguments) throws IllegalArgumentException, IllegalAccessException
	{
		if (arguments.length == 2)
		{
			final String stringToFind = arguments[1].trim();

			for (final Field field : ModPropertiesList.class.getDeclaredFields())
			{
				if (field.getName().equalsIgnoreCase(stringToFind))
				{
					sender.addChatMessage(new ChatComponentText(Color.YELLOW + "Value of " + arguments[1] + " equals: " + field.get(MCA.getInstance().getModProperties()).toString()));
					return;
				}
			}

			sender.addChatMessage(new ChatComponentText(Color.RED + "Mod property not found: " + arguments[1]));
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
	
	private void listModProperties(ICommandSender sender, String[] arguments) throws IllegalArgumentException, IllegalAccessException
	{
		if (arguments.length == 1)
		{
			for (final Field field : ModPropertiesList.class.getDeclaredFields())
			{
				sender.addChatMessage(new ChatComponentText(Color.YELLOW + "<" + field.getName() + "> = " + field.get(MCA.getInstance().getModProperties()).toString()));
			}
		}

		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
