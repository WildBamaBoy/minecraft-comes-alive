package mca.client.gui;

import mca.cobalt.network.NetworkHandler;
import mca.entity.EntitiesMCA;
import mca.entity.VillagerEntityMCA;
import mca.network.CallToPlayerMessage;
import mca.network.GetFamilyRequest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.*;

import org.jetbrains.annotations.NotNull;

public class WhistleScreen extends Screen {
    private List<String> keys = new ArrayList<>();
    private NbtCompound villagerData = new NbtCompound();

    private VillagerEntityMCA dummy;

    private ButtonWidget selectionLeftButton;
    private ButtonWidget selectionRightButton;
    private ButtonWidget villagerNameButton;
    private ButtonWidget callButton;
    private int loadingAnimationTicks;
    private int selectedIndex;

    public WhistleScreen() {
        super(new TranslatableText("gui.whistle.title"));
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
        NetworkHandler.sendToServer(new GetFamilyRequest());

        selectionLeftButton = addButton(new ButtonWidget(width / 2 - 123, height / 2 + 65, 20, 20, new LiteralText("<<"), b -> {
            if (selectedIndex == 0) {
                selectedIndex = keys.size() - 1;
            } else {
                selectedIndex--;
            }
            setVillagerData(selectedIndex);
        }));
        selectionRightButton = addButton(new ButtonWidget(width / 2 + 103, height / 2 + 65, 20, 20, new LiteralText(">>"), b -> {
            if (selectedIndex == keys.size() - 1) {
                selectedIndex = 0;
            } else {
                selectedIndex++;
            }
            setVillagerData(selectedIndex);
        }));
        villagerNameButton = addButton(new ButtonWidget(width / 2 - 100, height / 2 + 65, 200, 20, new LiteralText(""), b -> {
        }));

        callButton = addButton(new ButtonWidget(width / 2 - 100, height / 2 + 90, 60, 20, new TranslatableText("gui.button.call"), (b) -> {
            NetworkHandler.sendToServer(new CallToPlayerMessage(UUID.fromString(keys.get(selectedIndex))));
            Objects.requireNonNull(this.client).openScreen(null);
        }));

        addButton(new ButtonWidget(width / 2 + 40, height / 2 + 90, 60, 20, new TranslatableText("gui.button.exit"), b -> Objects.requireNonNull(this.client).openScreen(null)));

        toggleButtons(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack transform, int sizeX, int sizeY, float offset) {
        renderBackground(transform);

        drawCenteredText(transform, textRenderer, new TranslatableText("gui.whistle.title"), width / 2, height / 2 - 100, 0xffffff);

        if (loadingAnimationTicks != -1) {
            String loadingMsg = new String(new char[(loadingAnimationTicks / 5) % 4]).replace("\0", ".");
            drawTextWithShadow(transform, textRenderer, new TranslatableText("gui.loading").append(new LiteralText(loadingMsg)), width / 2 - 20, height / 2 - 10, 0xffffff);
        } else {
            if (keys.size() == 0) {
                drawCenteredText(transform, textRenderer, new TranslatableText("gui.whistle.noFamily"), width / 2, height / 2 + 50, 0xffffff);
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
        if (dummy != null) {
            InventoryScreen.drawEntity(posX, posY, 60, 0, 0, dummy);
        }
    }

    public void setVillagerData(@NotNull NbtCompound data) {
        villagerData = data;
        keys = new ArrayList<>(data.getKeys());
        loadingAnimationTicks = -1;
        selectedIndex = 0;

        setVillagerData(0);
    }

    private void setVillagerData(int index) {
        if (keys.size() > 0) {
            NbtCompound firstData = villagerData.getCompound(keys.get(index));

            dummy = EntitiesMCA.MALE_VILLAGER.create(MinecraftClient.getInstance().world);
            dummy.readCustomDataFromNbt(firstData);

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
