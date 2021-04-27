package mca.client.gui;

import cobalt.minecraft.entity.player.CPlayer;
import mca.api.API;
import mca.core.MCA;
import mca.items.ItemBaby;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class GuiNameBaby extends Screen {
    private final CPlayer player;

    private TextFieldWidget babyNameTextField;
    private Button doneButton;
    private Button randomButton;
    private ItemBaby baby;

    public GuiNameBaby(CPlayer player, ItemStack babyStack) {
        super(new StringTextComponent("Name Baby"));
        this.player = player;

        if (babyStack.getItem() instanceof ItemBaby) this.baby = (ItemBaby) babyStack.getItem();
    }

//    @Override
//    public void updateScreen() {
//        super.updateScreen();
//
//        if (babyNameTextField != null) {
//            babyNameTextField.updateCursorCounter();
//            doneButton.enabled = !babyNameTextField.getText().isEmpty();
//        }
//    }
//
//    @Override
//    public void initGui() {
//        Keyboard.enableRepeatEvents(true);
//
//        buttonList.clear();
//        buttonList.add(doneButton = new Button(1, width / 2 - 40, height / 2 - 10, 80, 20, MCA.localize("gui.button.done")));
//        buttonList.add(randomButton = new Button(2, width / 2 + 105, height / 2 - 60, 60, 20, MCA.localize("gui.button.random")));
//        babyNameTextField = new TextFieldWidget(3, fontRenderer, width / 2 - 100, height / 2 - 60, 200, 20);
//        babyNameTextField.setMaxStringLength(32);
//
//        if (this.baby == null) this.mc.displayScreen(null);
//    }
//
//    @Override
//    public void onGuiClosed() {
//        Keyboard.enableRepeatEvents(false);
//    }
//
//    @Override
//    public boolean doesGuiPauseGame() {
//        return false;
//    }
//
//    @Override
//    protected void actionPerformed(Button button) {
//        if (button == doneButton) {
//            NetMCA.INSTANCE.sendToServer(new NetMCA.BabyName(babyNameTextField.getText().trim()));
//            mc.displayScreen(null);
//        } else if (button == randomButton) {
//            babyNameTextField.setText(API.getRandomName(baby.getGender()));
//        }
//    }
//
//    @Override
//    protected void keyTyped(char c, int i) {
//        babyNameTextField.textboxKeyTyped(c, i);
//    }
//
//    @Override
//    protected void mouseClicked(int clickX, int clickY, int clicked) throws IOException {
//        super.mouseClicked(clickX, clickY, clicked);
//        babyNameTextField.mouseClicked(clickX, clickY, clicked);
//    }
//
//    @Override
//    public void drawScreen(int sizeX, int sizeY, float offset) {
//        drawDefaultBackground();
//        drawString(fontRenderer, MCA.localize("gui.title.namebaby"), width / 2 - 100, height / 2 - 70, 0xa0a0a0);
//        babyNameTextField.drawTextBox();
//        super.drawScreen(sizeX, sizeY, offset);
//    }
}