package mca.client.gui;

import mca.api.API;
import mca.client.gui.component.GuiButtonEx;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.ParentData;
import mca.entity.data.PlayerHistory;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMoveState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Optional;

@SideOnly(Side.CLIENT)
public class GuiInteract extends GuiScreen {
    private static final ResourceLocation ICON_TEXTURES = new ResourceLocation("mca:textures/gui.png");
    private static boolean displaySuccessChance;
    private final EntityVillagerMCA villager;
    private final EntityPlayer player;

    private boolean inGiftMode;

    private int timeSinceLastClick;

    private int marriedIconU = 0;
    private int engagedIconU = 64;
    private int notMarriedIconU = 16;
    private int parentsIconU = 32;
    private int giftIconU = 48;
    private int redHeartIconU = 80;
    private int blackHeartIconU = 96;
    private int goldHeartIconU = 112;

    private int mouseX;
    private int mouseY;

    // Tracks which page we're on in the GUI for sending button events
    private String activeKey;

    public GuiInteract(EntityVillagerMCA villager, EntityPlayer player) {
        super();
        this.villager = villager;
        this.player = player;
        this.activeKey = "main";
    }

    @Override
    public void initGui() {
        drawMainButtonMenu();
    }

    @Override
    public void onGuiClosed() {
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        if (timeSinceLastClick < 100) {
            timeSinceLastClick++;
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);
        drawIcons();
        drawTextPopups();

        mouseX = Mouse.getEventX() * width / mc.displayWidth;
        mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (Mouse.getEventDWheel() < 0) {
            player.inventory.currentItem = player.inventory.currentItem == 8 ? 0 : player.inventory.currentItem + 1;
        } else if (Mouse.getEventDWheel() > 0) {
            player.inventory.currentItem = player.inventory.currentItem == 0 ? 8 : player.inventory.currentItem - 1;
        }
    }

    @Override
    protected void mouseClicked(int posX, int posY, int button) throws IOException {
        super.mouseClicked(posX, posY, button);

        // Right mouse button
        if (inGiftMode && button == 1) NetMCA.INSTANCE.sendToServer(new NetMCA.ButtonAction(activeKey, "gui.button.gift", villager.getUniqueID()));
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        // Hotkey to leave gift mode
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (inGiftMode) {
                inGiftMode = false;
                enableAllButtons();
            } else {
                this.mc.displayGuiScreen(null);
            }
        } else if (keyCode == Keyboard.KEY_LCONTROL) {
            displaySuccessChance = !displaySuccessChance;
        } else {
            try {
                int intInput = Integer.parseInt(String.valueOf(keyChar));

                if (intInput > 0) {
                    player.inventory.currentItem = intInput - 1;
                }
            } catch (NumberFormatException ignored) {
                // When a non numeric character is entered.
            }
        }
    }

    private void drawIcons() {
        PlayerHistory history = villager.getPlayerHistoryFor(player.getUniqueID());
        EnumMarriageState marriageState = EnumMarriageState.byId(villager.get(EntityVillagerMCA.MARRIAGE_STATE));
        int marriageIconU =
                marriageState == EnumMarriageState.MARRIED ? marriedIconU :
                        marriageState == EnumMarriageState.ENGAGED ? engagedIconU :
                                notMarriedIconU;
        int heartIconU =
                history.getHearts() < 0 ? blackHeartIconU :
                        history.getHearts() >= 100 ? goldHeartIconU :
                                redHeartIconU;

        GL11.glPushMatrix();
        {
            GL11.glColor3f(255.0F, 255.0F, 255.0F);
            GL11.glScalef(2.0F, 2.0F, 2.0F);

            this.mc.getTextureManager().bindTexture(ICON_TEXTURES);
            this.drawTexturedModalRect(5, 15, heartIconU, 0, 16, 16);
            this.drawTexturedModalRect(5, 30, marriageIconU, 0, 16, 16);

            if (canDrawParentsIcon()) this.drawTexturedModalRect(5, 45, parentsIconU, 0, 16, 16);

            if (canDrawGiftIcon()) this.drawTexturedModalRect(5, 60, giftIconU, 0, 16, 16);
        }
        GL11.glPopMatrix();
    }

    private void drawTextPopups() {
        EnumMarriageState marriageState = EnumMarriageState.byId(villager.get(EntityVillagerMCA.MARRIAGE_STATE));
        String marriageInfo;

        if (hoveringOverHeartsIcon()) {
            int hearts = villager.getPlayerHistoryFor(player.getUniqueID()).getHearts();
            this.drawHoveringText(hearts + " hearts", 35, 55);
        }

        if (hoveringOverMarriageIcon()) {
            String spouseName = villager.get(EntityVillagerMCA.SPOUSE_NAME);
            if (marriageState == EnumMarriageState.MARRIED) marriageInfo = MCA.getLocalizer().localize("gui.interact.label.married", spouseName);
            else if (marriageState == EnumMarriageState.ENGAGED) marriageInfo = MCA.getLocalizer().localize("gui.interact.label.engaged", spouseName);
            else marriageInfo = MCA.getLocalizer().localize("gui.interact.label.notmarried");

            this.drawHoveringText(marriageInfo, 35, 85);
        }
        if (canDrawParentsIcon() && hoveringOverParentsIcon()) {
            ParentData data = ParentData.fromNBT(villager.get(EntityVillagerMCA.PARENTS));
            this.drawHoveringText(MCA.getLocalizer().localize("gui.interact.label.parents", data.getParent1Name(), data.getParent2Name()), 35, 115);
        }

        if (canDrawGiftIcon() && hoveringOverGiftIcon()) this.drawHoveringText(MCA.getLocalizer().localize("gui.interact.label.gift"), 35, 145);
    }

    private boolean hoveringOverHeartsIcon() {
        return mouseX <= 32 && mouseX >= 16 && mouseY >= 32 && mouseY <= 48;
    }

    private boolean hoveringOverMarriageIcon() {
        return mouseX <= 32 && mouseX >= 16 && mouseY >= 66 && mouseY <= 81;
    }

    private boolean hoveringOverParentsIcon() {
        return mouseX <= 32 && mouseX >= 16 && mouseY >= 100 && mouseY <= 115;
    }

    private boolean hoveringOverGiftIcon() {
        return mouseX <= 32 && mouseX >= 16 && mouseY >= 124 && mouseY <= 148;
    }

    private boolean canDrawParentsIcon() {
        ParentData data = ParentData.fromNBT(villager.get(EntityVillagerMCA.PARENTS));
        return !data.getParent1UUID().equals(Constants.ZERO_UUID) &&
                !data.getParent2UUID().equals(Constants.ZERO_UUID);
    }

    private boolean canDrawGiftIcon() {
        return villager.getPlayerHistoryFor(player.getUniqueID()).isGiftPresent();
    }

    protected void actionPerformed(GuiButton button) {
        GuiButtonEx btn = (GuiButtonEx) button;
        String id = btn.getApiButton().getIdentifier();

        if (timeSinceLastClick <= 2) {
            return; /* Prevents click-throughs on Mojang's button system */
        }
        timeSinceLastClick = 0;

        /* Progression to different GUIs */
        if (id.equals("gui.button.interact")) {
            activeKey = "interact";
            drawInteractButtonMenu();
            return;
        } else if (id.equals("gui.button.work")) {
            activeKey = "work";
            drawWorkButtonMenu();
            return;
        } else if (id.equals("gui.button.backarrow")) {
            drawMainButtonMenu();
            activeKey = "main";
            return;
        } else if (id.equals("gui.button.location")) {
            activeKey = "location";
            drawLocationButtonMenu();
            return;
        }

        /* Anything that should notify the server is handled here */
        else if (btn.getApiButton().isNotifyServer()) {
            NetMCA.INSTANCE.sendToServer(new NetMCA.ButtonAction(activeKey, id, villager.getUniqueID()));
        } else if (id.equals("gui.button.gift")) {
            this.inGiftMode = true;
            disableAllButtons();
            return;
        }

        this.mc.displayGuiScreen(null);
    }

    private void drawMainButtonMenu() {
        buttonList.clear();
        API.addButtons("main", villager, player, this);

        EnumMoveState moveState = EnumMoveState.byId(villager.get(EntityVillagerMCA.MOVE_STATE));
        if (moveState == EnumMoveState.FOLLOW) disableButton("gui.button.follow");
        else if (moveState == EnumMoveState.STAY) disableButton("gui.button.stay");
        else if (moveState == EnumMoveState.MOVE) disableButton("gui.button.move");
    }

    private void drawInteractButtonMenu() {
        buttonList.clear();
        API.addButtons("interact", villager, player, this);
    }

    private void drawWorkButtonMenu() {
        buttonList.clear();
        API.addButtons("work", villager, player, this);
    }

    private void drawLocationButtonMenu() {
        buttonList.clear();
        API.addButtons("location", villager, player, this);
    }

    private void disableButton(String id) {
        Optional<GuiButtonEx> b = API.getButton(id, this);

        b.ifPresent(guiButtonEx -> guiButtonEx.enabled = false);
    }

    private void enableAllButtons() {
        buttonList.forEach((b) -> b.enabled = true);
    }

    private void disableAllButtons() {
        buttonList.forEach((b) -> b.enabled = false);
    }
}