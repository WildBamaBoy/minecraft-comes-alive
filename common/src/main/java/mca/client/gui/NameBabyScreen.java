package mca.client.gui;

import mca.cobalt.network.NetworkHandler;
import mca.item.BabyItem;
import mca.network.BabyNameRequest;
import mca.network.BabyNamingVillagerMessage;
import mca.resources.API;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import java.util.Objects;

public class NameBabyScreen extends Screen {
    private final ItemStack baby;
    private final PlayerEntity player;
    private TextFieldWidget babyNameTextField;

    public NameBabyScreen(PlayerEntity player, ItemStack baby) {
        super(new LiteralText("Name Baby"));
        this.baby = baby;
        this.player = player;
    }

    @Override
    public void tick() {
        super.tick();

        babyNameTextField.tick();
    }

    @Override
    public void init() {
        addButton(new ButtonWidget(width / 2 - 40, height / 2 - 10, 80, 20, new TranslatableText("gui.button.done"), (b) -> {
            NetworkHandler.sendToServer(new BabyNamingVillagerMessage(player.inventory.selectedSlot, babyNameTextField.getText().trim()));
            Objects.requireNonNull(this.client).openScreen(null);
        }));
        addButton(new ButtonWidget(width / 2 + 105, height / 2 - 60, 60, 20, new TranslatableText("gui.button.random"), (b) -> {
            NetworkHandler.sendToServer(new BabyNameRequest(((BabyItem)baby.getItem()).getGender()));
        }));

        babyNameTextField = new TextFieldWidget(this.textRenderer, width / 2 - 100, height / 2 - 60, 200, 20, new TranslatableText("structure_block.structure_name"));
        babyNameTextField.setMaxLength(32);

        setInitialFocus(babyNameTextField);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack transform, int w, int h, float scale) {
        renderBackground(transform);

        setFocused(babyNameTextField);

        drawCenteredText(transform, this.textRenderer, this.title, this.width / 2, 10, 16777215);

        babyNameTextField.render(transform, width / 2 - 100, height / 2 - 70, scale);

        super.render(transform, w, h, scale);
    }

    public void setBabyName(String name) {
        babyNameTextField.setText(name);
    }
}
