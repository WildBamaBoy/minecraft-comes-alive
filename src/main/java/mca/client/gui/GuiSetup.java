package mca.client.gui;

import java.io.IOException;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import mca.core.MCA;
import mca.core.forge.EventHooksFML;
import mca.data.NBTPlayerData;
import mca.enums.EnumDestinyChoice;
import mca.enums.EnumGender;
import mca.packets.PacketDestinyChoice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Font.Color;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.client.RadixRender;
import radixcore.modules.schematics.BlockObj;
import radixcore.modules.schematics.RadixSchematics;

@SideOnly(Side.CLIENT)
public class GuiSetup extends GuiScreen
{
	private static ResourceLocation setupLogo = new ResourceLocation("mca:textures/setup.png");
	
	private EntityPlayer player;
	private NBTPlayerData data;
	private EnumDestinyChoice destinyChoice;
	private GuiTextField nameTextField;

	private int page;
	public GuiSetup(EntityPlayer player)
	{
		super();
		this.player = player;
		this.data = MCA.getPlayerData(player);
	}

	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);
		page = 1;
		drawControls();
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if (page == 3 && nameTextField != null)
		{
			nameTextField.updateCursorCounter();
		}
	}

	@Override
	public void handleMouseInput() throws IOException 
	{
		super.handleMouseInput();
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		if (page == 6)
		{
			drawBackground(0);
		}
		
		else
		{
			drawDefaultBackground();
		}

		GL11.glPushMatrix();
		{
			GL11.glScaled(0.55D, 0.25D, 1.0D);
			RadixRender.drawTexturedRectangle(setupLogo, width / 2 + 62, height / 2 - 120, 0, 0, 256, 256);
		}
		GL11.glPopMatrix();


		if (page == 1)
		{
			drawCenteredString(fontRenderer, "Are you a male, or a female?", width / 2, 120, 0xffffff);
		}

		else if (page == 2)
		{
			drawCenteredString(fontRenderer, "Which do you prefer?", width / 2, 120, 0xffffff);
		}

		else if (page == 3 && nameTextField != null)
		{
			drawCenteredString(fontRenderer, "What is your name?", width / 2, 100, 0xffffff);
			nameTextField.drawTextBox();
		}

		else if (page == 4)
		{
			drawCenteredString(fontRenderer, "Choose your destiny...", width / 2, 70, 0xffffff);
		}
		
		else if (page == 5)
		{
			drawCenteredString(fontRenderer, "WARNING: This destiny can potentially be destructive to your world.", width / 2, 70, 0xffffff);
			drawCenteredString(fontRenderer, "This option works best on flat land with no other structures nearby. Continue?", width / 2, 85, 0xffffff);
		}

		super.drawScreen(sizeX, sizeY, offset);
		drawControls();
	}

	private void cleanUpOnClose()
	{
		try
		{
			Map<Point3D, BlockObj> destinySchematic = RadixSchematics.readSchematic("/assets/mca/schematic/destiny-test.schematic");

			//Purge the old schematic.
			for (Map.Entry<Point3D, BlockObj> entry : destinySchematic.entrySet())
			{
				int y = MCA.destinyCenterPoint.iY() + entry.getKey().iY();

				if (y > (int)player.posY - 2)
				{
					RadixBlocks.setBlock(player.world,
							MCA.destinyCenterPoint.iX() + entry.getKey().iX(), 
							y, 
							MCA.destinyCenterPoint.iZ() + entry.getKey().iZ(), Blocks.AIR);
				}
			}
		}

		catch (NullPointerException e)
		{
			//Ignore NPE here due to using on LAN or dedicated server.
		}

		EntityPlayerSP playerSP = (EntityPlayerSP)player;
		EventHooksFML.playPortalAnimation = true;
		playerSP.timeInPortal = 6.0F;
		playerSP.prevTimeInPortal = 0.0F;

		MCA.destinySpawnFlag = false;
		
		player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, 0.5F, 2.0F);
	}
	
	@Override
	protected void keyTyped(char c, int i)
	{
		if (page == 3 && nameTextField != null)
		{
			nameTextField.textboxKeyTyped(c, i);
		}
	}

	@Override
	protected void mouseClicked(int clickX, int clickY, int clicked) throws IOException
	{
		super.mouseClicked(clickX, clickY, clicked);

		if (page == 3 && nameTextField != null)
		{
			nameTextField.mouseClicked(clickX, clickY, clicked);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		//Page switching
		switch (button.id)
		{
		case 0: page = page == 6 ? 1 : page - 1; break;
		case 1: case 2: 					page = 2; break;
		case 3: case 4: case 5: 			page = 3; break;
		case 6: if (MCA.getConfig().enableStructureSpawning) page = 4; break;
		default:
			page = 1;
		}

		//Button actions.
		switch (button.id)
		{
		case 1: data.setGender(EnumGender.MALE); break;
		case 2: data.setGender(EnumGender.FEMALE); break;
		case 3: data.setGenderPreference(EnumGender.MALE); break;
		case 4: data.setGenderPreference(EnumGender.UNASSIGNED); break;
		case 5: data.setGenderPreference(EnumGender.FEMALE); break;
		case 6: 
			data.setMcaName(nameTextField.getText());

			boolean isDedicatedServer = !Minecraft.getMinecraft().isIntegratedServerRunning();
			
			//Skip destiny choices if on single player and the option is disabled, or if we're on
			//a dedicated server and the server option is disabled. Server option is disabled by
			//default, and is actually verified server-side before spawning the structure.
			if ((!isDedicatedServer && !MCA.getConfig().enableStructureSpawning) ||
				(isDedicatedServer && !MCA.getConfig().serverEnableStructureSpawning)) 
			{
				setDestinyComplete();
				cleanUpOnClose();
				mc.displayGuiScreen(null);
				MCA.getPacketHandler().sendPacketToServer(new PacketDestinyChoice(EnumDestinyChoice.NONE));
			}

			break;

		case 7: destinyChoice = EnumDestinyChoice.FAMILY; page = 5; break;
		case 8: destinyChoice = EnumDestinyChoice.ALONE; page = 5; break;
		case 9: destinyChoice = EnumDestinyChoice.VILLAGE; page = 5; break;
		case 10: //No destiny.
			data.setHasChosenDestiny(true);
			setDestinyComplete();
			cleanUpOnClose();
			mc.displayGuiScreen(null);
			
			MCA.getPacketHandler().sendPacketToServer(new PacketDestinyChoice(EnumDestinyChoice.NONE)); break;
		case 11: //Confirmation button to spawn destiny area.
			data.setHasChosenDestiny(true);
			setDestinyComplete();
			cleanUpOnClose();
			mc.displayGuiScreen(null);
			
			MCA.getPacketHandler().sendPacketToServer(new PacketDestinyChoice(destinyChoice)); 
			break;
		case 12: page = 4; break;
		case 13: MCA.getPacketHandler().sendPacketToServer(new PacketDestinyChoice(EnumDestinyChoice.CANCEL));
				cleanUpOnClose();
				mc.displayGuiScreen(null); break;
		}
	}

	@Override
	public boolean doesGuiPauseGame() 
	{
		return false;
	}

	private void drawControls()
	{
		buttonList.clear();
		
		if (page > 1)
		{
			buttonList.add(new GuiButton(0, width / 2 - 200, height / 2 + 90, 65, 20, "Back"));
		}

		if (page == 1)
		{
			buttonList.add(new GuiButton(1, width / 2 - 65, height / 2 + 10, 65, 20, Color.AQUA + "Male"));
			buttonList.add(new GuiButton(2, width / 2 + 2, height / 2 + 10, 65, 20, Color.LIGHTPURPLE + "Female"));
		}

		else if (page == 2)
		{
			buttonList.add(new GuiButton(3, width / 2 - 97, height / 2 + 10, 65, 20, Color.AQUA + "Males"));
			buttonList.add(new GuiButton(4, width / 2 - 32, height / 2 + 10, 65, 20, Color.GREEN + "Either"));
			buttonList.add(new GuiButton(5, width / 2 + 33, height / 2 + 10, 65, 20, Color.LIGHTPURPLE + "Females"));
		}

		else if (page == 3)
		{
			if (nameTextField == null)
			{
				nameTextField = new GuiTextField(-3, fontRenderer, width / 2 - 100, height / 2 - 5, 200, 20);
				nameTextField.setText(player.getName());
			}

			GuiButton doneButton = new GuiButton(6, width / 2 - 32, height / 2 + 30, 65, 20, MCA.getConfig().enableStructureSpawning ? "Continue" : "Done");
			doneButton.enabled = !nameTextField.getText().trim().isEmpty();
			buttonList.add(doneButton);
		}

		else if (page == 4)
		{
			buttonList.add(new GuiButton(7, width / 2 - 46, height / 2 - 40, 95, 20, "I have a family."));
			buttonList.add(new GuiButton(8, width / 2 - 46, height / 2 - 20, 95, 20, "I live alone."));
			buttonList.add(new GuiButton(9, width / 2 - 46, height / 2 + 0, 95, 20, "I live in a village."));
			buttonList.add(new GuiButton(10, width / 2 - 46, height / 2 + 20, 95, 20, "None of these."));
		}
		
		else if (page == 5)
		{
			buttonList.add(new GuiButton(11, width / 2 - 46, height / 2 - 20, 95, 20, "Yes"));
			buttonList.add(new GuiButton(12, width / 2 - 46, height / 2 - 0, 95, 20, "No"));
			buttonList.add(new GuiButton(13, width / 2 - 46, height / 2 + 20, 95, 20, "Cancel"));
		}
	}

	private void setDestinyComplete()
	{
		data.setHasChosenDestiny(true);
	}
}
