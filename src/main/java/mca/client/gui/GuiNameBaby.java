package mca.client.gui;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mca.core.MCA;
import mca.items.ItemBaby;
import mca.packets.PacketBabyName;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Defines the GUI shown when the player must name their child.
 */
@SideOnly(Side.CLIENT)
public class GuiNameBaby extends GuiScreen
{
	private final EntityPlayer player;
	
	private GuiTextField babyNameTextField;
	private final boolean isMale;
	private GuiButton doneButton;
	private GuiButton randomButton;

	public GuiNameBaby(EntityPlayer player, boolean isMale)
	{
		super();
		this.player = player;
		this.isMale = isMale;
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		try
		{
			babyNameTextField.updateCursorCounter();

			if (babyNameTextField.getText().isEmpty())
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

		buttonList.clear();
		buttonList.add(doneButton = new GuiButton(1, width / 2 - 40, height / 2 - 10, 80, 20, MCA.getLanguageManager().getString("gui.button.done")));
		buttonList.add(randomButton = new GuiButton(2, width / 2 + 105, height / 2 - 60, 60, 20, MCA.getLanguageManager().getString("gui.button.random")));

		babyNameTextField = new GuiTextField(fontRendererObj, width / 2 - 100, height / 2 - 60, 200, 20);
		babyNameTextField.setMaxStringLength(32);
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public boolean doesGuiPauseGame() 
	{
		return false;
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
			int slot = -1;
			
			for (int i = 0; i < player.inventory.mainInventory.length; i++)
			{
				ItemStack stack = player.inventory.mainInventory[i];
				
				if (stack != null && stack.getItem() instanceof ItemBaby && stack.getTagCompound().getString("name").equals("Unnamed"))
				{
					ItemBaby item = (ItemBaby) stack.getItem();
					
					if ((item.getIsBoy() && isMale) || (!item.getIsBoy() && !isMale))
					{
						slot = i;
						break;
					}
				}
			}

			MCA.getPacketHandler().sendPacketToServer(new PacketBabyName(babyNameTextField.getText().trim(), slot));
			mc.displayGuiScreen(null);
		}

		else if (guibutton == randomButton)
		{
			if (isMale)
			{
				babyNameTextField.setText(MCA.getLanguageManager().getString("name.male"));
			}
			
			else
			{
				babyNameTextField.setText(MCA.getLanguageManager().getString("name.female"));
			}
		}
	}

	@Override
	protected void keyTyped(char c, int i)
	{
		babyNameTextField.textboxKeyTyped(c, i);
	}

	@Override
	protected void mouseClicked(int clickX, int clickY, int clicked)
	{
		super.mouseClicked(clickX, clickY, clicked);
		babyNameTextField.mouseClicked(clickX, clickY, clicked);
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawDefaultBackground();

		if (isMale)
		{
			drawCenteredString(fontRendererObj, MCA.getLanguageManager().getString("gui.title.namebaby.male"), width / 2, height / 2 - 90, 0xffffff);
		}

		else
		{
			drawCenteredString(fontRendererObj, MCA.getLanguageManager().getString("gui.title.namebaby.female"), width / 2, height / 2 - 90, 0xffffff);
		}

		drawString(fontRendererObj, MCA.getLanguageManager().getString("gui.title.namebaby"), width / 2 - 100, height / 2 - 70, 0xa0a0a0);

		babyNameTextField.drawTextBox();
		super.drawScreen(sizeX, sizeY, offset);
	}
}
