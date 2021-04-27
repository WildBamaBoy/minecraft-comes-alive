package mca.client.gui;

import lombok.NonNull;
import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import cobalt.minecraft.nbt.CNBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiWhistle extends Screen {
    private EntityVillagerMCA dummyHuman;
    private List<CNBT> villagerDataList;

    private Button selectionLeftButton;
    private Button selectionRightButton;
    private Button villagerNameButton;
    private Button callButton;
    private Button exitButton;
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
        buttonList.add(selectionLeftButton = new Button(1, width / 2 - 123, height / 2 + 65, 20, 20, "<<"));
        buttonList.add(selectionRightButton = new Button(2, width / 2 + 103, height / 2 + 65, 20, 20, ">>"));
        buttonList.add(villagerNameButton = new Button(3, width / 2 - 100, height / 2 + 65, 200, 20, ""));
        buttonList.add(callButton = new Button(4, width / 2 - 100, height / 2 + 90, 60, 20, MCA.localize("gui.button.call")));
        buttonList.add(exitButton = new Button(6, width / 2 + 40, height / 2 + 90, 60, 20, MCA.localize("gui.button.exit")));
        NetMCA.INSTANCE.sendToServer(new NetMCA.GetFamily());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void actionPerformed(Button guibutton) {
        if (guibutton == exitButton) {
            Minecraft.getMinecraft().displayScreen(null);
        }

        if (villagerDataList != null && villagerDataList.size() > 0) {
            CNBT data = villagerDataList.get(selectedIndex - 1);

            if (guibutton == selectionLeftButton) {
                if (selectedIndex == 1) {
                    selectedIndex = villagerDataList.size();
                } else {
                    selectedIndex--;
                }
            } else if (guibutton == selectionRightButton) {
                if (selectedIndex == villagerDataList.size()) {
                    selectedIndex = 1;
                } else {
                    selectedIndex++;
                }
            } else if (guibutton == callButton) {
                NetMCA.INSTANCE.sendToServer(new NetMCA.CallToPlayer(data.getUUID("uuid")));
                Minecraft.getMinecraft().displayScreen(null);
            }

            villagerNameButton.displayString = data.getString("name");
            dummyHuman.readAppearanceFromNBT(data);
        }
    }

    @Override
    public void drawScreen(int sizeX, int sizeY, float offset) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, MCA.localize("gui.title.whistle"), width / 2, height / 2 - 110, 0xffffff);

        if (loadingAnimationTicks != -1) {
            drawString(fontRenderer, "Loading" + StringUtils.repeat(".", loadingAnimationTicks % 10), width / 2 - 20, height / 2 - 10, 0xffffff);
        } else {
            if (villagerDataList.size() == 0) {
                drawCenteredString(fontRenderer, "No family members could be found in the area.", width / 2, height / 2 + 50, 0xffffff);
            } else {
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

    public void setVillagerDataList(@NonNull List<CNBT> dataList) {
        this.villagerDataList = dataList;
        this.loadingAnimationTicks = -1;
        this.selectedIndex = 1;

        try {
            CNBT firstData = dataList.get(0);
            villagerNameButton.displayString = firstData.getString("name");
            dummyHuman = new EntityVillagerMCA(Minecraft.getMinecraft().world);
            dummyHuman.readAppearanceFromNBT(firstData);
        } catch (IndexOutOfBoundsException e) {
            callButton.enabled = false;
        }
    }
}