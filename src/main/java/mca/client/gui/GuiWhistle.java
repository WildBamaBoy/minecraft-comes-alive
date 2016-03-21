package mca.client.gui;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import mca.core.MCA;
import mca.data.VillagerSaveData;
import mca.entity.EntityHuman;
import mca.packets.PacketCallVillager;
import mca.packets.PacketRequestRelatedVillagers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.data.DataWatcherEx;

/**
 * Defines the GUI shown when the player right clicks the whistle.
 */
@SideOnly(Side.CLIENT)
public class GuiWhistle extends GuiScreen
{
	private EntityHuman dummyHuman;

	private List<VillagerSaveData> villagerDataList;

	private GuiButton selectionLeftButton;
	private GuiButton selectionRightButton;
	private GuiButton villagerNameButton;
	private GuiButton callButton;
	private GuiButton callAllButton;
	private GuiButton exitButton;
	private int loadingAnimationTicks;
	private int selectedIndex;

	public GuiWhistle(EntityPlayer player)
	{
		super();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if (loadingAnimationTicks != -1)
		{
			loadingAnimationTicks++;
		}

		if (loadingAnimationTicks >= 20)
		{
			loadingAnimationTicks = 0;
		}
	}

	@Override
	public void initGui()
	{
		buttonList.clear();

		buttonList.add(selectionLeftButton = new GuiButton(1, width / 2 - 123, height / 2 + 65, 20, 20, "<<"));
		buttonList.add(selectionRightButton = new GuiButton(2, width / 2 + 103, height / 2 + 65, 20, 20, ">>"));
		buttonList.add(villagerNameButton = new GuiButton(3, width / 2 - 100, height / 2 + 65, 200, 20, ""));
		buttonList.add(callButton = new GuiButton(4, width / 2 - 100, height / 2 + 90, 60, 20, MCA.getLanguageManager().getString("gui.button.call")));
		buttonList.add(callAllButton = new GuiButton(5, width / 2 - 30, height / 2 + 90, 60, 20, MCA.getLanguageManager().getString("gui.button.callall")));
		buttonList.add(exitButton = new GuiButton(6, width / 2 + 40, height / 2 + 90, 60, 20, MCA.getLanguageManager().getString("gui.button.exit")));

		MCA.getPacketHandler().sendPacketToServer(new PacketRequestRelatedVillagers());
	}

	@Override
	public boolean doesGuiPauseGame() 
	{
		return false;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if (guibutton == exitButton)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
		}

		if (villagerDataList.size() > 0)
		{
			if (guibutton == selectionLeftButton)
			{
				if (selectedIndex == 1)
				{
					selectedIndex = villagerDataList.size();
				}

				else
				{
					selectedIndex--;
				}
			}

			else if (guibutton == selectionRightButton)
			{
				if (selectedIndex == villagerDataList.size())
				{
					selectedIndex = 1;
				}

				else
				{
					selectedIndex++;
				}
			}

			else if (guibutton == callButton)
			{
				MCA.getPacketHandler().sendPacketToServer(new PacketCallVillager(villagerDataList.get(selectedIndex - 1).uuid));
				Minecraft.getMinecraft().displayGuiScreen(null);
			}

			else if (guibutton == callAllButton)
			{
				MCA.getPacketHandler().sendPacketToServer(new PacketCallVillager(true));
				Minecraft.getMinecraft().displayGuiScreen(null);
			}

			VillagerSaveData data = villagerDataList.get(selectedIndex - 1);
			villagerNameButton.displayString = data.displayTitle;
			updateDummyVillagerWithData(data);
		}
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, MCA.getLanguageManager().getString("gui.title.whistle"), width / 2, height / 2 - 110, 0xffffff);

		if (loadingAnimationTicks != -1)
		{
			drawString(fontRendererObj, "Loading" + StringUtils.repeat(".", loadingAnimationTicks % 10), width / 2 - 20, height / 2 - 10, 0xffffff);
		}

		else
		{
			if (villagerDataList.size() == 0)
			{
				drawCenteredString(fontRendererObj, "No family members could be found in the area.", width / 2, height / 2 + 50, 0xffffff);				
			}

			else
			{
				drawCenteredString(fontRendererObj, selectedIndex + " / " + villagerDataList.size(), width / 2, height / 2 + 50, 0xffffff);
			}
		}

		if (dummyHuman != null)
		{
			drawDummyVillager();
		}

		super.drawScreen(sizeX, sizeY, offset);
	}

	private void drawDummyVillager()
	{
		final int posX = width / 2;
		int posY = height / 2 + 45;
		final int scale = 70;

		net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen(posX, posY, 75, 0, 0, dummyHuman);
	}

	public void setVillagerDataList(List<VillagerSaveData> dataList)
	{
		this.villagerDataList = dataList;
		this.loadingAnimationTicks = -1;
		this.selectedIndex = 1;

		try
		{
			VillagerSaveData firstData = dataList.get(0);
			villagerNameButton.displayString = firstData.displayTitle;
			dummyHuman = new EntityHuman(Minecraft.getMinecraft().theWorld);

			updateDummyVillagerWithData(firstData);
		}

		catch (IndexOutOfBoundsException e) //When no family members are found.
		{
			callButton.enabled = false;
			callAllButton.enabled = false;
		}
	}

	private void updateDummyVillagerWithData(VillagerSaveData data)
	{
		DataWatcherEx.allowClientSideModification = true;
		dummyHuman.setIsMale(data.isMale);
		dummyHuman.setProfessionId(data.professionId);
		dummyHuman.setHeadTexture(data.headTexture);
		dummyHuman.setClothesTexture(data.clothesTexture);
		dummyHuman.setIsChild(data.isChild);
		dummyHuman.setAge(data.age);
		dummyHuman.setGirth(data.scaleGirth);
		dummyHuman.setHeight(data.scaleHeight);
		dummyHuman.setDoDisplay(true);
		DataWatcherEx.allowClientSideModification = false;
	}
}
