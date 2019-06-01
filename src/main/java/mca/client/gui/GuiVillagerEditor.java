package mca.client.gui;

import mca.api.API;
import mca.api.types.APIButton;
import mca.client.gui.component.GuiButtonEx;
import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiVillagerEditor extends GuiScreen {
    private final EntityVillagerMCA villager;
    private final EntityPlayer player;

    private GuiTextField nameTextField;
    private GuiTextField dummyTextField;

    private GuiButton doneButton;
    private GuiButton copyUUIDButton;

    private int currentPage = 1;

    public GuiVillagerEditor(EntityVillagerMCA EntityHuman, EntityPlayer player) {
        super();
        this.player = player;
        villager = EntityHuman;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (nameTextField != null && doneButton != null) {
            nameTextField.updateCursorCounter();
            if (nameTextField.getText().isEmpty()) {
                doneButton.enabled = false;
            } else {
                doneButton.enabled = true;
            }
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        drawEditorGui();

        nameTextField = new GuiTextField(1, fontRenderer, width / 2 - 205, height / 2 - 95, 150, 20);
        nameTextField.setMaxStringLength(32);
        nameTextField.setText(villager.get(EntityVillagerMCA.VILLAGER_NAME));
        dummyTextField = new GuiTextField(2, fontRenderer, width / 2 + 90, height / 2 - 100, 100, 200);
        dummyTextField.setMaxStringLength(0);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        APIButton btn = ((GuiButtonEx) guiButton).getApiButton();
        if (btn.getNotifyServer()) {
            NetMCA.INSTANCE.sendToServer(new NetMCA.ButtonAction(btn.getLangId(), villager.getUniqueID()));
        } else if (btn.getLangId().equals("gui.button.done")) {
            mc.displayGuiScreen(null);
        } else if (btn.getLangId().equals("gui.button.copyuuid")) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(villager.getUniqueID().toString()), null);
        }
    }

    @Override
    protected void keyTyped(char c, int i) throws IOException {
        if (i == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        } else {
            nameTextField.textboxKeyTyped(c, i);
            String text = nameTextField.getText().trim();
            NetMCA.INSTANCE.sendToServer(new NetMCA.SetName(villager.getUniqueID(), text));
            drawEditorGui();
        }
    }

    @Override
    protected void mouseClicked(int clickX, int clickY, int clicked) throws IOException {
        super.mouseClicked(clickX, clickY, clicked);
        nameTextField.mouseClicked(clickX, clickY, clicked);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void drawEditorGui() {
        buttonList.clear();
        API.addButtons("editor", villager, player, this);
    }

    @Override
    public void drawScreen(int sizeX, int sizeY, float offset) {
        drawGradientRect(0, 0, width, height, -1072689136, -804253680);
        drawString(fontRenderer, "Name:", width / 2 - 205, height / 2 - 110, 0xffffff);
        drawCenteredString(fontRenderer, MCA.getLocalizer().localize("gui.title.editor"), width / 2, height / 2 - 110, 0xffffff);
        nameTextField.drawTextBox();

        super.drawScreen(sizeX, sizeY, offset);
    }
}