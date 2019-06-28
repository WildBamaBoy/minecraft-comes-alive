package mca.client.gui;

import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class GuiStaffOfLife extends GuiScreen {
    private Map<String, NBTTagCompound> villagerData;
    private GuiButton reviveButton;
    private GuiButton nameButton;
    private GuiButton backButton;
    private GuiButton nextButton;
    private GuiButton closeButton;
    private EntityVillagerMCA dummy;
    private EntityPlayer player;

    // selection fields
    private int index = 0;
    private List<String> keys = new ArrayList<>();

    public GuiStaffOfLife(EntityPlayer player) {
        super();
        this.player = player;
    }

    @Override
    public void initGui() {
        NetMCA.INSTANCE.sendToServer(new NetMCA.SavedVillagersRequest());

        buttonList.clear();
        buttonList.add(backButton = new GuiButton(1, width / 2 - 123, height / 2 + 65, 20, 20, "<<"));
        buttonList.add(nextButton = new GuiButton(2, width / 2 + 103, height / 2 + 65, 20, 20, ">>"));
        buttonList.add(nameButton = new GuiButton(3, width / 2 - 100, height / 2 + 65, 200, 20, ""));
        buttonList.add(reviveButton = new GuiButton(4, width / 2 - 100, height / 2 + 90, 60, 20, MCA.getLocalizer().localize("gui.button.revive")));
        buttonList.add(closeButton = new GuiButton(5, width / 2 + 40, height / 2 + 90, 60, 20, MCA.getLocalizer().localize("gui.button.exit")));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton == reviveButton) {
            NetMCA.INSTANCE.sendToServer(new NetMCA.ReviveVillager(UUID.fromString(keys.get(index))));
            mc.displayGuiScreen(null);
        } else if (guibutton == backButton) selectData(index - 1);
        else if (guibutton == nextButton) selectData(index + 1);
        else if (guibutton == closeButton) mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int sizeX, int sizeY, float offset) {
        drawDefaultBackground();
        drawDummy();
        drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.title.staffoflife"), width / 2, height / 2 - 110, 0xffffff);
        super.drawScreen(sizeX, sizeY, offset);
    }

    public void setVillagerData(Map<String, NBTTagCompound> data) {
        villagerData = data;

        if (data.size() > 0) {
            dummy = new EntityVillagerMCA(player.world);
            keys.addAll(data.keySet());
            selectData(0);
        } else {
            nameButton.displayString = "No villagers found.";
            backButton.enabled = false;
            nextButton.enabled = false;
            nameButton.enabled = false;
            reviveButton.enabled = false;
        }
    }

    private void updateDummy(NBTTagCompound nbt) {
        dummy.readEntityFromNBT(nbt);
        dummy.setHealth(20.0F);
    }

    private void selectData(int i) {
        if (i < 0) i = keys.size() - 1;
        else if (i > keys.size() - 1) i = 0;

        index = i;
        updateDummy(villagerData.get(keys.get(index)));
        nameButton.displayString = dummy.getDisplayName().getUnformattedText();
    }

    private void drawDummy() {
        int posX = width / 2;
        int posY = height / 2 + 45;

        if (dummy != null) GuiInventory.drawEntityOnScreen(posX, posY, 60, 0, 0, dummy);
    }
}