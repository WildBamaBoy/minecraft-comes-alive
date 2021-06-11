package mca.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.network.NetworkHandler;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.network.ReviveVillagerMessage;
import mca.network.SavedVillagersRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class GuiStaffOfLife extends Screen {
    private final List<String> keys = new ArrayList<>();
    private Map<String, CNBT> villagerData;
    private VillagerEntityMCA dummy;
    // selection fields
    private int selectedIndex = 0;
    private Button nameButton;
    private Button reviveButton;
    private Button nextButton;
    private Button backButton;

    public GuiStaffOfLife() {
        super(new StringTextComponent("Staff of Life"));
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new SavedVillagersRequest());

        backButton = addButton(new Button(width / 2 - 123, height / 2 + 65, 20, 20, new StringTextComponent("<<"),
                (button) -> selectData(selectedIndex - 1)));

        nextButton = addButton(new Button(width / 2 + 103, height / 2 + 65, 20, 20, new StringTextComponent(">>"),
                (button) -> selectData(selectedIndex + 1)));

        nameButton = addButton(new Button(width / 2 - 100, height / 2 + 65, 200, 20, new StringTextComponent(""),
                (button) -> {
                }));

        reviveButton = addButton(new Button(width / 2 - 100, height / 2 + 90, 60, 20, MCA.localizeText("gui.button.revive"),
                (button) -> {
                    NetworkHandler.sendToServer(new ReviveVillagerMessage(UUID.fromString(keys.get(selectedIndex))));
                    Objects.requireNonNull(this.minecraft).setScreen(null);
                }));

        addButton(new Button(width / 2 + 40, height / 2 + 90, 60, 20, MCA.localizeText("gui.button.exit"),
                (button) -> Objects.requireNonNull(this.minecraft).setScreen(null)));

        toggleButtons(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(MatrixStack transform, int w, int h, float scale) {
        renderBackground(transform);

        drawCenteredString(transform, font, MCA.localize("gui.title.staffoflife"), width / 2, height / 2 - 110, 0xffffff);

        super.render(transform, w, h, scale);

        drawDummy();
    }

    public void setVillagerData(Map<String, CNBT> data) {
        villagerData = data;

        if (data.size() > 0) {
            dummy = new VillagerEntityMCA(Minecraft.getInstance().level);
            keys.clear();
            keys.addAll(data.keySet());
            selectData(0);
            toggleButtons(true);
        } else {
            nameButton.setMessage(new StringTextComponent("No villagers found."));
            toggleButtons(false);
        }
    }

    private void toggleButtons(boolean enabled) {
        backButton.active = enabled;
        nextButton.active = enabled;
        nameButton.active = enabled;
        reviveButton.active = enabled;
    }

    private void updateDummy(CNBT nbt) {
        dummy.readAdditionalSaveData(nbt.getMcCompound());
    }

    private void selectData(int i) {
        if (i < 0) i = keys.size() - 1;
        else if (i > keys.size() - 1) i = 0;

        selectedIndex = i;
        updateDummy(villagerData.get(keys.get(selectedIndex)));
        nameButton.setMessage(dummy.getDisplayName());
    }

    private void drawDummy() {
        int posX = width / 2;
        int posY = height / 2 + 45;

        if (dummy != null) InventoryScreen.renderEntityInInventory(posX, posY, 60, 0, 0, dummy);
    }
}