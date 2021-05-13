package mca.client.gui;

import cobalt.minecraft.nbt.CNBT;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class GuiStaffOfLife extends Screen {
    private Map<String, CNBT> villagerData;
    private Button reviveButton;
    private Button nameButton;
    private Button backButton;
    private Button nextButton;
    private Button closeButton;
    private EntityVillagerMCA dummy;
    private final PlayerEntity player;

    // selection fields
    private int index = 0;
    private final List<String> keys = new ArrayList<>();

    public GuiStaffOfLife(PlayerEntity player) {
        super(new StringTextComponent("Staff of Life"));
        this.player = player;
    }

//    @Override
//    public void initGui() {
//        NetMCA.INSTANCE.sendToServer(new NetMCA.SavedVillagersRequest());
//
//        buttonList.clear();
//        buttonList.add(backButton = new Button(1, width / 2 - 123, height / 2 + 65, 20, 20, "<<"));
//        buttonList.add(nextButton = new Button(2, width / 2 + 103, height / 2 + 65, 20, 20, ">>"));
//        buttonList.add(nameButton = new Button(3, width / 2 - 100, height / 2 + 65, 200, 20, ""));
//        buttonList.add(reviveButton = new Button(4, width / 2 - 100, height / 2 + 90, 60, 20, MCA.localize("gui.button.revive")));
//        buttonList.add(closeButton = new Button(5, width / 2 + 40, height / 2 + 90, 60, 20, MCA.localize("gui.button.exit")));
//    }
//
//    @Override
//    public boolean doesGuiPauseGame() {
//        return false;
//    }
//
//    @Override
//    protected void actionPerformed(Button guibutton) {
//        if (guibutton == reviveButton) {
//            NetMCA.INSTANCE.sendToServer(new NetMCA.ReviveVillager(UUID.fromString(keys.get(index))));
//            mc.displayScreen(null);
//        } else if (guibutton == backButton) selectData(index - 1);
//        else if (guibutton == nextButton) selectData(index + 1);
//        else if (guibutton == closeButton) mc.displayScreen(null);
//    }
//
//    @Override
//    public void drawScreen(int sizeX, int sizeY, float offset) {
//        drawDefaultBackground();
//        drawDummy();
//        drawCenteredString(fontRenderer, MCA.localize("gui.title.staffoflife"), width / 2, height / 2 - 110, 0xffffff);
//        super.drawScreen(sizeX, sizeY, offset);
//    }
//
//    public void setVillagerData(Map<String, CNBT> data) {
//        villagerData = data;
//
//        if (data.size() > 0) {
//            dummy = new EntityVillagerMCA(player.world);
//            keys.addAll(data.keySet());
//            selectData(0);
//        } else {
//            nameButton.displayString = "No villagers found.";
//            backButton.enabled = false;
//            nextButton.enabled = false;
//            nameButton.enabled = false;
//            reviveButton.enabled = false;
//        }
//    }
//
//    private void updateDummy(CNBT nbt) {
//        dummy.readEntityFromNBT(nbt);
//        dummy.setHealth(20.0F);
//    }
//
//    private void selectData(int i) {
//        if (i < 0) i = keys.size() - 1;
//        else if (i > keys.size() - 1) i = 0;
//
//        index = i;
//        updateDummy(villagerData.get(keys.get(index)));
//        nameButton.displayString = dummy.getDisplayName().getUnformattedText();
//    }
//
//    private void drawDummy() {
//        int posX = width / 2;
//        int posY = height / 2 + 45;
//
//        if (dummy != null) GuiInventory.drawEntityOnScreen(posX, posY, 60, 0, 0, dummy);
//    }
}