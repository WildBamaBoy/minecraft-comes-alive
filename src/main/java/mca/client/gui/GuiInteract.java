package mca.client.gui;

import cobalt.network.NetworkHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import mca.api.API;
import mca.api.types.APIIcon;
import mca.client.gui.component.ButtonEx;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.Memories;
import mca.entity.data.ParentPair;
import mca.enums.EnumAgeState;
import mca.enums.EnumMarriageState;
import mca.network.InteractionServerMessage;
import mca.network.InteractionVillagerMessage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class GuiInteract extends Screen {
    private static final ResourceLocation ICON_TEXTURES = new ResourceLocation("mca:textures/gui.png");
    private final EntityVillagerMCA villager;
    private final PlayerEntity player;

    private boolean inGiftMode;

    private int timeSinceLastClick;

    private final float iconScale = 1.5f;

    private int mouseX;
    private int mouseY;

    // Tracks which page we're on in the GUI for sending button events
    private String activeKey;

    public GuiInteract(EntityVillagerMCA villager, PlayerEntity player) {
        super(new StringTextComponent("Interact"));

        this.villager = villager;
        this.player = player;
        this.activeKey = "main";
    }

    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Objects.requireNonNull(this.minecraft).setScreen(null);
    }

    @Override
    public void init() {
        drawMainButtonMenu();
    }

    public void addExButton(ButtonEx b) {
        addButton(b);
    }

    @Override
    public void tick() {
        if (timeSinceLastClick < 100) {
            timeSinceLastClick++;
        }
    }

    @Override
    public void render(MatrixStack transform, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(transform, p_230430_2_, p_230430_3_, p_230430_4_);

        drawIcons(transform);
        drawTextPopups(transform);

        mouseX = (int) (minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getWidth());
        mouseY = (int) (minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getHeight());
    }

    @Override
    public boolean mouseScrolled(double x, double y, double d) {
        if (d < 0) {
            player.inventory.selected = player.inventory.selected == 8 ? 0 : player.inventory.selected + 1;
        } else if (d > 0) {
            player.inventory.selected = player.inventory.selected == 0 ? 8 : player.inventory.selected - 1;
        }

        return super.mouseScrolled(x, y, d);
    }

    @Override
    public boolean mouseClicked(double posX, double posY, int button) {
        super.mouseClicked(posX, posY, button);

        // Right mouse button
        if (inGiftMode && button == 1) {
            NetworkHandler.sendToServer(new InteractionVillagerMessage(activeKey, "gui.button.gift", villager.getUUID()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyChar, int keyCode, int unknown) {
        // Hotkey to leave gift mode
        if (keyChar == GLFW.GLFW_KEY_ESCAPE) {
            if (inGiftMode) {
                inGiftMode = false;
                enableAllButtons();
            } else {
                onClose();
            }
            return true;
        }
        return false;
    }

    private void drawIcon(MatrixStack transform, String key) {
        APIIcon icon = API.getIcon(key);
        this.blit(transform, (int) (icon.getX() / iconScale), (int) (icon.getY() / iconScale), icon.getU(), icon.getV(), 16, 16);
    }

    private void drawHoveringIconText(MatrixStack transform, String text, String key) {
        APIIcon icon = API.getIcon(key);
        renderTooltip(transform, text, icon.getX() + 16, icon.getY() + 20);
    }

    private void renderTooltip(MatrixStack transform, String text, int x, int y) {
        renderTooltip(transform, new StringTextComponent(text), x, y);
    }

    private void drawHoveringIconText(MatrixStack transform, List<ITextComponent> text, String key) {
        APIIcon icon = API.getIcon(key);
        this.renderComponentTooltip(transform, text, icon.getX() + 16, icon.getY() + 20);
    }

    private void drawIcons(MatrixStack transform) {
        EnumMarriageState marriageState = EnumMarriageState.byId(villager.marriageState.get());
        String marriageIcon =
                marriageState == EnumMarriageState.MARRIED ? "married" :
                        marriageState == EnumMarriageState.ENGAGED ? "engaged" :
                                "notMarried";

        Memories memory = villager.getMemoriesForPlayer(player);
        String heartIcon =
                memory.getHearts() < 0 ? "blackHeart" :
                        memory.getHearts() >= 100 ? "goldHeart" :
                                "redHeart";

        String emeraldIcon = "neutralEmerald";

        GL11.glPushMatrix();
        {
            GL11.glScalef(iconScale, iconScale, iconScale);

            this.minecraft.getTextureManager().bind(ICON_TEXTURES);

            drawIcon(transform, marriageIcon);
            drawIcon(transform, heartIcon);
            drawIcon(transform, emeraldIcon);
            drawIcon(transform, "genes");

            if (canDrawParentsIcon()) drawIcon(transform, "parents");
            if (canDrawGiftIcon()) drawIcon(transform, "gift");
        }
        GL11.glPopMatrix();
    }

    private void drawTextPopups(MatrixStack transform) {
        //general information
        EnumAgeState age = villager.getAgeState();
        String professionName = age != EnumAgeState.ADULT ? age.localizedName() : MCA.localize("entity.Villager." + villager.getProfession());

        //name or state tip (gifting, ...)
        int h = 17;
        if (inGiftMode) {
            renderTooltip(transform, MCA.localize("gui.interact.label.giveGift"), 10, 28);
        } else {
            renderTooltip(transform, villager.getName(), 10, 28);
        }

        //age or profession
        renderTooltip(transform, professionName, 10, 30 + h);

        //mood
        String color = villager.getMoodLevel() < 0 ? Constants.Color.RED : villager.getMoodLevel() > 0 ? Constants.Color.GREEN : Constants.Color.WHITE;
        String mood = MCA.localize("gui.interact.label.mood", villager.getMood().getLocalizedName());
        renderTooltip(transform, color + mood, 10, 30 + h * 2);

        //personality
        if (hoveringOverText(10, 30 + h * 3, 128)) {
            renderTooltip(transform, villager.getPersonality().getLocalizedDescription(), 10, 30 + h * 3);
        } else {
            color = Constants.Color.WHITE; //White as we don't know if a personality is negative
            String personality = MCA.localize("gui.interact.label.personality", villager.getPersonality().getLocalizedName());
            renderTooltip(transform, color + personality, 10, 30 + h * 3);
        }

        //hearts
        if (hoveringOverIcon("redHeart")) {
            int hearts = villager.getMemoriesForPlayer(player).getHearts();
            drawHoveringIconText(transform, hearts + " hearts", "redHeart");
        }

        //marriage status
        EnumMarriageState marriageState = EnumMarriageState.byId(villager.marriageState.get());
        String marriageInfo;
        if (hoveringOverIcon("married")) {
            String spouseName = villager.spouseName.get();
            if (marriageState == EnumMarriageState.MARRIED)
                marriageInfo = MCA.localize("gui.interact.label.married", spouseName);
            else if (marriageState == EnumMarriageState.ENGAGED)
                marriageInfo = MCA.localize("gui.interact.label.engaged", spouseName);
            else marriageInfo = MCA.localize("gui.interact.label.notmarried");

            drawHoveringIconText(transform, marriageInfo, "married");
        }

        //parents
        if (canDrawParentsIcon() && hoveringOverIcon("parents")) {
            ParentPair data = ParentPair.fromNBT(villager.parents.get());
            drawHoveringIconText(transform, MCA.localize("gui.interact.label.parents", data.getParent1Name(), data.getParent2Name()), "parents");
        }

        //gift
        if (canDrawGiftIcon() && hoveringOverIcon("gift"))
            drawHoveringIconText(transform, MCA.localize("gui.interact.label.gift"), "gift");

        //genes
        if (hoveringOverIcon("genes")) {
            List<ITextComponent> lines = new LinkedList<>();
            lines.add(new StringTextComponent("Genes"));
            for (int i = 0; i < villager.GENES.length; i++) {
                String key = EntityVillagerMCA.GENES_NAMES[i].replace("_", ".");
                int value = (int) (villager.GENES[i].get() * 100);
                lines.add(new StringTextComponent(String.format("%s: %d%%", MCA.localize(key), value)));
            }
            drawHoveringIconText(transform, lines, "genes");
        }

        //happiness
        if (hoveringOverIcon("neutralEmerald")) {
            List<ITextComponent> lines = new LinkedList<>();
            lines.add(MCA.localizeText("gui.interact.label.happiness", "0/10"));

            drawHoveringIconText(transform, lines, "neutralEmerald");
        }
    }

    //checks if the mouse hovers over a specified button
    private boolean hoveringOverIcon(String key) {
        APIIcon icon = API.getIcon(key);
        return hoveringOver(icon.getX(), icon.getY(), (int) (16 * iconScale), (int) (16 * iconScale));
    }

    //checks if the mouse hovers over a rectangle
    private boolean hoveringOver(int x, int y, int w, int h) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    //checks if the mouse hovers over a tooltip
    //tooltips are not rendered on the given coordinates so we need an offset
    private boolean hoveringOverText(int x, int y, int w) {
        return hoveringOver(x + 8, y - 16, w, 16);
    }

    private boolean canDrawParentsIcon() {
        ParentPair data = ParentPair.fromNBT(villager.parents.get());
        return !data.getParent1UUID().equals(Constants.ZERO_UUID) &&
                !data.getParent2UUID().equals(Constants.ZERO_UUID);
    }

    private boolean canDrawGiftIcon() {
//        return villager.getMemoriesForPlayer(player).isGiftPresent();
        return false;
    }

    public void buttonPressed(ButtonEx button) {
        String id = button.getApiButton().getIdentifier();

        if (timeSinceLastClick <= 2) {
            return; /* Prevents click-throughs on Mojang's button system */
        }
        timeSinceLastClick = 0;

        /* Progression to different GUIs */
        if (id.equals("gui.button.interact")) {
            activeKey = "interact";
            drawInteractButtonMenu();
        } else if (id.equals("gui.button.command")) {
            activeKey = "command";
            drawCommandButtonMenu();
        } else if (id.equals("gui.button.clothing")) {
            activeKey = "clothing";
            drawClothingMenu();
        } else if (id.equals("gui.button.work")) {
            activeKey = "work";
            drawWorkButtonMenu();
        } else if (id.equals("gui.button.backarrow")) {
            drawMainButtonMenu();
            activeKey = "main";
        } else if (id.equals("gui.button.locations")) {
            activeKey = "locations";
            drawLocationsButtonMenu();
        }
        /* Anything that should notify the server is handled here */
        else if (button.getApiButton().isNotifyServer()) {
            if (button.getApiButton().isTargetServer()) {
                NetworkHandler.sendToServer(new InteractionServerMessage(activeKey, id));
            } else {
                NetworkHandler.sendToServer(new InteractionVillagerMessage(activeKey, id, villager.getUUID()));
            }
        } else if (id.equals("gui.button.gift")) {
            this.inGiftMode = true;
            disableAllButtons();
        }
    }

    private void clearButtons() {
        buttons.clear();
        children.clear();
    }

    private void drawMainButtonMenu() {
        clearButtons();
        API.addButtons("main", villager, player, this);

        //TODO Reimplement disable button, this doesn't work anyway
        /*EnumMoveState moveState = EnumMoveState.byId(villager.moveState.get());
        if (moveState == EnumMoveState.FOLLOW) disableButton("gui.button.follow");
        else if (moveState == EnumMoveState.STAY) disableButton("gui.button.stay");
        else if (moveState == EnumMoveState.MOVE) disableButton("gui.button.move");*/
    }

    private void drawInteractButtonMenu() {
        clearButtons();
        API.addButtons("interact", villager, player, this);
    }

    private void drawCommandButtonMenu() {
        clearButtons();
        API.addButtons("command", villager, player, this);
    }

    private void drawClothingMenu() {
        clearButtons();
        API.addButtons("clothing", villager, player, this);
    }

    private void drawWorkButtonMenu() {
        clearButtons();
        API.addButtons("work", villager, player, this);
    }

    private void drawLocationsButtonMenu() {
        clearButtons();
        API.addButtons("locations", villager, player, this);
    }

    private void disableButton(String id) {
        buttons.forEach(b -> {
            if (((ButtonEx) b).getApiButton().getIdentifier().equals(id)) {
                b.active = false;
            }
        });
    }

    private void enableAllButtons() {
        buttons.forEach(b -> b.active = true);
    }

    private void disableAllButtons() {
        buttons.forEach(b -> b.active = false);
    }
}