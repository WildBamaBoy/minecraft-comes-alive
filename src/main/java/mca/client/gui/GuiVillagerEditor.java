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
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiVillagerEditor extends GuiScreen {
    private final EntityVillagerMCA villager;
    private final EntityPlayer player;

    private GuiTextField nameTextField;
    private GuiTextField professionTextField;
    private GuiTextField textureTextField;

    public GuiVillagerEditor(EntityVillagerMCA EntityHuman, EntityPlayer player) {
        super();
        this.player = player;
        villager = EntityHuman;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        nameTextField.updateCursorCounter();
        professionTextField.updateCursorCounter();
        textureTextField.updateCursorCounter();
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        drawEditorGui();

        nameTextField = new GuiTextField(1, fontRenderer, width / 2 - 205, height / 2 - 95, 150, 20);
        nameTextField.setMaxStringLength(32);
        nameTextField.setText(villager.get(EntityVillagerMCA.VILLAGER_NAME));
        professionTextField = new GuiTextField(2, fontRenderer, width / 2 - 190, height / 2 + 10, 250, 20);
        professionTextField.setMaxStringLength(64);
        professionTextField.setText(villager.getVanillaCareer().getName());
        textureTextField = new GuiTextField(3, fontRenderer, width / 2 - 190, height / 2 - 15, 250, 20);
        textureTextField.setMaxStringLength(128);
        textureTextField.setText(villager.get(EntityVillagerMCA.TEXTURE));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        APIButton btn = ((GuiButtonEx) guiButton).getApiButton();
        if (btn.isNotifyServer()) {
            NetMCA.INSTANCE.sendToServer(new NetMCA.ButtonAction("editor", btn.getIdentifier(), villager.getUniqueID()));
        } else if (btn.getIdentifier().equals("gui.button.done")) {
            mc.displayGuiScreen(null);
        } else if (btn.getIdentifier().equals("gui.button.copyuuid")) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(villager.getUniqueID().toString()), null);
            Minecraft.getMinecraft().player.sendChatMessage("Villager UUID copied to clipboard.");
        } else if (btn.getIdentifier().equals("gui.button.profession.set")) {
            String profession = professionTextField.getText();
            NetMCA.INSTANCE.sendToServer(new NetMCA.SetProfession(villager.getUniqueID(), profession));
            new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            NetMCA.INSTANCE.sendToServer(new NetMCA.CareerRequest(villager.getUniqueID()));
                        }
                    },500
            );
        } else if (btn.getIdentifier().contains("gui.button.texture")) {
            String texture = btn.getIdentifier().endsWith(".set") ? textureTextField.getText() : API.getRandomSkin(villager);
            NetMCA.INSTANCE.sendToServer(new NetMCA.SetTexture(villager.getUniqueID(), texture));
            textureTextField.setText(texture);
        }
    }

    @Override
    protected void keyTyped(char c, int i) {
        if (i == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        } else {
            if (nameTextField.textboxKeyTyped(c, i)) {
                String text = nameTextField.getText().trim();
                NetMCA.INSTANCE.sendToServer(new NetMCA.SetName(text, villager.getUniqueID()));
            }
            textureTextField.textboxKeyTyped(c, i);
            professionTextField.textboxKeyTyped(c, i);
            drawEditorGui();
        }
    }

    @Override
    protected void mouseClicked(int clickX, int clickY, int clicked) throws IOException {
        super.mouseClicked(clickX, clickY, clicked);
        nameTextField.mouseClicked(clickX, clickY, clicked);
        professionTextField.mouseClicked(clickX, clickY, clicked);
        textureTextField.mouseClicked(clickX, clickY, clicked);
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
        professionTextField.drawTextBox();
        textureTextField.drawTextBox();
        super.drawScreen(sizeX, sizeY, offset);
    }
}