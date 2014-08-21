/*******************************************************************************
 * GuiInventory.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import mca.core.MCA;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.entity.EntityVillagerAdult;
import mca.inventory.ContainerInventory;
import mca.network.packets.PacketSetInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the inventory GUI shown when changing an entity's inventory.
 */
@SideOnly(Side.CLIENT)
public class GuiInventory extends InventoryEffectRenderer
{	
	private static final ResourceLocation resourceLocation = new ResourceLocation("mca:textures/gui/container/inventory.png");

	private AbstractEntity owner;
	private GuiButton backButton;
	private GuiButton exitButton;

	/** The number of rows in the inventory. */
	private int inventoryRows;

	/** Has the inventory been opened from the villager editor? */
	private boolean fromEditor = false;

	/**
	 * Constructor
	 * 
	 * @param 	entity			The entity who owns the inventory being accessed.
	 * @param 	playerInventory	The inventory of the player opening this GUI.
	 * @param 	entityInventory	The inventory of the entity that the player is interacting with.
	 * @param	fromEditor		Is this GUI being opened from the villager editor?
	 */
	public GuiInventory(AbstractEntity entity, IInventory playerInventory, IInventory entityInventory, boolean fromEditor)
	{
		super(new ContainerInventory(playerInventory, entityInventory, entity));

		owner = entity;
		allowUserInput = false;
		this.fromEditor = fromEditor;

		char c = '\336';
		int i = c - 108;		
		inventoryRows = entityInventory.getSizeInventory() / 9;
		xSize = xSize + 24;
		ySize = i + inventoryRows * 18;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.clear();
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.exit")));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if (guibutton == backButton)
		{
			if (!fromEditor)
			{
				if (owner instanceof EntityPlayerChild)
				{
					Minecraft.getMinecraft().displayGuiScreen(new GuiInteractionPlayerChild((EntityPlayerChild)owner, owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer)));
				}

				else if (owner instanceof EntityVillagerAdult)
				{
					if (owner.spousePlayerName.equals(Minecraft.getMinecraft().thePlayer.getCommandSenderName()))
					{
						Minecraft.getMinecraft().displayGuiScreen(new GuiInteractionSpouse(owner, owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer)));
					}
					
					else
					{
						Minecraft.getMinecraft().displayGuiScreen(new GuiInteractionVillagerAdult(owner, owner.worldObj.getPlayerEntityByName(owner.lastInteractingPlayer)));
					}
				}
			}

			else
			{
				FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiVillagerEditor(owner, Minecraft.getMinecraft().thePlayer));
			}
		}

		else if (guibutton == exitButton)
		{
			if (!fromEditor)
			{
				Minecraft.getMinecraft().displayGuiScreen(null);
			}

			else
			{
				FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiVillagerEditor(owner, Minecraft.getMinecraft().thePlayer));
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRendererObj.drawString(MCA.getInstance().getLanguageLoader().getString("gui.title.inventory", null, owner, false), 32, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float offset, int mouseX, int mouseY)
	{
		this.mc.getTextureManager().bindTexture(resourceLocation);

		int addX = Minecraft.getMinecraft().thePlayer.getActivePotionEffects().size() > 0 ? 120 : 0;

		//Draw the two inventories.
		int x = (width - xSize + addX) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize + 26, inventoryRows * 18 + 21);			//Top inventory
		drawTexturedModalRect(x, y + inventoryRows * 18 + 17, 0, 126, xSize + 26, 96);	//Bottom inventory
	}

	@Override
	public void onGuiClosed() 
	{
		super.onGuiClosed();
		owner.inventory.closeInventory();
		MCA.packetHandler.sendPacketToServer(new PacketSetInventory(owner.getEntityId(), owner.inventory));
	}
}
