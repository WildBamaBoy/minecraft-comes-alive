package mca.client.gui;

import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.NetworkHandler;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.network.ReviveVillagerMessage;
import mca.network.SavedVillagersRequest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import java.util.*;

public class GuiStaffOfLife extends Screen {
    private final List<String> keys = new ArrayList<>();
    private Map<String, CNBT> villagerData;
    private VillagerEntityMCA dummy;
    // selection fields
    private int selectedIndex = 0;
    private ButtonWidget nameButton;
    private ButtonWidget reviveButton;
    private ButtonWidget nextButton;
    private ButtonWidget backButton;

    public GuiStaffOfLife() {
        super(new LiteralText("Staff of Life"));
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new SavedVillagersRequest());

        backButton = addButton(new ButtonWidget(width / 2 - 123, height / 2 + 65, 20, 20, new LiteralText("<<"),
                (button) -> selectData(selectedIndex - 1)));

        nextButton = addButton(new ButtonWidget(width / 2 + 103, height / 2 + 65, 20, 20, new LiteralText(">>"),
                (button) -> selectData(selectedIndex + 1)));

        nameButton = addButton(new ButtonWidget(width / 2 - 100, height / 2 + 65, 200, 20, new LiteralText(""),
                (button) -> {
                }));

        reviveButton = addButton(new ButtonWidget(width / 2 - 100, height / 2 + 90, 60, 20, MCA.localizeText("gui.button.revive"),
                (button) -> {
                    NetworkHandler.sendToServer(new ReviveVillagerMessage(UUID.fromString(keys.get(selectedIndex))));
                    Objects.requireNonNull(this.client).openScreen(null);
                }));

        addButton(new ButtonWidget(width / 2 + 40, height / 2 + 90, 60, 20, MCA.localizeText("gui.button.exit"),
                (button) -> Objects.requireNonNull(this.client).openScreen(null)));

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

        drawCenteredString(transform, textRenderer, MCA.localize("gui.title.staffoflife"), width / 2, height / 2 - 110, 0xffffff);

        super.render(transform, w, h, scale);

        drawDummy();
    }

    public void setVillagerData(Map<String, CNBT> data) {
        villagerData = data;

        if (data.size() > 0) {
            dummy = new VillagerEntityMCA(MinecraftClient.getInstance().world);
            keys.clear();
            keys.addAll(data.keySet());
            selectData(0);
            toggleButtons(true);
        } else {
            nameButton.setMessage(new LiteralText("No villagers found."));
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
        dummy.readCustomDataFromNbt(nbt.getMcCompound());
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

        if (dummy != null) InventoryScreen.drawEntity(posX, posY, 60, 0, 0, dummy);
    }
}