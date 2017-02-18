package mca.client.gui;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.inventory.ContainerInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Defines the inventory GUI shown when changing an entity's inventory.
 */
@SideOnly(Side.CLIENT)
public class GuiInventory extends InventoryEffectRenderer
{
	private static final ResourceLocation resourceLocation = new ResourceLocation("mca:textures/gui/container/inventory.png");

	private final EntityVillagerMCA owner;
	private GuiButton backButton;
	private GuiButton exitButton;

	/** The number of rows in the inventory. */
	private final int inventoryRows;

	/** Has the inventory been opened from the villager editor? */
	private boolean fromEditor = false;

	/**
	 * Constructor
	 * 
	 * @param entity The entity who owns the inventory being accessed.
	 * @param playerInventory The inventory of the player opening this GUI.
	 * @param entityInventory The inventory of the entity that the player is interacting with.
	 * @param fromEditor Is this GUI being opened from the villager editor?
	 */
	public GuiInventory(EntityVillagerMCA entity, IInventory playerInventory, IInventory entityInventory, boolean fromEditor)
	{
		super(new ContainerInventory(playerInventory, entityInventory, entity));

		owner = entity;
		allowUserInput = false;
		this.fromEditor = fromEditor;

		final char c = '\336';
		final int i = c - 108;
		inventoryRows = entityInventory.getSizeInventory() / 9;
		xSize = xSize + 24;
		ySize = i + inventoryRows * 18;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.clear();
		buttonList.add(backButton = new GuiButton(10, width / 2 - 190, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageManager().getString("gui.button.back")));
		buttonList.add(exitButton = new GuiButton(11, width / 2 + 125, height / 2 + 85, 65, 20, MCA.getInstance().getLanguageManager().getString("gui.button.exit")));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if (guibutton == backButton)
		{
			
		}

		else if (guibutton == exitButton)
		{
			if (!fromEditor)
			{
				Minecraft.getMinecraft().displayGuiScreen(null);
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRendererObj.drawString("Inventory", 32, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float offset, int mouseX, int mouseY)
	{
		mc.getTextureManager().bindTexture(resourceLocation);

		final int addX = Minecraft.getMinecraft().thePlayer.getActivePotionEffects().size() > 0 ? 120 : 0;

		//Draw the two inventories.
		final int x = (width - xSize + addX) / 2;
		final int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize + 26, inventoryRows * 18 + 21); //Top inventory
		drawTexturedModalRect(x, y + inventoryRows * 18 + 17, 0, 126, xSize + 26, 96); //Bottom inventory
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		owner.getVillagerInventory().closeInventory(Minecraft.getMinecraft().thePlayer);
	}
}