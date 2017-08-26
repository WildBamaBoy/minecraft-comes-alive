package mca.client.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import mca.actions.ActionSleep;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumEditAction;
import mca.enums.EnumSleepingState;
import mca.packets.PacketEditVillager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Defines the GUI used to edit villager information.
 */
@SideOnly(Side.CLIENT)
public class GuiVillagerEditor extends GuiScreen
{
	private final EntityVillagerMCA villager;
	private final EntityPlayer player;

	private GuiButton familyTreeButton;

	private GuiTextField nameTextField;
	private GuiTextField dummyTextField;
	//private GuiTextField playerSkinTextField;
	
	private GuiButton randomButton;
	private GuiButton genderButton;
	private GuiButton shiftTextureIndexUpButton;
	private GuiButton shiftTextureIndexDownButton;
	private GuiButton professionButton;
	private GuiButton shiftProfessionUpButton;
	private GuiButton shiftProfessionDownButton;
	private GuiButton shiftTraitUpButton;
	private GuiButton shiftTraitDownButton;

	//Buttons on page 2
	private GuiButton heightButton;
	private GuiButton shiftHeightUpButton;
	private GuiButton shiftHeightDownButton;
	private GuiButton girthButton;
	private GuiButton shiftGirthUpButton;
	private GuiButton shiftGirthDownButton;
	private GuiButton isInfectedButton;
	
	private GuiButton backButton;
	private GuiButton nextButton;
	private GuiButton doneButton;

	private GuiButton textureButton;
	private GuiButton personalityButton;

	private int moodListIndex = 0;
	private int currentPage = 1;
	
	public GuiVillagerEditor(EntityVillagerMCA EntityHuman, EntityPlayer player)
	{
		super();

		this.player = player;
		villager = EntityHuman;
		
		villager.getBehavior(ActionSleep.class).setSleepingState(EnumSleepingState.INTERRUPTED);
	}

	@Override
	public void updateScreen()
	{
		//Makes the cursor on the text box blink.
		super.updateScreen();

		try
		{
			nameTextField.updateCursorCounter();
			//playerSkinTextField.updateCursorCounter();
			
			if (nameTextField.getText().isEmpty())
			{
				doneButton.enabled = false;
			}

			else
			{
				doneButton.enabled = true;
			}
			
			// Update buttons to watch for server-side changes.
			if (currentPage == 1)
			{
				if (!nameTextField.getText().equals(villager.attributes.getName()))
				{
					nameTextField.setText(villager.attributes.getName());
				}
				
				genderButton.displayString = MCA.getLocalizer().getString(("gui.button.setup.gender." + villager.attributes.getIsMale()));
				textureButton.displayString = "Texture: " + villager.attributes.getHeadTexture().replace("mca:textures/skins/", "").replace("sleeping/", "").replace(".png", "");
				professionButton.displayString = "Job: " + villager.attributes.getProfessionEnum().getUserFriendlyForm(villager);
				personalityButton.displayString = "Personality: " + villager.attributes.getPersonality().getFriendlyName();
			}
			
			if (currentPage == 2)
			{
				int displayHeight = Math.round(villager.attributes.getScaleHeight() * 100);
				int displayGirth = Math.round(villager.attributes.getScaleWidth() * 100);
				
				heightButton.displayString = "Height Factor: " + displayHeight;
				girthButton.displayString = "Girth Factor: " + displayGirth;
				isInfectedButton.displayString = "Is Infected: " + (villager.attributes.getIsInfected() ? "Yes" : "No");
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

		nameTextField = new GuiTextField(1, fontRenderer, width / 2 - 205, height / 2 - 95, 150, 20);
		nameTextField.setMaxStringLength(32);
		nameTextField.setText(villager.attributes.getName());

		dummyTextField = new GuiTextField(2, fontRenderer, width / 2 + 90, height / 2 - 100, 100, 200);
		dummyTextField.setMaxStringLength(0);
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		villager.isInteractionGuiOpen = false;
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
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.RANDOM_NAME));
			nameTextField.mouseClicked(5, 5, 5);
			drawEditorGuiPage1();
		}

		else if (guibutton == genderButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.SWAP_GENDER));
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTextureIndexUpButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.TEXTURE_UP));
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTextureIndexDownButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.TEXTURE_DOWN));
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftProfessionUpButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.PROFESSION_UP));
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftProfessionDownButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.PROFESSION_DOWN));
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTraitUpButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.TRAIT_UP));
			drawEditorGuiPage1();
		}

		else if (guibutton == shiftTraitDownButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.TRAIT_DOWN));
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
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.HEIGHT_UP));
			drawEditorGuiPage2();
		}

		else if (guibutton == shiftHeightDownButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.HEIGHT_DOWN));
			drawEditorGuiPage2();
		}

		else if (guibutton == shiftGirthUpButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.GIRTH_UP));
			drawEditorGuiPage2();
		}

		else if (guibutton == shiftGirthDownButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.GIRTH_DOWN));
			drawEditorGuiPage2();
		}
		
		else if (guibutton == isInfectedButton)
		{
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.TOGGLE_INFECTED));
			drawEditorGuiPage2();
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
			MCA.getPacketHandler().sendPacketToServer(new PacketEditVillager(villager.getEntityId(), EnumEditAction.SET_NAME, text));
			villager.attributes.setName(text);
			
			//playerSkinTextField.textboxKeyTyped(c, i);
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
			//playerSkinTextField.mouseClicked(clickX, clickY, clicked);
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
		buttonList.add(randomButton = new GuiButton(1, width / 2 - 50, height / 2 - 95, 60, 20, MCA.getLocalizer().getString("gui.button.random")));
		buttonList.add(genderButton = new GuiButton(2, width / 2 - 190, height / 2 - 60, 175, 20, MCA.getLocalizer().getString("gui.button.setup.gender." + villager.attributes.getIsMale())));
		buttonList.add(textureButton = new GuiButton(3, width / 2 - 190, height / 2 - 40, 175, 20, "Texture: " + villager.attributes.getHeadTexture().replace("mca:textures/skins/", "").replace(".png", "")));
		buttonList.add(shiftTextureIndexUpButton = new GuiButton(4, width / 2 - 15, height / 2 - 40, 20, 20, ">>"));
		buttonList.add(shiftTextureIndexDownButton = new GuiButton(5, width / 2 - 210, height / 2 - 40, 20, 20, "<<"));
		buttonList.add(professionButton = new GuiButton(6, width / 2 - 190, height / 2 - 20, 175, 20, "Job: " + villager.attributes.getProfessionEnum().getUserFriendlyForm(villager)));
		buttonList.add(shiftProfessionUpButton = new GuiButton(7, width / 2 - 15, height / 2 - 20, 20, 20, ">>"));
		buttonList.add(shiftProfessionDownButton = new GuiButton(8, width / 2 - 210, height / 2 - 20, 20, 20, "<<"));
		buttonList.add(personalityButton = new GuiButton(12, width / 2 - 190, height / 2 + 0, 175, 20, MCA.getLocalizer().getString("gui.info.personality", villager.attributes.getPersonality().getFriendlyName())));
		buttonList.add(shiftTraitUpButton = new GuiButton(13, width / 2 - 15, height / 2 + 0, 20, 20, ">>"));
		buttonList.add(shiftTraitDownButton = new GuiButton(14, width / 2 - 210, height / 2 + 0, 20, 20, "<<"));
		
		buttonList.add(doneButton = new GuiButton(17, width / 2 - 50, height / 2 + 85, 75, 20, MCA.getLocalizer().getString("gui.button.done")));
		buttonList.add(nextButton = new GuiButton(18, width / 2 + 25, height / 2 + 85, 50, 20, MCA.getLocalizer().getString("gui.button.next")));
		buttonList.add(backButton = new GuiButton(19, width / 2 - 101, height / 2 + 85, 50, 20, MCA.getLocalizer().getString("gui.button.back")));

		backButton.enabled = false;
	}

	/**
	 * Draws the editor GUI.
	 */
	private void drawEditorGuiPage2()
	{
		final int displayHeight = Math.round(villager.attributes.getScaleHeight() * 100);
		final int displayGirth = Math.round(villager.attributes.getScaleWidth() * 100);

		currentPage = 2;
		buttonList.clear();
		buttonList.add(heightButton = new GuiButton(1, width / 2 - 190, height / 2 - 40, 175, 20, "Height Factor: " + displayHeight));
		buttonList.add(shiftHeightUpButton = new GuiButton(2, width / 2 - 15, height / 2 - 40, 20, 20, ">>"));
		buttonList.add(shiftHeightDownButton = new GuiButton(3, width / 2 - 210, height / 2 - 40, 20, 20, "<<"));
		buttonList.add(girthButton = new GuiButton(4, width / 2 - 190, height / 2 - 20, 175, 20, "Girth Factor: " + displayGirth));
		buttonList.add(shiftGirthUpButton = new GuiButton(5, width / 2 - 15, height / 2 - 20, 20, 20, ">>"));
		buttonList.add(shiftGirthDownButton = new GuiButton(6, width / 2 - 210, height / 2 - 20, 20, 20, "<<"));
		buttonList.add(isInfectedButton = new GuiButton(7, width / 2 - 190, height / 2 - 0, 175, 20, "Is Infected: " + villager.attributes.getIsInfected()));
		
		buttonList.add(doneButton = new GuiButton(16, width / 2 - 50, height / 2 + 85, 75, 20, MCA.getLocalizer().getString("gui.button.done")));
		buttonList.add(nextButton = new GuiButton(17, width / 2 + 25, height / 2 + 85, 50, 20, MCA.getLocalizer().getString("gui.button.next")));
		buttonList.add(backButton = new GuiButton(18, width / 2 - 101, height / 2 + 85, 50, 20, MCA.getLocalizer().getString("gui.button.back")));

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

			if (!villager.getBehavior(ActionSleep.class).getIsSleeping())
			{
				posY = height / 2 + 80;
			}

			net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen(posX, posY, 75, 0, 0, villager);
			dummyTextField.drawTextBox();
		}

		if (currentPage == 1)
		{
			drawString(fontRenderer, "Name:", width / 2 - 205, height / 2 - 110, 0xffffff);
			drawString(fontRenderer, MCA.getLocalizer().getString("gui.title.editor"), width / 2 - 205, height / 2 - 87, 0xa0a0a0);
			
			nameTextField.drawTextBox();
		}

		GL11.glPushMatrix();
		GL11.glScalef(1.5F, 1.5F, 1.5F);
		drawCenteredString(fontRenderer, MCA.getLocalizer().getString("gui.title.editor"), width / 2 - 75, height / 2 - 125, 0xffffff);
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
