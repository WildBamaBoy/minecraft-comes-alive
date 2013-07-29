/*******************************************************************************
 * GuiSpecialDivorceCouple.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI shown when the player wants to divorce a couple.
 */
@SideOnly(Side.CLIENT)
public class GuiSpecialDivorceCouple extends Gui
{
	/** Map containing all married villagers. */
	public Map<String, String> marriedVillagers = null;
	
	/** The current index in the married villagers map. */
	public int currentIndex = 0;
	
	/** The max index in the married villagers map. */
	public int maxIndex = 0;

	private GuiButton selectedCoupleButton;
	private GuiButton shiftIndexDownButton;
	private GuiButton shiftIndexUpButton;

	private GuiButton divorceButton;
	private GuiButton backButton;
	private GuiButton exitButton;

	/**
	 * Constructor
	 * 
	 * @param 	player	The player that opened this GUI.
	 */
	public GuiSpecialDivorceCouple(EntityPlayer player)
	{
		super(player);
		marriedVillagers = buildMarriedVillagerMap();
		maxIndex = marriedVillagers.size() - 1;
	}

	@Override
	public void initGui()
	{
		drawDivorceCoupleSelectGui();
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if (guibutton == shiftIndexDownButton)
		{
			if (currentIndex == 0)
			{
				currentIndex = maxIndex;
			}

			else
			{
				currentIndex--;
			}

			drawDivorceCoupleSelectGui();
		}

		else if (guibutton == shiftIndexUpButton)
		{
			if (currentIndex == maxIndex)
			{
				currentIndex = 0;
			}

			else
			{
				currentIndex++;
			}

			drawDivorceCoupleSelectGui();
		}

		else if (guibutton == divorceButton)
		{
			try
			{
				divorceSelectedVillagers();
				mc.displayGuiScreen(null);
			}

			catch (ArrayIndexOutOfBoundsException e)
			{

			}
		}

		else if (guibutton == backButton)
		{
			Minecraft.getMinecraft().displayGuiScreen(parentGui);
			return;
		}

		else if (guibutton == exitButton)
		{
			mc.displayGuiScreen(null);
		}
	}

	@Override
	protected void keyTyped(char c, int i)
	{
		if (i == Keyboard.KEY_BACK)
		{
			if (backButton.enabled)
			{
				actionPerformed(backButton);
			}

			else
			{
				actionPerformed(exitButton);
			}
		}

		else if (i == Keyboard.KEY_ESCAPE)
		{
			actionPerformed(exitButton);
		}

		else if (i == Keyboard.KEY_LSHIFT || i == Keyboard.KEY_RSHIFT)
		{
			boolean hotkeysDisplayed = false;

			for (Object obj : buttonList)
			{
				GuiButton button = (GuiButton)obj;

				if (button.displayString.contains(new Integer(button.id).toString()))
				{
					hotkeysDisplayed = true;
					break;
				}

				if (button.id == 10) //Back button
				{
					button.displayString = "Bkspc: " + button.displayString;
				}

				else if (button.id == 11) //Exit button
				{
					button.displayString = "Esc: " + button.displayString;
				}

				else
				{
					button.displayString = button.id + ": " + button.displayString;
				}
			}

			if (hotkeysDisplayed)
			{
				for (Object obj : buttonList)
				{
					GuiButton button = (GuiButton)obj;

					if (button.id == 10) //Back button
					{
						button.displayString = "gui.button.back";
					}

					else if (button.id == 11) //Exit button
					{
						button.displayString = "gui.button.exit";
					}

					else
					{
						button.displayString = button.displayString.substring(3);
					}
				}
			}
		}

		else
		{
			try
			{
				int id = Integer.parseInt(Character.toString(c));

				for (Object obj : buttonList)
				{
					GuiButton button = (GuiButton)obj;

					if (button.id == id)
					{
						actionPerformed(button);
					}
				}
			}

			catch (Throwable e)
			{
				return;
			}
		}
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawDefaultBackground();
		drawCenteredString(fontRenderer, Localization.getString("gui.title.priest.divorcecouple"), width / 2, height / 2 - 50, 0xffffff);
		super.drawScreen(sizeX, sizeY, offset);
	}

	/**
	 * Draws the GUI used to select a couple that should divorce.
	 */
	private void drawDivorceCoupleSelectGui()
	{
		buttonList.clear();

		buttonList.add(shiftIndexDownButton = new GuiButton(1, width / 2 - 135, height / 2 + -16, 40, 20, "<--"));
		buttonList.add(shiftIndexUpButton = new GuiButton(2, width / 2 + 105, height / 2 + -16, 40, 20, "-->"));
		buttonList.add(divorceButton = new GuiButton(3, width / 2 - 20, height / 2 + 50 + -16, 65, 20, Localization.getString("gui.button.special.priest.divorce")));

		try
		{
			String husbandName = (String)marriedVillagers.keySet().toArray()[currentIndex];
			String wifeName = (String)marriedVillagers.values().toArray()[currentIndex];
			buttonList.add(selectedCoupleButton = new GuiButton(4, width / 2 - 95, height / 2 + -16, husbandName + " & " + wifeName));
		}

		catch (ArrayIndexOutOfBoundsException e)
		{
			buttonList.add(selectedCoupleButton = new GuiButton(4, width / 2 - 95, height / 2 + -16, Localization.getString("gui.button.special.priest.divorcecouple.nonefound")));
		}

		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, Localization.getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, Localization.getString("gui.button.exit")));
		backButton.enabled = true;
	}

	/**
	 * Builds a map containing all entities that are married and their spouse.
	 * 
	 * @return	Map<String, String> whose key is the name of an entity and whose value is the name of their spouse.
	 */
	private Map<String, String> buildMarriedVillagerMap()
	{
		//Used for storing IDs already gotten that belong to married villagers.
		Map<Integer, Integer> tempIDMap = new HashMap<Integer, Integer>();
		Map<String, String> returnMap = new HashMap<String, String>();

		for (Object obj : player.worldObj.loadedEntityList)
		{
			Entity entity = (Entity)obj;

			if (entity instanceof EntityBase)
			{
				EntityBase entityBase = (EntityBase)entity;

				if (entityBase.isMarried)
				{
					//Be sure the value isn't already contained in the key set.
					if (tempIDMap.containsKey(entityBase.mcaID) == false && tempIDMap.containsValue(entityBase.mcaID) == false)
					{
						int spouseId = entityBase.familyTree.getEntityWithRelation(EnumRelation.Spouse);

						if (spouseId == -1)
						{
							MCA.instance.log("WARNING: Wife or Husband not found for entity identified as married.");
						}

						tempIDMap.put(entityBase.mcaID, spouseId);
					}
				}
			}
		}

		//Loop through each ID in the tempIDMap, get the entities' name, and put them in the returnMap.
		for (Map.Entry<Integer, Integer> entry : tempIDMap.entrySet())
		{
			String husbandName = "";
			String wifeName = "";

			for (Object obj : Minecraft.getMinecraft().theWorld.loadedEntityList)
			{
				if (obj instanceof EntityBase)
				{
					EntityBase entity = (EntityBase)obj;

					if (entity.mcaID == entry.getKey())
					{
						husbandName = entity.name;
					}

					else if (entity.mcaID == entry.getValue())
					{
						wifeName = entity.name;
					}

					//Check if both names have been assigned.
					if (!husbandName.equals("") && !wifeName.equals(""))
					{
						returnMap.put(husbandName, wifeName);
						break;
					}
				}
			}
		}

		return returnMap;
	}

	/**
	 * Makes the selected villagers in the map divorce from each other.
	 */
	private void divorceSelectedVillagers()
	{
		String spouseEntity1Name = (String)marriedVillagers.keySet().toArray()[currentIndex];
		String spouseEntity2Name = (String)marriedVillagers.values().toArray()[currentIndex];

		EntityBase spouseEntity1 = null;
		EntityBase spouseEntity2 = null;

		for (Object obj : Minecraft.getMinecraft().theWorld.loadedEntityList)
		{
			if (obj instanceof EntityBase)
			{
				EntityBase entityBase = (EntityBase)obj;

				if (entityBase.isMarried)
				{
					//Find instances of the husband and wife entity.
					if (entityBase.name.equals(spouseEntity1Name) && entityBase.familyTree.getInstanceOfRelative(EnumRelation.Spouse).name.equals(spouseEntity2Name))
					{
						spouseEntity1 = entityBase;
					}

					else if (entityBase.name.equals(spouseEntity2Name) && entityBase.familyTree.getInstanceOfRelative(EnumRelation.Spouse).name.equals(spouseEntity1Name))
					{
						spouseEntity2 = entityBase;
					}
				}
			}
		}

		if (spouseEntity1 != null)
		{
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(spouseEntity1.entityId, "shouldDivorce", true));
		}

		if (spouseEntity2 != null)
		{
			PacketDispatcher.sendPacketToServer(PacketCreator.createFieldValuePacket(spouseEntity2.entityId, "shouldDivorce", true));
		}
	}
}
