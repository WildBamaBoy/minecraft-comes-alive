package mca.client.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import mca.core.MCA;
import mca.tile.TileTombstone;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTombstone extends GuiScreen
{
	private static final String allowedCharacters = new String(ChatAllowedCharacters.ILLEGAL_FILE_CHARACTERS);

	private final TileTombstone entityTombstone;
	private int updateCounter;
	private int editLine;

	public GuiTombstone(TileTombstone tileEntityTombstone)
	{
		super();
		entityTombstone = tileEntityTombstone;
		editLine = 0;
	}

	@Override
	public void initGui()
	{
		buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 + 70, MCA.getLanguageManager().getString("gui.button.ok")));
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		final NetHandlerPlayClient nethandlerplayclient = mc.getConnection();

		if (nethandlerplayclient != null)
		{
			TextComponentString[] textArray = new TextComponentString[4];
			
			for (int i = 0; i < entityTombstone.signText.length; i++)
			{
				textArray[i] = (TextComponentString) entityTombstone.signText[i];
			}
			nethandlerplayclient.sendPacket(new CPacketUpdateSign(entityTombstone.getPos(), textArray));
		}

//		MCA.getPacketHandler().sendPacketToServer(new PacketTombstoneUpdateSet(entityTombstone));
//		entityTombstone.hasSynced = true;
//		entityTombstone.guiOpen = false;
	}

	@Override
	public void updateScreen()
	{
		updateCounter++;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if (guibutton.enabled)
		{
			if (guibutton.id == 0)
			{
				entityTombstone.markDirty();
				mc.displayGuiScreen(null);
			}

			return;
		}
	}

	@Override
	protected void keyTyped(char c, int id)
	{
		if (id == 200)
		{
			editLine = editLine - 1 & 3;
		}

		if (id == 208 || id == 28 || id == 156)
		{
			editLine = editLine + 1 & 3;
		}

		if (id == 14 && entityTombstone.signText[editLine].getUnformattedText().length() > 0)
		{
			entityTombstone.signText[editLine] = new TextComponentString(entityTombstone.signText[editLine].getUnformattedText().substring(0, entityTombstone.signText[editLine].getUnformattedText().length() - 1));
		}

		if (ChatAllowedCharacters.isAllowedCharacter(c) && entityTombstone.signText[editLine].getUnformattedText().length() < 15)
		{
			entityTombstone.signText[editLine] = new TextComponentString(entityTombstone.signText[editLine].getUnformattedText() + c);
		}

		if (id == 0)
		{
			actionPerformed((GuiButton) buttonList.get(0));
		}
	}

	@Override
	public void drawScreen(int sizeX, int sizeY, float offset)
	{
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, MCA.getLanguageManager().getString("gui.title.tombstone"), width / 2, 40, 0xffffff);

		GL11.glPushMatrix();

		GL11.glTranslatef(width / 2, -50.0F, 50F);
		GL11.glScalef(-150.00F, -150.00F, -150.00F);
		GL11.glTranslatef(0, -0.8F, 0);
		GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);

		final float rotationAngle = entityTombstone.getBlockMetadata() * 360 / 16F;
		GL11.glRotatef(rotationAngle, 0.0F, 1.0F, 0.0F);

		if (updateCounter / 6 % 2 == 0)
		{
			entityTombstone.lineBeingEdited = editLine;
		}

		TileEntityRendererDispatcher.instance.renderTileEntityAt(entityTombstone, -0.5D, -0.75D, -0.5D, 0.0F);
		entityTombstone.lineBeingEdited = -1;

		GL11.glPopMatrix();

		super.drawScreen(sizeX, sizeY, offset);
	}
}
