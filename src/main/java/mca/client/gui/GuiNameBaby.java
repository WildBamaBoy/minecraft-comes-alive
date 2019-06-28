package mca.client.gui;

import mca.api.API;
import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.items.ItemBaby;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiNameBaby extends GuiScreen {
    private final EntityPlayer player;

    private GuiTextField babyNameTextField;
    private GuiButton doneButton;
    private GuiButton randomButton;
    private ItemBaby baby;

    public GuiNameBaby(EntityPlayer player, ItemStack babyStack) {
        super();
        this.player = player;

        if (babyStack.getItem() instanceof ItemBaby) this.baby = (ItemBaby) babyStack.getItem();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (babyNameTextField != null) {
            babyNameTextField.updateCursorCounter();
            doneButton.enabled = !babyNameTextField.getText().isEmpty();
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        buttonList.clear();
        buttonList.add(doneButton = new GuiButton(1, width / 2 - 40, height / 2 - 10, 80, 20, MCA.getLocalizer().localize("gui.button.done")));
        buttonList.add(randomButton = new GuiButton(2, width / 2 + 105, height / 2 - 60, 60, 20, MCA.getLocalizer().localize("gui.button.random")));
        babyNameTextField = new GuiTextField(3, fontRenderer, width / 2 - 100, height / 2 - 60, 200, 20);
        babyNameTextField.setMaxStringLength(32);

        if (this.baby == null) this.mc.displayGuiScreen(null);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == doneButton) {
            NetMCA.INSTANCE.sendToServer(new NetMCA.BabyName(babyNameTextField.getText().trim()));
            mc.displayGuiScreen(null);
        } else if (button == randomButton) {
            babyNameTextField.setText(API.getRandomName(baby.getGender()));
        }
    }

    @Override
    protected void keyTyped(char c, int i) {
        babyNameTextField.textboxKeyTyped(c, i);
    }

    @Override
    protected void mouseClicked(int clickX, int clickY, int clicked) throws IOException {
        super.mouseClicked(clickX, clickY, clicked);
        babyNameTextField.mouseClicked(clickX, clickY, clicked);
    }

    @Override
    public void drawScreen(int sizeX, int sizeY, float offset) {
        drawDefaultBackground();
        drawString(fontRenderer, MCA.getLocalizer().localize("gui.title.namebaby"), width / 2 - 100, height / 2 - 70, 0xa0a0a0);
        babyNameTextField.drawTextBox();
        super.drawScreen(sizeX, sizeY, offset);
    }
}