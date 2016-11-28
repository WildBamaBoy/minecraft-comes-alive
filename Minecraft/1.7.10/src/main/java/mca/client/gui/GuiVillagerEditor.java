/*******************************************************************************
 * GuiVillagerEditor.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.client.gui;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mca.core.MCA;
import mca.core.util.Utility;
import mca.entity.AbstractEntity;
import mca.entity.EntityPlayerChild;
import mca.enums.EnumMood;
import mca.enums.EnumPacketType;
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

import com.radixshock.radixcore.network.Packet;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to edit villager information.
 */
@SideOnly(Side.CLIENT)
public class GuiVillagerEditor extends AbstractGui 
{
	private AbstractEntity editingVillager;

	private GuiTextField nameTextField;
	private GuiTextField dummyTextField;
	private GuiButton randomButton;
	private GuiButton genderButton;
	private GuiButton shiftTextureIndexUpButton;
	private GuiButton shiftTextureIndexDownButton;
	private GuiButton professionButton;
	private GuiButton shiftProfessionUpButton;
	private GuiButton shiftProfessionDownButton;
	private GuiButton inventoryButton;
	private GuiButton shiftMoodUpButton;
	private GuiButton shiftMoodDownButton;
	private GuiButton shiftTraitUpButton;
	private GuiButton shiftTraitDownButton;

	//Buttons on page 2
	private GuiButton appliesHeightButton;
	private GuiButton heightButton;
	private GuiButton shiftHeightUpButton;
	private GuiButton shiftHeightDownButton;
	private GuiButton appliesGirthButton;
	private GuiButton girthButton;
	private GuiButton shiftGirthUpButton;
	private GuiButton shiftGirthDownButton;

	private GuiButton backButton;
	private GuiButton nextButton;
	private GuiButton doneButton;

	/** Label buttons. */
	private GuiButton textureButton;
	private GuiButton moodButton;
	private GuiButton traitButton;

	/** Variables */
	private boolean containsInvalidCharacters;
	private List<EnumMood> moodList = EnumMood.getMoodsAsCyclableList();
	private int moodListIndex = 0;
	private int currentPage = 1;

	/**
	 * Constructor
	 * 
	 * @param 	abstractEntity	The entity being edited.
	 * @param 	player		The player that opened this GUI.
	 */
	public GuiVillagerEditor(AbstractEntity abstractEntity, EntityPlayer player) 
	{
		super(player);
		editingVillager = abstractEntity;
		editingVillager.isSleeping = false;
		moodListIndex = moodList.indexOf(editingVillager.mood);

		MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetFieldValue, editingVillager.getEntityId(), "isSleeping", editingVillager.isSleeping));
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

		if (!MCA.getInstance().inDebugMode)
		{
			drawEditorGuiPage1();

			nameTextField = new GuiTextField(fontRendererObj, width / 2 - 205, height / 2 - 75, 150, 20);
			nameTextField.setMaxStringLength(32);
			nameTextField.setText(editingVillager.name);
		}

		else
		{
			currentPage = -1;
		}

		dummyTextField = new GuiTextField(fontRendererObj, width / 2 + 90, height / 2 - 100, 100, 200);
		dummyTextField.setMaxStringLength(0);
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
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
			MCA.packetPipeline.sendPacketToServer(
					new Packet(EnumPacketType.SyncEditorSettings, 
							editingVillager.getEntityId(), editingVillager.name, editingVillager.isMale, editingVillager.profession, 
							editingVillager.moodPointsAnger, editingVillager.moodPointsHappy, editingVillager.moodPointsSad, 
							editingVillager.traitId, editingVillager.inventory, editingVillager.texture));
			close();
		}

		else if (guibutton == randomButton)
		{
			nameTextField.setText(Utility.getRandomName(editingVillager.isMale));
			editingVillager.name = nameTextField.getText();
			nameTextField.mouseClicked(5, 5, 5);
			drawEditorGuiPage1();
		}

		else if (guibutton == genderButton)
		{
			editingVillager.isMale = !editingVillager.isMale;
			editingVillager.setTexture();
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTextureIndexUpButton)
		{
			List<String> textureList = MCA.getSkinList(editingVillager);
			sortTextureList(textureList);
			
			int textureIndex = textureList.indexOf(editingVillager.getTexture());
			int maxIndex = textureList.size() - 1;

			if (textureIndex != maxIndex)
			{
				textureIndex++;
			}

			else
			{
				textureIndex = 0;
			}

			editingVillager.setTexture(textureList.get(textureIndex));
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTextureIndexDownButton)
		{
			List<String> textureList = MCA.getSkinList(editingVillager);
			sortTextureList(textureList);
			
			int textureIndex = textureList.indexOf(editingVillager.getTexture());
			int maxIndex = textureList.size() - 1;

			if (textureIndex != 0)
			{
				textureIndex--;
			}

			else
			{
				textureIndex = maxIndex;
			}

			editingVillager.setTexture(textureList.get(textureIndex));
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftProfessionUpButton)
		{
			if (editingVillager.profession != 7)
			{
				editingVillager.profession++;
			}

			else
			{
				editingVillager.profession = 0;
			}

			if (editingVillager.profession == 4 && !editingVillager.isMale)
			{
				editingVillager.profession++;
			}

			editingVillager.setTexture();
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftProfessionDownButton)
		{
			if (editingVillager.profession != 0)
			{
				editingVillager.profession--;
			}

			else
			{
				editingVillager.profession = 7;
			}

			if (editingVillager.profession == 4 && !editingVillager.isMale)
			{
				editingVillager.profession--;
			}

			editingVillager.setTexture();
			drawEditorGuiPage1();
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

			editingVillager.mood = moodList.get(moodListIndex);

			if (editingVillager.mood.isAnger())
			{
				editingVillager.moodPointsAnger = editingVillager.mood.getMoodLevel();
				editingVillager.moodPointsHappy = 0.0F;
				editingVillager.moodPointsSad = 0.0F;
			}

			else if (editingVillager.mood.isSadness())
			{
				editingVillager.moodPointsAnger = 0.0F;
				editingVillager.moodPointsHappy = 0.0F;
				editingVillager.moodPointsSad = editingVillager.mood.getMoodLevel();
			}

			else if (editingVillager.mood.isHappy())
			{
				editingVillager.moodPointsAnger = 0.0F;
				editingVillager.moodPointsHappy = editingVillager.mood.getMoodLevel();
				editingVillager.moodPointsSad = 0.0F;
			}

			else if (editingVillager.mood.isNeutral())
			{
				editingVillager.moodPointsAnger = 0.0F;
				editingVillager.moodPointsHappy = 0.0F;
				editingVillager.moodPointsSad = 0.0F;
			}

			editingVillager.setMoodByMoodPoints(true);
			drawEditorGuiPage1();
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

			editingVillager.mood = moodList.get(moodListIndex);

			if (editingVillager.mood.isAnger())
			{
				editingVillager.moodPointsAnger = editingVillager.mood.getMoodLevel();
				editingVillager.moodPointsHappy = 0.0F;
				editingVillager.moodPointsSad = 0.0F;
			}

			else if (editingVillager.mood.isSadness())
			{
				editingVillager.moodPointsAnger = 0.0F;
				editingVillager.moodPointsHappy = 0.0F;
				editingVillager.moodPointsSad = editingVillager.mood.getMoodLevel();
			}

			else if (editingVillager.mood.isHappy())
			{
				editingVillager.moodPointsAnger = 0.0F;
				editingVillager.moodPointsHappy = editingVillager.mood.getMoodLevel();
				editingVillager.moodPointsSad = 0.0F;
			}

			else if (editingVillager.mood.isNeutral())
			{
				editingVillager.moodPointsAnger = 0.0F;
				editingVillager.moodPointsHappy = 0.0F;
				editingVillager.moodPointsSad = 0.0F;
			}

			editingVillager.setMoodByMoodPoints(true);
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTraitUpButton)
		{
			if (editingVillager.traitId != EnumTrait.values().length - 1)
			{
				editingVillager.traitId++;
			}

			else
			{
				editingVillager.traitId = 1;
			}

			editingVillager.trait = EnumTrait.getTraitById(editingVillager.traitId);
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTraitDownButton)
		{
			if (editingVillager.traitId != 1)
			{
				editingVillager.traitId--;
			}

			else
			{
				editingVillager.traitId = EnumTrait.values().length - 1;
			}

			editingVillager.trait = EnumTrait.getTraitById(editingVillager.traitId);
			drawEditorGuiPage1();
		}

		else if (guibutton == inventoryButton)
		{
			editingVillager.doOpenInventory = true;
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetFieldValue, editingVillager.getEntityId(), "doOpenInventory", editingVillager.doOpenInventory));
			close();
		}

		else if (guibutton == nextButton)
		{
			switch (currentPage)
			{
			case 1: drawEditorGuiPage2(); break;
			case 2: drawEditorGuiPage2(); break;
			}
		}

		else if (guibutton == backButton)
		{
			switch (currentPage)
			{
			case 1: drawEditorGuiPage1(); break;
			case 2: drawEditorGuiPage1(); break;
			}
		}

		else if (guibutton == shiftHeightUpButton)
		{
			editingVillager.heightFactor += 0.01F;
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetFieldValue, editingVillager.getEntityId(), "heightFactor", editingVillager.heightFactor));
			drawEditorGuiPage2();
		}

		else if (guibutton == shiftHeightDownButton)
		{
			editingVillager.heightFactor -= 0.01F;
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetFieldValue, editingVillager.getEntityId(), "heightFactor", editingVillager.heightFactor));
			drawEditorGuiPage2();
		}

		else if (guibutton == shiftGirthUpButton)
		{
			editingVillager.girthFactor += 0.01F;
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetFieldValue, editingVillager.getEntityId(), "girthFactor", editingVillager.girthFactor));
			drawEditorGuiPage2();
		}

		else if (guibutton == shiftGirthDownButton)
		{
			editingVillager.girthFactor -= 0.01F;
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetFieldValue, editingVillager.getEntityId(), "girthFactor", editingVillager.girthFactor));
			drawEditorGuiPage2();
		}
		
		else if (guibutton == appliesHeightButton)
		{
			editingVillager.doApplyHeight = !editingVillager.doApplyHeight;
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetFieldValue, editingVillager.getEntityId(), "doApplyHeight", editingVillager.doApplyHeight));
			drawEditorGuiPage2();
		}
		
		else if (guibutton == appliesGirthButton)
		{
			editingVillager.doApplyGirth = !editingVillager.doApplyGirth;
			MCA.packetPipeline.sendPacketToServer(new Packet(EnumPacketType.SetFieldValue, editingVillager.getEntityId(), "doApplyGirth", editingVillager.doApplyGirth));
			drawEditorGuiPage2();
		}
	}

	@Override
	protected void keyTyped(char c, int i)
	{
		if (currentPage == 1)
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
				editingVillager.name = nameTextField.getText();
			}

			drawEditorGuiPage1();
		}
		
		else
		{
			super.keyTyped(c, i);
		}
	}

	@Override
	protected void mouseClicked(int clickX, int clickY, int clicked)
	{
		super.mouseClicked(clickX, clickY, clicked);

		if (currentPage == 1)
		{
			nameTextField.mouseClicked(clickX, clickY, clicked);
		}
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);

		if (currentPage != -1)
		{
			int posX = width / 2 + 140;
			int posY = height / 2 + 95;
			int scale = 80;

			if (!editingVillager.isSleeping)
			{
				posY = height / 2 + 80;
			}

			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glPushMatrix();
			GL11.glTranslatef(posX, posY, 50.0F);
			GL11.glScalef((-scale), scale, scale);
			GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

			float yawOffset = editingVillager.renderYawOffset;
			float rotationYaw = editingVillager.rotationYaw;
			float rotationPitch = editingVillager.rotationPitch;

			GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
			RenderHelper.enableStandardItemLighting();
			GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-((float)Math.atan(0F / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);

			editingVillager.renderYawOffset = (float)Math.atan(0F / 40.0F) * 20.0F;
			editingVillager.rotationYaw = (float)Math.atan(0F / 40.0F) * 40.0F;
			editingVillager.rotationPitch = -((float)Math.atan(0F / 40.0F)) * 20.0F;
			editingVillager.rotationYawHead = editingVillager.rotationYaw;

			GL11.glTranslatef(0.0F, editingVillager.yOffset, 0.0F);

			RenderManager.instance.playerViewY = 180.0F;
			RenderManager.instance.renderEntityWithPosYaw(editingVillager, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);

			editingVillager.renderYawOffset = yawOffset;
			editingVillager.rotationYaw = rotationYaw;
			editingVillager.rotationPitch = rotationPitch;

			GL11.glPopMatrix();

			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

			dummyTextField.drawTextBox();
		}

		if (currentPage == 1)
		{
			drawString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.editor.name"), width / 2 - 205, height / 2 - 87, 0xa0a0a0);
			nameTextField.drawTextBox();

			if (containsInvalidCharacters)
			{
				drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("gui.editor.name.invalid"), width / 2 - 90, (height / 2) - 87, 0xCC0000);
			}
		}

		GL11.glPushMatrix();
		GL11.glScalef(1.5F, 1.5F, 1.5F);
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageLoader().getString("item.editor"), width / 2 - 75, (height / 2) - 115, 0xffffff);
		GL11.glPopMatrix();

		if (currentPage == -1)
		{
			drawEditorDebugText();
		}

		super.drawScreen(sizeX, sizeY, offset);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	private void drawEditorDebugText()
	{
		int fieldIndex = 0;
		for (int column = 1; column < 4; column++)
		{
			int fieldsAdded = 0;
			final Field[] fieldArray = AbstractEntity.class.getDeclaredFields();

			if (fieldIndex < fieldArray.length - 1)
			{
				while (fieldsAdded != 13)
				{
					try
					{
						String fieldName = fieldArray[fieldIndex].getName();
						String fieldValue = fieldArray[fieldIndex].get(editingVillager).toString();
						
						if (fieldName.length() > 14)
						{
							fieldName = fieldName.substring(0, 13) + "...";
						}

						if (fieldValue.length() > 10)
						{
							fieldValue = fieldValue.substring(0, 9) + "...";
						}
						
						drawString(fontRendererObj, fieldName + " = " + fieldValue, width / 2 - 340 + (140 * column), height / 2 - 90 + 15 * fieldsAdded, 0xffffff);
						fieldIndex++;
						fieldsAdded++;
					}

					catch (Exception e)
					{
						continue;
					}
				}

				continue;
			}

			else
			{
				break;
			}
		}
	}

	/**
	 * Draws the editor GUI.
	 */
	private void drawEditorGuiPage1()
	{
		currentPage = 1;

		buttonList.clear();
		buttonList.add(randomButton                = new GuiButton(1,  width / 2 - 50,  height / 2 - 75, 60, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.random")));
		buttonList.add(genderButton                = new GuiButton(2,  width / 2 - 190, height / 2 - 40, 175, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.setup.gender" + editingVillager.getGenderAsString())));
		buttonList.add(textureButton               = new GuiButton(3,  width / 2 - 190, height / 2 - 20, 175, 20, "Texture: " + editingVillager.getTexture().replace("textures/skins/", "").replace(".png", "")));
		buttonList.add(shiftTextureIndexUpButton   = new GuiButton(4,  width / 2 - 15,  height / 2 - 20, 20, 20, ">>"));
		buttonList.add(shiftTextureIndexDownButton = new GuiButton(5,  width / 2 - 210, height / 2 - 20, 20, 20, "<<"));
		buttonList.add(professionButton            = new GuiButton(6,  width / 2 - 190, height / 2 - 0, 175, 20, "Title: " + editingVillager.getLocalizedProfessionString()));
		buttonList.add(shiftProfessionUpButton     = new GuiButton(7,  width / 2 - 15,  height / 2 - 0, 20, 20, ">>"));
		buttonList.add(shiftProfessionDownButton   = new GuiButton(8,  width / 2 - 210, height / 2 - 0, 20, 20, "<<"));
		buttonList.add(moodButton                  = new GuiButton(9, width / 2 - 190, height / 2 + 20, 175, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.editor.mood") + editingVillager.mood.getLocalizedValue() + " (Lvl. " + editingVillager.mood.getMoodLevel() + ")"));
		buttonList.add(shiftMoodUpButton           = new GuiButton(10, width / 2 - 15,  height / 2 + 20, 20, 20, ">>"));
		buttonList.add(shiftMoodDownButton         = new GuiButton(11, width / 2 - 210, height / 2 + 20, 20, 20, "<<"));
		buttonList.add(traitButton                 = new GuiButton(12, width / 2 - 190, height / 2 + 40, 175, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.editor.trait") + editingVillager.trait.getLocalizedValue()));
		buttonList.add(shiftTraitUpButton          = new GuiButton(13, width / 2 - 15,  height / 2 + 40, 20, 20, ">>"));
		buttonList.add(shiftTraitDownButton        = new GuiButton(14, width / 2 - 210, height / 2 + 40, 20, 20, "<<"));
		buttonList.add(inventoryButton             = new GuiButton(15, width / 2 - 190, height / 2 + 60, 175, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.spouse.inventory")));
		buttonList.add(doneButton                  = new GuiButton(16, width / 2 - 50,  height / 2 + 85, 75, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.done")));
		buttonList.add(nextButton                  = new GuiButton(17, width / 2 + 25,  height / 2 + 85, 50, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.next")));
		buttonList.add(backButton                  = new GuiButton(18, width / 2 - 101,  height / 2 + 85, 50, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));

		if (editingVillager instanceof EntityPlayerChild)
		{
			professionButton.enabled = false;
			shiftProfessionUpButton.enabled = false;
			shiftProfessionDownButton.enabled = false;
		}

		backButton.enabled = false;
	}

	/**
	 * Draws the editor GUI.
	 */
	private void drawEditorGuiPage2()
	{
		final int displayHeight = Math.round(editingVillager.heightFactor * 100);
		final int displayGirth = Math.round(editingVillager.girthFactor * 100);

		currentPage = 2;
		buttonList.clear();
		buttonList.add(appliesHeightButton   = new GuiButton(1,  width / 2 - 190, height / 2 - 60, 175, 20, "Applies Height: " + editingVillager.doApplyHeight));
		buttonList.add(heightButton          = new GuiButton(2,  width / 2 - 190, height / 2 - 40, 175, 20, "Height Factor: " + displayHeight));
		buttonList.add(shiftHeightUpButton   = new GuiButton(3,  width / 2 - 15,  height / 2 - 40, 20, 20, ">>"));
		buttonList.add(shiftHeightDownButton = new GuiButton(4,  width / 2 - 210, height / 2 - 40, 20, 20, "<<"));
		buttonList.add(appliesGirthButton   = new GuiButton(5,  width / 2 - 190, height / 2 - 20, 175, 20, "Applies Girth: " + editingVillager.doApplyGirth));
		buttonList.add(girthButton          = new GuiButton(6,  width / 2 - 190, height / 2 - 0, 175, 20, "Girth Factor: " + displayGirth));
		buttonList.add(shiftGirthUpButton   = new GuiButton(7,  width / 2 - 15,  height / 2 - 0, 20, 20, ">>"));
		buttonList.add(shiftGirthDownButton = new GuiButton(8,  width / 2 - 210, height / 2 - 0, 20, 20, "<<"));
		buttonList.add(doneButton            = new GuiButton(16, width / 2 - 50,  height / 2 + 85, 75, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.done")));
		buttonList.add(nextButton            = new GuiButton(17, width / 2 + 25,  height / 2 + 85, 50, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.next")));
		buttonList.add(backButton            = new GuiButton(18, width / 2 - 101,  height / 2 + 85, 50, 20, MCA.getInstance().getLanguageLoader().getString("gui.button.back")));

		if (!editingVillager.doApplyHeight)
		{
			heightButton.enabled = false;
			shiftHeightUpButton.enabled = false;
			shiftHeightDownButton.enabled = false;
		}
		
		if (!editingVillager.doApplyGirth)
		{
			girthButton.enabled = false;
			shiftGirthUpButton.enabled = false;
			shiftGirthDownButton.enabled = false;
		}

		nextButton.enabled = false;
	}
	
	private void sortTextureList(List<String>listToSort)
	{
		Collections.sort(listToSort, 
				new Comparator<String>()
				{
					@Override
					public int compare(String o1, String o2) 
					{
						final int skinNumber1 = Integer.parseInt(o1.replaceAll("[^0-9]+", " ").trim());
						final int skinNumber2 = Integer.parseInt(o2.replaceAll("[^0-9]+", " ").trim());
						
						if (skinNumber1 == skinNumber2)
						{
							return 0;
						}
						
						else if (skinNumber1 < skinNumber2)
						{
							return -1;
						}
						
						return 1;
					}
				});
	}
}
