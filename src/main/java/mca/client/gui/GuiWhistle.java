package mca.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.NonNull;
import mca.api.cobalt.minecraft.nbt.CNBT;
import mca.api.cobalt.network.NetworkHandler;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.network.CallToPlayerMessage;
import mca.network.GetVillagerRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class GuiWhistle extends Screen {
    private final List<String> keys = new ArrayList<>();
    private VillagerEntityMCA dummy;
    private Map<String, CNBT> villagerData;
    private Button selectionLeftButton;
    private Button selectionRightButton;
    private Button villagerNameButton;
    private Button callButton;
    private int loadingAnimationTicks;
    private int selectedIndex;

    public GuiWhistle() {
        super(new StringTextComponent("Whistle"));
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

        selectionLeftButton = addButton(new Button(width / 2 - 123, height / 2 + 65, 20, 20, new StringTextComponent("<<"), (b) -> {
            if (selectedIndex == 0) {
                selectedIndex = keys.size() - 1;
            } else {
                selectedIndex--;
            }
            setVillagerData(selectedIndex);
        }));

        selectionRightButton = addButton(new Button(width / 2 + 103, height / 2 + 65, 20, 20, new StringTextComponent(">>"), (b) -> {
            if (selectedIndex == keys.size() - 1) {
                selectedIndex = 0;
            } else {
                selectedIndex++;
            }
            setVillagerData(selectedIndex);
        }));

        villagerNameButton = addButton(new Button(width / 2 - 100, height / 2 + 65, 200, 20, new StringTextComponent(""), (b) -> {
        }));

        callButton = addButton(new Button(width / 2 - 100, height / 2 + 90, 60, 20, new StringTextComponent(MCA.localize("gui.button.call")), (b) -> {
            NetworkHandler.sendToServer(new CallToPlayerMessage(UUID.fromString(keys.get(selectedIndex))));
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));

        addButton(new Button(width / 2 + 40, height / 2 + 90, 60, 20, new StringTextComponent(MCA.localize("gui.button.exit")), (b) -> Objects.requireNonNull(this.minecraft).setScreen(null)));

        toggleButtons(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack transform, int sizeX, int sizeY, float offset) {
        renderBackground(transform);

        drawCenteredString(transform, font, MCA.localize("gui.title.whistle"), width / 2, height / 2 - 110, 0xffffff);

        if (loadingAnimationTicks != -1) {
            String loadingMsg = "Loading" + new String(new char[loadingAnimationTicks % 10]).replace("\0", ".");
            drawString(transform, font, loadingMsg, width / 2 - 20, height / 2 - 10, 0xffffff);
        } else {
            if (keys.size() == 0) {
                drawCenteredString(transform, font, "No family members could be found in the area.", width / 2, height / 2 + 50, 0xffffff);
            } else {
                drawCenteredString(transform, font, (selectedIndex + 1) + " / " + keys.size(), width / 2, height / 2 + 50, 0xffffff);
            }
        }

        drawDummy();

        super.render(transform, sizeX, sizeY, offset);
    }

    private void drawDummy() {
        final int posX = width / 2;
        int posY = height / 2 + 45;
        if (dummy != null) InventoryScreen.renderEntityInInventory(posX, posY, 60, 0, 0, dummy);
    }

    public void setVillagerData(@NonNull Map<String, CNBT> data) {
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

            dummy = new VillagerEntityMCA(Minecraft.getInstance().level);
            dummy.readAdditionalSaveData(firstData.getMcCompound());

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