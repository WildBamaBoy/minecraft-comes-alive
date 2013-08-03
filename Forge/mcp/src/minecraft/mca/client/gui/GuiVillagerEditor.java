/*******************************************************************************
 * GuiVillagerEditor.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import java.util.List;

import mca.core.MCA;
import mca.core.util.LanguageHelper;
import mca.core.util.PacketHelper;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumMood;
import mca.enums.EnumTrait;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to edit villager information.
 */
@SideOnly(Side.CLIENT)
public class GuiVillagerEditor extends AbstractGui 
{
	private AbstractEntity villagerBeingEdited;
	
	private GuiTextField nameTextField;
	private GuiTextField dummyTextField;
	private GuiButton randomButton;
	private GuiButton genderButton;
	private GuiButton textureButton;
	private GuiButton shiftTextureIndexUpButton;
	private GuiButton shiftTextureIndexDownButton;
	private GuiButton professionButton;
	private GuiButton shiftProfessionUpButton;
	private GuiButton shiftProfessionDownButton;
	private GuiButton inventoryButton;
	private GuiButton moodButton;
	private GuiButton shiftMoodUpButton;
	private GuiButton shiftMoodDownButton;
	private GuiButton traitButton;
	private GuiButton shiftTraitUpButton;
	private GuiButton shiftTraitDownButton;
	private GuiButton doneButton;
	
	private boolean containsInvalidCharacters;
	private List<EnumMood> moodList = EnumMood.getMoodsAsCyclableList();
	private int moodListIndex = 0;
	
	/**
	 * Constructor
	 * 
	 * @param 	entityBase	The entity being edited.
	 * @param 	player		The player that opened this GUI.
	 */
	public GuiVillagerEditor(AbstractEntity entityBase, EntityPlayer player) 
	{
		super(player);
		villagerBeingEdited = entityBase;
		villagerBeingEdited.isSleeping = false;
		moodListIndex = moodList.indexOf(villagerBeingEdited.mood);
		PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(villagerBeingEdited.entityId, "isSleeping", false));
	}
	
	@Override
	public void updateScreen()
	{
		//Makes the cursor on the text box blink.
		super.updateScreen();

		try
		{
			nameTextField.updateCursorCounter();

			if (nameTextField.getText().isEmpty())
			{
				doneButton.enabled = false;
			}

			else
			{
				doneButton.enabled = true;
			}
		}

		catch (NullPointerException e)
		{

		}
	}

	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);

		drawEditorGui();
		
		nameTextField = new GuiTextField(fontRenderer, width / 2 - 205, height / 2 - 75, 150, 20);
		nameTextField.setMaxStringLength(32);
		nameTextField.setText(villagerBeingEdited.name);
		
		dummyTextField = new GuiTextField(fontRenderer, width / 2 + 90, height / 2 - 100, 100, 200);
		dummyTextField.setMaxStringLength(0);
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);

		PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(villagerBeingEdited.entityId, "name", villagerBeingEdited.name));
		PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(villagerBeingEdited.entityId, "texture", villagerBeingEdited.getTexture()));
		PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(villagerBeingEdited.entityId, "gender", villagerBeingEdited.gender));
		PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(villagerBeingEdited.entityId, "profession", villagerBeingEdited.profession));
		PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(villagerBeingEdited.entityId, "traitId", villagerBeingEdited.traitId));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if (guibutton.enabled == false)
		{
			return;
		}

		else if (guibutton == doneButton)
		{
			close();
		}

		else if (guibutton == randomButton)
		{
			nameTextField.setText(AbstractEntity.getRandomName(villagerBeingEdited.gender));
			villagerBeingEdited.name = nameTextField.getText();
			nameTextField.mouseClicked(5, 5, 5);
			drawEditorGui();
		}
		
		else if (guibutton == genderButton)
		{
			if (villagerBeingEdited.gender.equals("Male"))
			{
				villagerBeingEdited.gender = "Female";
			}
			
			else
			{
				villagerBeingEdited.gender = "Male";
			}
			
			villagerBeingEdited.setTexture();
			drawEditorGui();
		}
		
		else if (guibutton == shiftTextureIndexUpButton)
		{
			List<String> textureList = MCA.getSkinList(villagerBeingEdited);
			int textureIndex = textureList.indexOf(villagerBeingEdited.getTexture());
			int maxIndex = textureList.size() - 1;
			
			if (textureIndex != maxIndex)
			{
				textureIndex++;
			}
			
			else
			{
				textureIndex = 0;
			}
			
			villagerBeingEdited.setTexture(textureList.get(textureIndex));
			drawEditorGui();
		}
		
		else if (guibutton == shiftTextureIndexDownButton)
		{
			List<String> textureList = MCA.instance.getSkinList(villagerBeingEdited);
			int textureIndex = textureList.indexOf(villagerBeingEdited.getTexture());
			int maxIndex = textureList.size() - 1;
			
			if (textureIndex != 0)
			{
				textureIndex--;
			}
			
			else
			{
				textureIndex = maxIndex;
			}
			
			villagerBeingEdited.setTexture(textureList.get(textureIndex));
			drawEditorGui();
		}
		
		else if (guibutton == shiftProfessionUpButton)
		{
			if (villagerBeingEdited.profession != 7)
			{
				villagerBeingEdited.profession++;
			}
			
			else
			{
				villagerBeingEdited.profession = 0;
			}
			
			if (villagerBeingEdited.profession == 4 && villagerBeingEdited.gender.equals("Female"))
			{
				villagerBeingEdited.profession++;
			}
			
			villagerBeingEdited.setTexture();
			drawEditorGui();
		}
		
		else if (guibutton == shiftProfessionDownButton)
		{
			if (villagerBeingEdited.profession != 0)
			{
				villagerBeingEdited.profession--;
			}
			
			else
			{
				villagerBeingEdited.profession = 7;
			}
			
			if (villagerBeingEdited.profession == 4 && villagerBeingEdited.gender.equals("Female"))
			{
				villagerBeingEdited.profession--;
			}
			
			villagerBeingEdited.setTexture();
			drawEditorGui();
		}
		
		else if (guibutton == shiftMoodUpButton)
		{
			if (moodListIndex != 0)
			{
				moodListIndex--;
			}
			
			else
			{
				moodListIndex = moodList.size() - 1;
			}
			
			villagerBeingEdited.mood = moodList.get(moodListIndex);
			
			if (villagerBeingEdited.mood.isAnger())
			{
				villagerBeingEdited.moodPointsAnger = villagerBeingEdited.mood.getMoodLevel();
				villagerBeingEdited.moodPointsFatigue = 0.0F;
				villagerBeingEdited.moodPointsHappy = 0.0F;
				villagerBeingEdited.moodPointsSad = 0.0F;
			}
			
			else if (villagerBeingEdited.mood.isSadness())
			{
				villagerBeingEdited.moodPointsAnger = 0.0F;
				villagerBeingEdited.moodPointsFatigue = 0.0F;
				villagerBeingEdited.moodPointsHappy = 0.0F;
				villagerBeingEdited.moodPointsSad = villagerBeingEdited.mood.getMoodLevel();
			}
			
			else if (villagerBeingEdited.mood.isHappy())
			{
				villagerBeingEdited.moodPointsAnger = 0.0F;
				villagerBeingEdited.moodPointsFatigue = 0.0F;
				villagerBeingEdited.moodPointsHappy = villagerBeingEdited.mood.getMoodLevel();
				villagerBeingEdited.moodPointsSad = 0.0F;
			}
			
			else if (villagerBeingEdited.mood.isFatigue())
			{
				villagerBeingEdited.moodPointsAnger = 0.0F;
				villagerBeingEdited.moodPointsFatigue = villagerBeingEdited.mood.getMoodLevel();
				villagerBeingEdited.moodPointsHappy = 0.0F;
				villagerBeingEdited.moodPointsSad = 0.0F;
			}
			
			else if (villagerBeingEdited.mood.isNeutral())
			{
				villagerBeingEdited.moodPointsAnger = 0.0F;
				villagerBeingEdited.moodPointsFatigue = 0.0F;
				villagerBeingEdited.moodPointsHappy = 0.0F;
				villagerBeingEdited.moodPointsSad = 0.0F;
			}
			
			villagerBeingEdited.setMoodByMoodPoints(true);
			drawEditorGui();
		}
		
		else if (guibutton == shiftMoodDownButton)
		{
			if (moodListIndex != moodList.size() - 1)
			{
				moodListIndex++;
			}
			
			else
			{
				moodListIndex = 0;
			}
			
			villagerBeingEdited.mood = moodList.get(moodListIndex);
			
			if (villagerBeingEdited.mood.isAnger())
			{
				villagerBeingEdited.moodPointsAnger = villagerBeingEdited.mood.getMoodLevel();
				villagerBeingEdited.moodPointsFatigue = 0.0F;
				villagerBeingEdited.moodPointsHappy = 0.0F;
				villagerBeingEdited.moodPointsSad = 0.0F;
			}
			
			else if (villagerBeingEdited.mood.isSadness())
			{
				villagerBeingEdited.moodPointsAnger = 0.0F;
				villagerBeingEdited.moodPointsFatigue = 0.0F;
				villagerBeingEdited.moodPointsHappy = 0.0F;
				villagerBeingEdited.moodPointsSad = villagerBeingEdited.mood.getMoodLevel();
			}
			
			else if (villagerBeingEdited.mood.isHappy())
			{
				villagerBeingEdited.moodPointsAnger = 0.0F;
				villagerBeingEdited.moodPointsFatigue = 0.0F;
				villagerBeingEdited.moodPointsHappy = villagerBeingEdited.mood.getMoodLevel();
				villagerBeingEdited.moodPointsSad = 0.0F;
			}
			
			else if (villagerBeingEdited.mood.isFatigue())
			{
				villagerBeingEdited.moodPointsAnger = 0.0F;
				villagerBeingEdited.moodPointsFatigue = villagerBeingEdited.mood.getMoodLevel();
				villagerBeingEdited.moodPointsHappy = 0.0F;
				villagerBeingEdited.moodPointsSad = 0.0F;
			}
			
			else if (villagerBeingEdited.mood.isNeutral())
			{
				villagerBeingEdited.moodPointsAnger = 0.0F;
				villagerBeingEdited.moodPointsFatigue = 0.0F;
				villagerBeingEdited.moodPointsHappy = 0.0F;
				villagerBeingEdited.moodPointsSad = 0.0F;
			}
			
			villagerBeingEdited.setMoodByMoodPoints(true);
			drawEditorGui();
		}
		
		else if (guibutton == shiftTraitUpButton)
		{
			if (villagerBeingEdited.traitId != EnumTrait.values().length - 1)
			{
				villagerBeingEdited.traitId++;
			}
			
			else
			{
				villagerBeingEdited.traitId = 1;
			}
			
			villagerBeingEdited.trait = EnumTrait.getTraitById(villagerBeingEdited.traitId);
			drawEditorGui();
		}
		
		else if (guibutton == shiftTraitDownButton)
		{
			if (villagerBeingEdited.traitId != 1)
			{
				villagerBeingEdited.traitId--;
			}
			
			else
			{
				villagerBeingEdited.traitId = EnumTrait.values().length - 1;
			}
			
			villagerBeingEdited.trait = EnumTrait.getTraitById(villagerBeingEdited.traitId);
			drawEditorGui();
		}
		
		else if (guibutton == inventoryButton)
		{
			villagerBeingEdited.shouldOpenInventory = true;
			PacketDispatcher.sendPacketToServer(PacketHelper.createFieldValuePacket(villagerBeingEdited.entityId, "shouldOpenInventory", true));
			close();
		}
	}

	@Override
	protected void keyTyped(char c, int i)
	{
		nameTextField.textboxKeyTyped(c, i);
		String text = nameTextField.getText().trim();

		if (text.contains("/") || text.contains("?") || text.contains("<") || text.contains(">") || text.contains("\\") || text.contains(":") || text.contains("*") || text.contains("|") || text.contains("\""))
		{
			containsInvalidCharacters = true;
			doneButton.enabled = false;
		}

		else
		{
			containsInvalidCharacters = false;
		}
		
		if (!containsInvalidCharacters)
		{
			villagerBeingEdited.name = nameTextField.getText();
		}
		
		drawEditorGui();
	}

	@Override
	protected void mouseClicked(int clickX, int clickY, int clicked)
	{
		super.mouseClicked(clickX, clickY, clicked);
		nameTextField.mouseClicked(clickX, clickY, clicked);
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);

		int posX = width / 2 + 140;
		int posY = height / 2 + 95;
		int scale = 80;
		
		if (!villagerBeingEdited.isSleeping)
		{
			posY = height / 2 + 80;
		}
		
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)posX, (float)posY, 50.0F);
        GL11.glScalef((float)(-scale), (float)scale, (float)scale);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        
        float yawOffset = villagerBeingEdited.renderYawOffset;
        float rotationYaw = villagerBeingEdited.rotationYaw;
        float rotationPitch = villagerBeingEdited.rotationPitch;
        
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-((float)Math.atan((double)(0F / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        
        villagerBeingEdited.renderYawOffset = (float)Math.atan((double)(0F / 40.0F)) * 20.0F;
        villagerBeingEdited.rotationYaw = (float)Math.atan((double)(0F / 40.0F)) * 40.0F;
        villagerBeingEdited.rotationPitch = -((float)Math.atan((double)(0F / 40.0F))) * 20.0F;
        villagerBeingEdited.rotationYawHead = villagerBeingEdited.rotationYaw;
        
        GL11.glTranslatef(0.0F, villagerBeingEdited.yOffset, 0.0F);
        
        RenderManager.instance.playerViewY = 180.0F;
        RenderManager.instance.renderEntityWithPosYaw(villagerBeingEdited, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        
        villagerBeingEdited.renderYawOffset = yawOffset;
        villagerBeingEdited.rotationYaw = rotationYaw;
        villagerBeingEdited.rotationPitch = rotationPitch;
        
        GL11.glPopMatrix();
        
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
		
		drawString(fontRenderer, LanguageHelper.getString("gui.editor.name"), width / 2 - 205, height / 2 - 87, 0xa0a0a0);

		if (containsInvalidCharacters)
		{
			drawCenteredString(fontRenderer, LanguageHelper.getString("gui.editor.name.invalid"), width / 2 - 90, (height / 2) - 87, 0xCC0000);
		}

		nameTextField.drawTextBox();
		dummyTextField.drawTextBox();

		GL11.glPushMatrix();
		GL11.glScalef(1.5F, 1.5F, 1.5F);
		drawCenteredString(fontRenderer, LanguageHelper.getString("item.editor"), width / 2 - 75, (height / 2) - 115, 0xffffff);
		GL11.glPopMatrix();
		
		super.drawScreen(sizeX, sizeY, offset);
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	/**
	 * Draws the editor GUI.
	 */
	private void drawEditorGui()
	{
		buttonList.clear();
		buttonList.add(randomButton                = new GuiButton(1,  width / 2 - 50,  height / 2 - 75, 60, 20, LanguageHelper.getString("gui.button.random")));
		buttonList.add(genderButton                = new GuiButton(2,  width / 2 - 190, height / 2 - 40, 175, 20, LanguageHelper.getString("gui.button.setup.gender" + villagerBeingEdited.gender.toLowerCase())));
		buttonList.add(textureButton               = new GuiButton(3,  width / 2 - 190, height / 2 - 20, 175, 20, "Texture: " + villagerBeingEdited.getTexture().replace("textures/skins//", "").replace(".png", "")));
		buttonList.add(shiftTextureIndexUpButton   = new GuiButton(4,  width / 2 - 15,  height / 2 - 20, 20, 20, ">>"));
		buttonList.add(shiftTextureIndexDownButton = new GuiButton(5,  width / 2 - 210, height / 2 - 20, 20, 20, "<<"));
		buttonList.add(professionButton            = new GuiButton(6,  width / 2 - 190, height / 2 - 0, 175, 20, "Title: " + villagerBeingEdited.getLocalizedProfessionString()));
		buttonList.add(shiftProfessionUpButton     = new GuiButton(7,  width / 2 - 15,  height / 2 - 0, 20, 20, ">>"));
		buttonList.add(shiftProfessionDownButton   = new GuiButton(8,  width / 2 - 210, height / 2 - 0, 20, 20, "<<"));
		buttonList.add(moodButton                  = new GuiButton(9, width / 2 - 190, height / 2 + 20, 175, 20, LanguageHelper.getString("gui.button.editor.mood") + villagerBeingEdited.mood.getLocalizedValue() + " (Lvl. " + villagerBeingEdited.mood.getMoodLevel() + ")"));
		buttonList.add(shiftMoodUpButton           = new GuiButton(10, width / 2 - 15,  height / 2 + 20, 20, 20, ">>"));
		buttonList.add(shiftMoodDownButton         = new GuiButton(11, width / 2 - 210, height / 2 + 20, 20, 20, "<<"));
		buttonList.add(traitButton                 = new GuiButton(12, width / 2 - 190, height / 2 + 40, 175, 20, LanguageHelper.getString("gui.button.editor.trait") + villagerBeingEdited.trait.getLocalizedValue()));
		buttonList.add(shiftTraitUpButton          = new GuiButton(13, width / 2 - 15,  height / 2 + 40, 20, 20, ">>"));
		buttonList.add(shiftTraitDownButton        = new GuiButton(14, width / 2 - 210, height / 2 + 40, 20, 20, "<<"));
		buttonList.add(inventoryButton             = new GuiButton(15, width / 2 - 190, height / 2 + 60, 175, 20, LanguageHelper.getString("gui.button.spouse.inventory")));
		buttonList.add(doneButton                  = new GuiButton(16, width / 2 - 50,  height / 2 + 85, 100, 20, LanguageHelper.getString("gui.button.done")));
		
		if (villagerBeingEdited instanceof EntityPlayerChild)
		{
			professionButton.enabled = false;
			shiftProfessionUpButton.enabled = false;
			shiftProfessionDownButton.enabled = false;
		}
	}
}
