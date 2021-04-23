package mca.client.gui;

import mca.api.API;
import mca.api.types.APIIcon;
import mca.client.gui.component.GuiButtonEx;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.forge.NetMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.ParentData;
import mca.entity.data.PlayerHistory;
import mca.enums.EnumAgeState;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMoveState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static mca.entity.EntityVillagerMCA.AGE_STATE;

@SideOnly(Side.CLIENT)
public class GuiInteract extends GuiScreen {
    private static final ResourceLocation ICON_TEXTURES = new ResourceLocation("mca:textures/gui.png");
    private static boolean displaySuccessChance;
    private final EntityVillagerMCA villager;
    private final EntityPlayer player;

    private boolean inGiftMode;

    private int timeSinceLastClick;

    private float iconScale = 1.5f;

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
        if (inGiftMode && button == 1)
            NetMCA.INSTANCE.sendToServer(new NetMCA.ButtonAction(activeKey, "gui.button.gift", villager.getUniqueID()));
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

    private void drawIcon(String key) {
        APIIcon icon = API.getIcon(key);
        this.drawTexturedModalRect((float) (icon.getX() / iconScale), (float) (icon.getY() / iconScale), icon.getU(), icon.getV(), 16, 16);
    }

    private void drawHoveringIconText(String text, String key) {
        APIIcon icon = API.getIcon(key);
        this.drawHoveringText(text, icon.getX() + 16, icon.getY() + 20);
    }

    private void drawHoveringIconText(List<String> text, String key) {
        APIIcon icon = API.getIcon(key);
        this.drawHoveringText(text, icon.getX() + 16, icon.getY() + 20);
    }

    private void drawIcons() {
        EnumMarriageState marriageState = EnumMarriageState.byId(villager.get(EntityVillagerMCA.MARRIAGE_STATE));
        String marriageIcon =
                marriageState == EnumMarriageState.MARRIED ? "married" :
                        marriageState == EnumMarriageState.ENGAGED ? "engaged" :
                                "notMarried";

        PlayerHistory history = villager.getPlayerHistoryFor(player.getUniqueID());
        String heartIcon =
                history.getHearts() < 0 ? "blackHeart" :
                        history.getHearts() >= 100 ? "goldHeart" :
                                "redHeart";

        String emeraldIcon = "neutralEmerald";

        GL11.glPushMatrix();
        {
            GL11.glColor3f(255.0F, 255.0F, 255.0F);
            GL11.glScalef(iconScale, iconScale, iconScale);

            this.mc.getTextureManager().bindTexture(ICON_TEXTURES);

            drawIcon(marriageIcon);
            drawIcon(heartIcon);
            drawIcon(emeraldIcon);
            drawIcon("genes");

            if (canDrawParentsIcon()) drawIcon("parents");
            if (canDrawGiftIcon()) drawIcon("gift");
        }
        GL11.glPopMatrix();
    }

    private void drawTextPopups() {
        //general information
        ITextComponent careerName = new TextComponentTranslation("entity.Villager." + villager.getVanillaCareer().getName());
        EnumAgeState age = EnumAgeState.byId(villager.get(AGE_STATE));
        String professionName = age != EnumAgeState.ADULT ? age.localizedName() : careerName.getUnformattedText();

        //name or state tip (gifting, ...)
        int h = 17;
        if (inGiftMode) {
            this.drawHoveringText(MCA.getLocalizer().localize("gui.interact.label.giveGift"), 10, 28);
        } else {
            drawHoveringText(villager.getName(), 10, 28);
        }

        //age or profession
        drawHoveringText(professionName, 10, 30 + h);

        //personality
        drawHoveringText(MCA.getLocalizer().localize("gui.interact.label.mood", villager.getMood().getLocalizedName()), 10, 30 + h * 2);
        drawHoveringText(MCA.getLocalizer().localize("gui.interact.label.personality", villager.getPersonality().getLocalizedName()), 10, 30 + h * 3);

        //hearts
        if (hoveringOverIcon("redHeart")) {
            int hearts = villager.getPlayerHistoryFor(player.getUniqueID()).getHearts();
            drawHoveringIconText(hearts + " hearts", "redHeart");
        }

        //hearts
        if (hoveringOverIcon("neutralEmerald")) {
            //int hearts = villager.getPlayerHistoryFor(player.getUniqueID()).getHearts();
            drawHoveringIconText("Happiness: 5", "neutralEmerald");
        }

        //marriage status
        EnumMarriageState marriageState = EnumMarriageState.byId(villager.get(EntityVillagerMCA.MARRIAGE_STATE));
        String marriageInfo;
        if (hoveringOverIcon("married")) {
            String spouseName = villager.get(EntityVillagerMCA.SPOUSE_NAME);
            if (marriageState == EnumMarriageState.MARRIED)
                marriageInfo = MCA.getLocalizer().localize("gui.interact.label.married", spouseName);
            else if (marriageState == EnumMarriageState.ENGAGED)
                marriageInfo = MCA.getLocalizer().localize("gui.interact.label.engaged", spouseName);
            else marriageInfo = MCA.getLocalizer().localize("gui.interact.label.notmarried");

            drawHoveringIconText(marriageInfo, "married");
        }

        //parents
        if (canDrawParentsIcon() && hoveringOverIcon("parents")) {
            ParentData data = ParentData.fromNBT(villager.get(EntityVillagerMCA.PARENTS));
            drawHoveringIconText(MCA.getLocalizer().localize("gui.interact.label.parents", data.getParent1Name(), data.getParent2Name()), "parents");
        }

        //gift
        if (canDrawGiftIcon() && hoveringOverIcon("gift"))
            drawHoveringIconText(MCA.getLocalizer().localize("gui.interact.label.gift"), "gift");

        //genes
        if (hoveringOverIcon("genes")) {
            List<String> lines = new LinkedList<>();
            lines.add("Genes");
            for (int i = 0; i < EntityVillagerMCA.GENES.length; i++) {
                String key = EntityVillagerMCA.GENES_NAMES[i].replace("_", ".");
                int value = (int) (villager.get(EntityVillagerMCA.GENES[i]) * 100);
                lines.add(String.format("%s: %d%%", MCA.getLocalizer().localize(key), value));
            }
            drawHoveringIconText(lines, "genes");
        }

        //happiness
        if (hoveringOverIcon("neutralEmerald")) {
            List<String> lines = new LinkedList<>();
            lines.add(MCA.getLocalizer().localize("gui.interact.label.happiness", 0 + "/" + 10));

            drawHoveringIconText(lines, "neutralEmerald");
        }
    }

    //checks if the mouse hovers over a specified button
    private boolean hoveringOverIcon(String key) {
        APIIcon icon = API.getIcon(key);
        return mouseX > icon.getX() && mouseX < icon.getX() + 16 * iconScale && mouseY > icon.getY() && mouseY < icon.getY() + 16 * iconScale;
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
        } else if (id.equals("gui.button.command")) {
            activeKey = "command";
            drawCommandButtonMenu();
            return;
        } else if (id.equals("gui.button.clothing")) {
            activeKey = "clothing";
            drawClothingMenu();
            return;
        } else if (id.equals("gui.button.work")) {
            activeKey = "work";
            drawWorkButtonMenu();
            return;
        } else if (id.equals("gui.button.backarrow")) {
            drawMainButtonMenu();
            activeKey = "main";
            return;
        } else if (id.equals("gui.button.locations")) {
            activeKey = "locations";
            drawLocationsButtonMenu();
            return;
        }

        /* Anything that should notify the server is handled here */
        else if (btn.getApiButton().isNotifyServer()) {
            NetMCA.INSTANCE.sendToServer(new NetMCA.ButtonAction(activeKey, id, villager.getUniqueID()));

            //don't exist
            if (id.contains("gui.button.clothing")) {
                return;
            }
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

    private void drawCommandButtonMenu() {
        buttonList.clear();
        API.addButtons("command", villager, player, this);
    }

    private void drawClothingMenu() {
        buttonList.clear();
        API.addButtons("clothing", villager, player, this);
    }

    private void drawWorkButtonMenu() {
        buttonList.clear();
        API.addButtons("work", villager, player, this);
    }

    private void drawLocationsButtonMenu() {
        buttonList.clear();
        API.addButtons("locations", villager, player, this);
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