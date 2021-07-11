package mca.client.gui;

import mca.cobalt.localizer.Localizer;
import mca.cobalt.minecraft.nbt.CNBT;
import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.network.CallToPlayerMessage;
import mca.network.GetVillagerRequest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import java.util.*;

import org.jetbrains.annotations.NotNull;

public class GuiWhistle extends Screen {
    private final List<String> keys = new ArrayList<>();
    private VillagerEntityMCA dummy;
    private Map<String, CNBT> villagerData;
    private ButtonWidget selectionLeftButton;
    private ButtonWidget selectionRightButton;
    private ButtonWidget villagerNameButton;
    private ButtonWidget callButton;
    private int loadingAnimationTicks;
    private int selectedIndex;

    public GuiWhistle() {
        super(new LiteralText("Whistle"));
    }

    @Override
    public void tick() {
        super.tick();

        if (loadingAnimationTicks != -1) {
            loadingAnimationTicks++;
        }

        if (loadingAnimationTicks >= 20) {
            loadingAnimationTicks = 0;
        }
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new GetVillagerRequest());

        selectionLeftButton = addSelectableChild(new ButtonWidget(width / 2 - 123, height / 2 + 65, 20, 20, new LiteralText("<<"), b -> {
            if (selectedIndex == 0) {
                selectedIndex = keys.size() - 1;
            } else {
                selectedIndex--;
            }
            setVillagerData(selectedIndex);
        }));
        selectionRightButton = addSelectableChild(new ButtonWidget(width / 2 + 103, height / 2 + 65, 20, 20, new LiteralText(">>"), b -> {
            if (selectedIndex == keys.size() - 1) {
                selectedIndex = 0;
            } else {
                selectedIndex++;
            }
            setVillagerData(selectedIndex);
        }));
        villagerNameButton = addSelectableChild(new ButtonWidget(width / 2 - 100, height / 2 + 65, 200, 20, new LiteralText(""), b -> {}));

        callButton = addSelectableChild(new ButtonWidget(width / 2 - 100, height / 2 + 90, 60, 20, new LiteralText(Localizer.getInstance().localize("gui.button.call")), (b) -> {
            NetworkHandler.sendToServer(new CallToPlayerMessage(UUID.fromString(keys.get(selectedIndex))));
            Objects.requireNonNull(this.client).openScreen(null);
        }));

        addSelectableChild(new ButtonWidget(width / 2 + 40, height / 2 + 90, 60, 20, new LiteralText(Localizer.getInstance().localize("gui.button.exit")), b -> Objects.requireNonNull(this.client).openScreen(null)));

        toggleButtons(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack transform, int sizeX, int sizeY, float offset) {
        renderBackground(transform);

        drawCenteredText(transform, textRenderer, Localizer.getInstance().localize("gui.title.whistle"), width / 2, height / 2 - 110, 0xffffff);

        if (loadingAnimationTicks != -1) {
            String loadingMsg = "Loading" + new String(new char[loadingAnimationTicks % 10]).replace("\0", ".");
            drawStringWithShadow(transform, textRenderer, loadingMsg, width / 2 - 20, height / 2 - 10, 0xffffff);
        } else {
            if (keys.size() == 0) {
                drawCenteredText(transform, textRenderer, "No family members could be found in the area.", width / 2, height / 2 + 50, 0xffffff);
            } else {
                drawCenteredText(transform, textRenderer, (selectedIndex + 1) + " / " + keys.size(), width / 2, height / 2 + 50, 0xffffff);
            }
        }

        drawDummy();

        super.render(transform, sizeX, sizeY, offset);
    }

    private void drawDummy() {
        final int posX = width / 2;
        int posY = height / 2 + 45;
        if (dummy != null) InventoryScreen.drawEntity(posX, posY, 60, 0, 0, dummy);
    }

    public void setVillagerData(@NotNull Map<String, CNBT> data) {
        villagerData = data;
        keys.clear();
        keys.addAll(data.keySet());
        loadingAnimationTicks = -1;
        selectedIndex = 0;

        setVillagerData(0);
    }

    private void setVillagerData(int index) {
        if (keys.size() > 0) {
            CNBT firstData = villagerData.get(keys.get(index));

            dummy = new VillagerEntityMCA(MinecraftClient.getInstance().world);
            dummy.readCustomDataFromNbt(firstData.getMcCompound());

            villagerNameButton.setMessage(dummy.getDisplayName());

            toggleButtons(true);
        } else {
            toggleButtons(false);
        }
    }

    private void toggleButtons(boolean enabled) {
        selectionLeftButton.active = enabled;
        selectionRightButton.active = enabled;
        callButton.active = enabled;
    }
}