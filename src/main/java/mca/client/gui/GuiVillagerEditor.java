package mca.client.gui;

import mca.api.API;
import mca.api.types.APIButton;
import mca.client.gui.component.ButtonEx;
import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import cobalt.minecraft.entity.player.CPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class GuiVillagerEditor extends Screen {
    private final EntityVillagerMCA villager;
    private final CPlayer player;

    private TextFieldWidget nameTextField;
    private TextFieldWidget professionTextField;
    private TextFieldWidget textureTextField;

    public GuiVillagerEditor(EntityVillagerMCA EntityHuman, CPlayer player) {
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

        nameTextField = new TextFieldWidget(1, fontRenderer, width / 2 - 205, height / 2 - 95, 150, 20);
        nameTextField.setMaxStringLength(32);
        nameTextField.setText(villager.get(EntityVillagerMCA.villagerName));
        professionTextField = new TextFieldWidget(2, fontRenderer, width / 2 - 190, height / 2 + 10, 250, 20);
        professionTextField.setMaxStringLength(64);
        professionTextField.setText(villager.getVanillaCareer().getName());
        textureTextField = new TextFieldWidget(3, fontRenderer, width / 2 - 190, height / 2 - 15, 250, 20);
        textureTextField.setMaxStringLength(128);
        textureTextField.setText(villager.get(EntityVillagerMCA.clothes));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(Button guiButton) {
        APIButton btn = ((ButtonEx) guiButton).getApiButton();
        if (btn.isNotifyServer()) {
            NetMCA.INSTANCE.sendToServer(new NetMCA.ButtonAction("editor", btn.getIdentifier(), villager.getUUID()));
        } else if (btn.getIdentifier().equals("gui.button.done")) {
            mc.displayScreen(null);
        } else if (btn.getIdentifier().equals("gui.button.copyuuid")) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(villager.getUUID().toString()), null);
            Minecraft.getMinecraft().player.sendChatMessage("Villager UUID copied to clipboard.");
        } else if (btn.getIdentifier().equals("gui.button.profession.set")) {
            String profession = professionTextField.getText();
            NetMCA.INSTANCE.sendToServer(new NetMCA.SetProfession(villager.getUUID(), profession));
            new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            NetMCA.INSTANCE.sendToServer(new NetMCA.CareerRequest(villager.getUUID()));
                        }
                    },500
            );
        } else if (btn.getIdentifier().contains("gui.button.texture")) {
            String texture = btn.getIdentifier().endsWith(".set") ? textureTextField.getText() : API.getRandomClothing(villager);
            NetMCA.INSTANCE.sendToServer(new NetMCA.SetTexture(villager.getUUID(), texture));
            textureTextField.setText(texture);
        }
    }

    @Override
    protected void keyTyped(char c, int i) {
        if (i == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().displayScreen(null);
        } else {
            if (nameTextField.textboxKeyTyped(c, i)) {
                String text = nameTextField.getText().trim();
                NetMCA.INSTANCE.sendToServer(new NetMCA.SetName(text, villager.getUUID()));
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
        //TODO editor currently out of service
    }

    @Override
    public void drawScreen(int sizeX, int sizeY, float offset) {
        drawGradientRect(0, 0, width, height, -1072689136, -804253680);
        drawString(fontRenderer, "Name:", width / 2 - 205, height / 2 - 110, 0xffffff);
        drawCenteredString(fontRenderer, MCA.localize("gui.title.editor"), width / 2, height / 2 - 110, 0xffffff);
        nameTextField.drawTextBox();
        professionTextField.drawTextBox();
        textureTextField.drawTextBox();
        super.drawScreen(sizeX, sizeY, offset);
    }
}