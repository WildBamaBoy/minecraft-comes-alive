package mca.client.gui;

import lombok.NonNull;
import mca.core.forge.NetMCA;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.StringUtils;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static mca.entity.EntityVillagerMCA.*;

@SideOnly(Side.CLIENT)
public class GuiWhistle extends GuiScreen {
    private EntityVillagerMCA dummyHuman;
    private List<NBTTagCompound> villagerDataList;

    private GuiButton selectionLeftButton;
    private GuiButton selectionRightButton;
    private GuiButton villagerNameButton;
    private GuiButton callButton;
    private GuiButton exitButton;
    private int loadingAnimationTicks;
    private int selectedIndex;

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (loadingAnimationTicks != -1) {
            loadingAnimationTicks++;
        }

        if (loadingAnimationTicks >= 20) {
            loadingAnimationTicks = 0;
        }
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(selectionLeftButton = new GuiButton(1, width / 2 - 123, height / 2 + 65, 20, 20, "<<"));
        buttonList.add(selectionRightButton = new GuiButton(2, width / 2 + 103, height / 2 + 65, 20, 20, ">>"));
        buttonList.add(villagerNameButton = new GuiButton(3, width / 2 - 100, height / 2 + 65, 200, 20, ""));
        buttonList.add(callButton = new GuiButton(4, width / 2 - 100, height / 2 + 90, 60, 20, MCA.getLocalizer().localize("gui.button.call")));
        buttonList.add(exitButton = new GuiButton(6, width / 2 + 40, height / 2 + 90, 60, 20, MCA.getLocalizer().localize("gui.button.exit")));
        NetMCA.INSTANCE.sendToServer(new NetMCA.GetFamily());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton == exitButton) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }

        if (villagerDataList != null && villagerDataList.size() > 0) {
            NBTTagCompound data = villagerDataList.get(selectedIndex - 1);

            if (guibutton == selectionLeftButton) {
                if (selectedIndex == 1) {
                    selectedIndex = villagerDataList.size();
                }

                else {
                    selectedIndex--;
                }
            }

            else if (guibutton == selectionRightButton) {
                if (selectedIndex == villagerDataList.size()) {
                    selectedIndex = 1;
                }

                else {
                    selectedIndex++;
                }
            }

            else if (guibutton == callButton) {
                NetMCA.INSTANCE.sendToServer(new NetMCA.CallToPlayer(data.getUniqueId("uuid")));
                Minecraft.getMinecraft().displayGuiScreen(null);
            }

            villagerNameButton.displayString = data.getString("name");
            updateDummyVillagerWithData(data);
        }
    }

    @Override
    public void drawScreen(int sizeX, int sizeY, float offset) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.title.whistle"), width / 2, height / 2 - 110, 0xffffff);

        if (loadingAnimationTicks != -1) {
            drawString(fontRenderer, "Loading" + StringUtils.repeat(".", loadingAnimationTicks % 10), width / 2 - 20, height / 2 - 10, 0xffffff);
        }

        else {
            if (villagerDataList.size() == 0) {
                drawCenteredString(fontRenderer, "No family members could be found in the area.", width / 2, height / 2 + 50, 0xffffff);
            }

            else {
                drawCenteredString(fontRenderer, selectedIndex + " / " + villagerDataList.size(), width / 2, height / 2 + 50, 0xffffff);
            }
        }

        if (dummyHuman != null) {
            drawDummyVillager();
        }

        super.drawScreen(sizeX, sizeY, offset);
    }

    private void drawDummyVillager() {
        final int posX = width / 2;
        int posY = height / 2 + 45;
        net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen(posX, posY, 75, 0, 0, dummyHuman);
    }

    public void setVillagerDataList(@NonNull List<NBTTagCompound> dataList) {
        this.villagerDataList = dataList;
        this.loadingAnimationTicks = -1;
        this.selectedIndex = 1;

        try {
            NBTTagCompound firstData = dataList.get(0);
            villagerNameButton.displayString = firstData.getString("name");
            dummyHuman = new EntityVillagerMCA(Minecraft.getMinecraft().world);
            updateDummyVillagerWithData(firstData);
        }

        catch (IndexOutOfBoundsException e) {
            callButton.enabled = false;
        }
    }

    private void updateDummyVillagerWithData(NBTTagCompound nbt) {
        dummyHuman.set(VILLAGER_NAME, nbt.getString("name"));
        dummyHuman.set(TEXTURE, nbt.getString("texture"));
        dummyHuman.set(GIRTH, nbt.getFloat("girth"));
        dummyHuman.set(TALLNESS, nbt.getFloat("tallness"));
        dummyHuman.set(IS_INFECTED, nbt.getBoolean("infected"));
        dummyHuman.set(AGE_STATE, nbt.getInteger("ageState"));
    }
}