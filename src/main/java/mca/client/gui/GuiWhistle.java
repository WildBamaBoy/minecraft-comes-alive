package mca.client.gui;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mca.core.MCA;
import mca.data.VillagerSaveData;
import mca.entity.EntityHuman;
import mca.packets.PacketCallVillager;
import mca.packets.PacketRequestRelatedVillagers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
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

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY, 50.0F);
		GL11.glScalef(-scale, scale, scale);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

		final float yawOffset = dummyHuman.renderYawOffset;
		final float rotationYaw = dummyHuman.rotationYaw;
		final float rotationPitch = dummyHuman.rotationPitch;

		GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-((float) Math.atan(0F / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);

		dummyHuman.renderYawOffset = (float) Math.atan(0F / 40.0F) * 20.0F;
		dummyHuman.rotationYaw = (float) Math.atan(0F / 40.0F) * 40.0F;
		dummyHuman.rotationPitch = -((float) Math.atan(0F / 40.0F)) * 20.0F;
		dummyHuman.rotationYawHead = dummyHuman.rotationYaw;

		GL11.glTranslatef(0.0F, dummyHuman.yOffset, 0.0F);

		RenderManager.instance.playerViewY = 180.0F;
		RenderManager.instance.renderEntityWithPosYaw(dummyHuman, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);

		dummyHuman.renderYawOffset = yawOffset;
		dummyHuman.rotationYaw = rotationYaw;
		dummyHuman.rotationPitch = rotationPitch;

		GL11.glPopMatrix();

		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
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
