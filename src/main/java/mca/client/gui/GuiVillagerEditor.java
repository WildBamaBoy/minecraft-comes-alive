package mca.client.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import mca.ai.AISleep;
import mca.core.MCA;
import mca.entity.EntityHuman;
import mca.enums.EnumPersonality;
import mca.enums.EnumProfession;
import mca.enums.EnumSleepingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Font.Color;
import radixcore.data.DataWatcherEx;
import radixcore.util.NumberCycleList;

/**
 * Defines the GUI used to edit villager information.
 */
@SideOnly(Side.CLIENT)
public class GuiVillagerEditor extends GuiScreen
{
	private final EntityHuman villager;
	private final EntityPlayer player;

	private GuiButton familyTreeButton;

	private GuiTextField nameTextField;
	private GuiTextField dummyTextField;
	private GuiTextField playerSkinTextField;
	
	private GuiButton randomButton;
	private GuiButton genderButton;
	private GuiButton shiftTextureIndexUpButton;
	private GuiButton shiftTextureIndexDownButton;
	private GuiButton professionButton;
	private GuiButton shiftProfessionUpButton;
	private GuiButton shiftProfessionDownButton;
	private GuiButton shiftTraitUpButton;
	private GuiButton shiftTraitDownButton;
	private GuiButton applyPlayerSkinButton;

	//Buttons on page 2
	private GuiButton heightButton;
	private GuiButton shiftHeightUpButton;
	private GuiButton shiftHeightDownButton;
	private GuiButton girthButton;
	private GuiButton shiftGirthUpButton;
	private GuiButton shiftGirthDownButton;
	private GuiButton isInfectedButton;
	private GuiButton clearInteractionFlagButton;
	
	private GuiButton backButton;
	private GuiButton nextButton;
	private GuiButton doneButton;

	private GuiButton textureButton;
	private GuiButton personalityButton;

	private int moodListIndex = 0;
	private int currentPage = 1;

	private NumberCycleList textures;
	private NumberCycleList jobs;
	private NumberCycleList personalities;
	
	public GuiVillagerEditor(EntityHuman EntityHuman, EntityPlayer player)
	{
		super();

		this.player = player;
		villager = EntityHuman;
		
		DataWatcherEx.allowClientSideModification = true;
		villager.getAI(AISleep.class).setSleepingState(EnumSleepingState.INTERRUPTED);
		
		jobs = NumberCycleList.fromList(EnumProfession.getListOfIds());
		personalities = NumberCycleList.fromList(EnumPersonality.getListOfIds());
		textures = villager.getProfessionGroup().getListOfSkinIDs(villager.getIsMale());
	}

	@Override
	public void updateScreen()
	{
		//Makes the cursor on the text box blink.
		super.updateScreen();

		try
		{
			nameTextField.updateCursorCounter();
			playerSkinTextField.updateCursorCounter();
			
			if (nameTextField.getText().isEmpty())
			{
				doneButton.enabled = false;
			}

			else
			{
				doneButton.enabled = true;
			}
		}

		catch (final NullPointerException e)
		{

		}
	}

	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);

		drawEditorGuiPage1();

		nameTextField = new GuiTextField(1, fontRendererObj, width / 2 - 205, height / 2 - 95, 150, 20);
		nameTextField.setMaxStringLength(32);
		nameTextField.setText(villager.getName());

		dummyTextField = new GuiTextField(2, fontRendererObj, width / 2 + 90, height / 2 - 100, 100, 200);
		dummyTextField.setMaxStringLength(0);
		
		playerSkinTextField = new GuiTextField(3, fontRendererObj, width / 2 - 205, height / 2 + 40, 150, 20);
		playerSkinTextField.setMaxStringLength(32);
		playerSkinTextField.setText(villager.usesPlayerSkin() ? villager.getPlayerSkinUsername() : "N/A");
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		
		villager.isInteractionGuiOpen = false;

		DataWatcherEx.allowClientSideModification = true;
		villager.setIsInteracting(false);
		DataWatcherEx.allowClientSideModification = false;
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
			Minecraft.getMinecraft().displayGuiScreen(null);
		}

		else if (guibutton == randomButton)
		{
			nameTextField.setText(villager.getIsMale() ? MCA.getLanguageManager().getString("name.male") : MCA.getLanguageManager().getString("name.female"));
			villager.setName(nameTextField.getText());
			nameTextField.mouseClicked(5, 5, 5);
			drawEditorGuiPage1();
		}

		else if (guibutton == genderButton)
		{
			villager.setIsMale(!villager.getIsMale());
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTextureIndexUpButton)
		{
			textures.next();
			
			String skin = villager.getHeadTexture();
			villager.setHeadTexture(skin.replaceAll("\\d+", String.valueOf(textures.get())));
			villager.setClothesTexture(villager.getHeadTexture());
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTextureIndexDownButton)
		{
			textures.previous();
			
			String skin = villager.getHeadTexture();
			villager.setHeadTexture(skin.replaceAll("\\d+", String.valueOf(textures.get())));
			villager.setClothesTexture(villager.getHeadTexture());
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftProfessionUpButton)
		{
			villager.setProfessionId(jobs.next());
			textures = villager.getProfessionGroup().getListOfSkinIDs(villager.getIsMale());
			villager.setHeadTexture(villager.getRandomSkin());
			villager.setClothesTexture(villager.getHeadTexture());
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftProfessionDownButton)
		{
			villager.setProfessionId(jobs.previous());
			textures = villager.getProfessionGroup().getListOfSkinIDs(villager.getIsMale());
			villager.setHeadTexture(villager.getRandomSkin());
			villager.setClothesTexture(villager.getHeadTexture());
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTraitUpButton)
		{
			villager.setPersonality(personalities.next());
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTraitDownButton)
		{
			villager.setPersonality(personalities.previous());
			drawEditorGuiPage1();
		}

		else if (guibutton == nextButton)
		{
			switch (currentPage)
			{
			case 1:
				drawEditorGuiPage2();
				break;
			case 2:
				drawEditorGuiPage2();
				break;
			}
		}

		else if (guibutton == backButton)
		{
			switch (currentPage)
			{
			case 1:
				drawEditorGuiPage1();
				break;
			case 2:
				drawEditorGuiPage1();
				break;
			}
		}

		else if (guibutton == shiftHeightUpButton)
		{
			villager.setHeight(villager.getHeight() + 0.01F);
			drawEditorGuiPage2();
		}

		else if (guibutton == shiftHeightDownButton)
		{
			villager.setHeight(villager.getHeight() - 0.01F);
			drawEditorGuiPage2();
		}

		else if (guibutton == shiftGirthUpButton)
		{
			villager.setGirth(villager.getGirth() + 0.01F);
			drawEditorGuiPage2();
		}

		else if (guibutton == shiftGirthDownButton)
		{
			villager.setGirth(villager.getGirth() - 0.01F);
			drawEditorGuiPage2();
		}
		
		else if (guibutton == applyPlayerSkinButton)
		{
			if (playerSkinTextField.getText().equals(""))
			{
				villager.setPlayerSkin("");
			}
			
			else
			{
				villager.setPlayerSkin(playerSkinTextField.getText().trim());
			}
		}
		
		else if (guibutton == isInfectedButton)
		{
			villager.setIsInfected(!villager.getIsInfected());
			drawEditorGuiPage2();
		}
		
		else if (guibutton == clearInteractionFlagButton)
		{
			villager.setIsInteracting(false);
			player.addChatComponentMessage(new TextComponentString(Color.GREEN + "[MCA] Interaction flag cleared."));
		}
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException
	{
		if (i == Keyboard.KEY_ESCAPE)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
		}

		else if (currentPage == 1)
		{
			nameTextField.textboxKeyTyped(c, i);
			final String text = nameTextField.getText().trim();
			villager.setName(text);
			
			playerSkinTextField.textboxKeyTyped(c, i);
			drawEditorGuiPage1();
		}

		else
		{
			super.keyTyped(c, i);
		}
	}

	@Override
	protected void mouseClicked(int clickX, int clickY, int clicked) throws IOException
	{
		super.mouseClicked(clickX, clickY, clicked);

		if (currentPage == 1)
		{
			nameTextField.mouseClicked(clickX, clickY, clicked);
			playerSkinTextField.mouseClicked(clickX, clickY, clicked);
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	private void drawEditorGuiPage1()
	{
		currentPage = 1;

		buttonList.clear();
		buttonList.add(randomButton = new GuiButton(1, width / 2 - 50, height / 2 - 95, 60, 20, MCA.getInstance().getLanguageManager().getString("gui.button.random")));
		buttonList.add(genderButton = new GuiButton(2, width / 2 - 190, height / 2 - 60, 175, 20, MCA.getInstance().getLanguageManager().getString("gui.button.setup.gender." + villager.getIsMale())));
		buttonList.add(textureButton = new GuiButton(3, width / 2 - 190, height / 2 - 40, 175, 20, "Texture: " + villager.getHeadTexture().replace("mca:textures/skins/", "").replace(".png", "")));
		buttonList.add(shiftTextureIndexUpButton = new GuiButton(4, width / 2 - 15, height / 2 - 40, 20, 20, ">>"));
		buttonList.add(shiftTextureIndexDownButton = new GuiButton(5, width / 2 - 210, height / 2 - 40, 20, 20, "<<"));
		buttonList.add(professionButton = new GuiButton(6, width / 2 - 190, height / 2 - 20, 175, 20, "Job: " + villager.getProfessionEnum().getUserFriendlyForm()));
		buttonList.add(shiftProfessionUpButton = new GuiButton(7, width / 2 - 15, height / 2 - 20, 20, 20, ">>"));
		buttonList.add(shiftProfessionDownButton = new GuiButton(8, width / 2 - 210, height / 2 - 20, 20, 20, "<<"));
		buttonList.add(personalityButton = new GuiButton(12, width / 2 - 190, height / 2 + 0, 175, 20, MCA.getInstance().getLanguageManager().getString("gui.info.personality", villager.getPersonality().getFriendlyName())));
		buttonList.add(shiftTraitUpButton = new GuiButton(13, width / 2 - 15, height / 2 + 0, 20, 20, ">>"));
		buttonList.add(shiftTraitDownButton = new GuiButton(14, width / 2 - 210, height / 2 + 0, 20, 20, "<<"));
//		buttonList.add(applyPlayerSkinButton = new GuiButton(15, width / 2 - 50, height / 2 + 40, 40, 20, "Apply"));
		
		buttonList.add(doneButton = new GuiButton(17, width / 2 - 50, height / 2 + 85, 75, 20, MCA.getInstance().getLanguageManager().getString("gui.button.done")));
		buttonList.add(nextButton = new GuiButton(18, width / 2 + 25, height / 2 + 85, 50, 20, MCA.getInstance().getLanguageManager().getString("gui.button.next")));
		buttonList.add(backButton = new GuiButton(19, width / 2 - 101, height / 2 + 85, 50, 20, MCA.getInstance().getLanguageManager().getString("gui.button.back")));

		backButton.enabled = false;
	}

	/**
	 * Draws the editor GUI.
	 */
	private void drawEditorGuiPage2()
	{
		final int displayHeight = Math.round(villager.getHeight() * 100);
		final int displayGirth = Math.round(villager.getGirth() * 100);

		currentPage = 2;
		buttonList.clear();
		buttonList.add(heightButton = new GuiButton(1, width / 2 - 190, height / 2 - 40, 175, 20, "Height Factor: " + displayHeight));
		buttonList.add(shiftHeightUpButton = new GuiButton(2, width / 2 - 15, height / 2 - 40, 20, 20, ">>"));
		buttonList.add(shiftHeightDownButton = new GuiButton(3, width / 2 - 210, height / 2 - 40, 20, 20, "<<"));
		buttonList.add(girthButton = new GuiButton(4, width / 2 - 190, height / 2 - 20, 175, 20, "Girth Factor: " + displayGirth));
		buttonList.add(shiftGirthUpButton = new GuiButton(5, width / 2 - 15, height / 2 - 20, 20, 20, ">>"));
		buttonList.add(shiftGirthDownButton = new GuiButton(6, width / 2 - 210, height / 2 - 20, 20, 20, "<<"));
		buttonList.add(isInfectedButton = new GuiButton(7, width / 2 - 190, height / 2 - 0, 175, 20, "Is Infected: " + villager.getIsInfected()));
		buttonList.add(clearInteractionFlagButton = new GuiButton(8, width / 2 - 190, height / 2 + 20, 175, 20, "Clear Interaction Flag"));
		
		buttonList.add(doneButton = new GuiButton(16, width / 2 - 50, height / 2 + 85, 75, 20, MCA.getInstance().getLanguageManager().getString("gui.button.done")));
		buttonList.add(nextButton = new GuiButton(17, width / 2 + 25, height / 2 + 85, 50, 20, MCA.getInstance().getLanguageManager().getString("gui.button.next")));
		buttonList.add(backButton = new GuiButton(18, width / 2 - 101, height / 2 + 85, 50, 20, MCA.getInstance().getLanguageManager().getString("gui.button.back")));

		nextButton.enabled = false;
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawGradientRect(0, 0, width, height, -1072689136, -804253680);

		if (currentPage != -1)
		{
			final int posX = width / 2 + 140;
			int posY = height / 2 + 95;
			final int scale = 80;

			if (!villager.getAI(AISleep.class).getIsSleeping())
			{
				posY = height / 2 + 80;
			}

			net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen(posX, posY, 75, 0, 0, villager);
			dummyTextField.drawTextBox();
		}

		if (currentPage == 1)
		{
			drawString(fontRendererObj, "Name:", width / 2 - 205, height / 2 - 110, 0xffffff);
			drawString(fontRendererObj, MCA.getInstance().getLanguageManager().getString("gui.title.editor"), width / 2 - 205, height / 2 - 87, 0xa0a0a0);
			//TODO
//			drawString(fontRendererObj, "Use Player's Skin:", width / 2 - 205, height / 2 + 25, 0xffffff);
			
			nameTextField.drawTextBox();
//			playerSkinTextField.drawTextBox();
		}

		GL11.glPushMatrix();
		GL11.glScalef(1.5F, 1.5F, 1.5F);
		drawCenteredString(fontRendererObj, MCA.getInstance().getLanguageManager().getString("gui.title.editor"), width / 2 - 75, height / 2 - 125, 0xffffff);
		GL11.glPopMatrix();

		super.drawScreen(sizeX, sizeY, offset);
	}

	private void sortTextureList(List<String> listToSort)
	{
		Collections.sort(listToSort, new Comparator<String>()
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
