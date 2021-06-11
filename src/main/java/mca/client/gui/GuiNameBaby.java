package mca.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mca.api.API;
import mca.api.cobalt.network.NetworkHandler;
import mca.core.MCA;
import mca.items.BabyItem;
import mca.network.BabyNamingVillagerMessage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class GuiNameBaby extends Screen {
    private final ItemStack baby;
    private final PlayerEntity player;
    private TextFieldWidget babyNameTextField;

    public GuiNameBaby(PlayerEntity player, ItemStack baby) {
        super(new StringTextComponent("Name Baby"));
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
        addButton(new Button(width / 2 - 40, height / 2 - 10, 80, 20, MCA.localizeText("gui.button.done"), (b) -> {
            NetworkHandler.sendToServer(new BabyNamingVillagerMessage(player.inventory.selected, babyNameTextField.getValue().trim()));
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));
        addButton(new Button(width / 2 + 105, height / 2 - 60, 60, 20, MCA.localizeText("gui.button.random"), (b) -> babyNameTextField.setValue(API.getRandomName(((BabyItem) baby.getItem()).getGender()))));

        babyNameTextField = new TextFieldWidget(this.font, width / 2 - 100, height / 2 - 60, 200, 20, new TranslationTextComponent("structure_block.structure_name"));
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

        drawCenteredString(transform, this.font, this.title, this.width / 2, 10, 16777215);

        babyNameTextField.render(transform, width / 2 - 100, height / 2 - 70, scale);

        super.render(transform, w, h, scale);
    }
}