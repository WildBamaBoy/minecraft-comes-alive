package mca.client.gui;

import mca.api.API;
import mca.api.types.APIIcon;
import mca.client.gui.component.ButtonEx;
import mca.cobalt.network.NetworkHandler;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.entity.data.Memories;
import mca.enums.Chore;
import mca.enums.MarriageState;
import mca.enums.MoveState;
import mca.network.GetInteractDataRequest;
import mca.network.InteractionServerMessage;
import mca.network.InteractionVillagerMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GuiInteract extends Screen {
    private static final Identifier ICON_TEXTURES = new Identifier("mca:textures/gui.png");
    private final VillagerEntityMCA villager;
    private final PlayerEntity player;
    private final float iconScale = 1.5f;
    private Map<String, Boolean> constraints;
    private boolean inGiftMode;
    private int timeSinceLastClick;
    private int mouseX;
    private int mouseY;

    private String father;
    private String mother;

    // Tracks which page we're on in the GUI for sending button events
    private String activeKey;

    public GuiInteract(VillagerEntityMCA villager, PlayerEntity player) {
        super(new LiteralText("Interact"));

        this.villager = villager;
        this.player = player;
        this.activeKey = "main";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Objects.requireNonNull(this.client).openScreen(null);
        villager.setInteractingPlayer(null);
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new GetInteractDataRequest(villager.getUuid()));
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

        mouseX = (int) (client.mouse.getX() * width / client.getWindow().getFramebufferWidth());
        mouseY = (int) (client.mouse.getY() * height / client.getWindow().getFramebufferHeight());
    }

    @Override
    public boolean mouseScrolled(double x, double y, double d) {
        if (d < 0) {
            player.inventory.selectedSlot = player.inventory.selectedSlot == 8 ? 0 : player.inventory.selectedSlot + 1;
        } else if (d > 0) {
            player.inventory.selectedSlot = player.inventory.selectedSlot == 0 ? 8 : player.inventory.selectedSlot - 1;
        }

        return super.mouseScrolled(x, y, d);
    }

    @Override
    public boolean mouseClicked(double posX, double posY, int button) {
        super.mouseClicked(posX, posY, button);

        // Right mouse button
        if (inGiftMode && button == 1) {
            NetworkHandler.sendToServer(new InteractionVillagerMessage(activeKey, "gui.button.gift", villager.getUuid()));
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
        this.drawTexture(transform, (int) (icon.getX() / iconScale), (int) (icon.getY() / iconScale), icon.getU(), icon.getV(), 16, 16);
    }

    private void drawHoveringIconText(MatrixStack transform, String text, String key) {
        APIIcon icon = API.getIcon(key);
        renderTooltip(transform, text, icon.getX() + 16, icon.getY() + 20);
    }

    private void renderTooltip(MatrixStack transform, String text, int x, int y) {
        renderTooltip(transform, new LiteralText(text), x, y);
    }

    private void drawHoveringIconText(MatrixStack transform, List<Text> text, String key) {
        APIIcon icon = API.getIcon(key);
        this.renderTooltip(transform, text, icon.getX() + 16, icon.getY() + 20);
    }

    private void drawIcons(MatrixStack transform) {
        MarriageState marriageState = MarriageState.byId(villager.marriageState.get());
        String marriageIcon =
                marriageState == MarriageState.MARRIED ? "married" :
                        marriageState == MarriageState.ENGAGED ? "engaged" :
                                marriageState == MarriageState.MARRIED_TO_PLAYER ? "marriedToPlayer" :
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

            this.client.getTextureManager().bindTexture(ICON_TEXTURES);

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
        VillagerProfession profession = villager.getProfession();
        String professionName = villager.isBaby() ? villager.getAgeState().localizedName() : MCA.localize("entity.minecraft.villager." + profession);

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
        MarriageState marriageState = MarriageState.byId(villager.marriageState.get());
        String marriageInfo;
        if (hoveringOverIcon("married")) {
            String spouseName = villager.spouseName.get();
            if (marriageState == MarriageState.MARRIED || marriageState == MarriageState.MARRIED_TO_PLAYER)
                marriageInfo = MCA.localize("gui.interact.label.married", spouseName);
            else if (marriageState == MarriageState.ENGAGED)
                marriageInfo = MCA.localize("gui.interact.label.engaged", spouseName);
            else marriageInfo = MCA.localize("gui.interact.label.notmarried");

            drawHoveringIconText(transform, marriageInfo, "married");
        }

        //parents
        if (canDrawParentsIcon() && hoveringOverIcon("parents")) {
            drawHoveringIconText(transform, MCA.localize("gui.interact.label.parents",
                    father == null ? MCA.localize("gui.interact.label.parentUnknown") : father,
                    mother == null ? MCA.localize("gui.interact.label.parentUnknown") : mother
            ), "parents");
        }

        //gift
        if (canDrawGiftIcon() && hoveringOverIcon("gift"))
            drawHoveringIconText(transform, MCA.localize("gui.interact.label.gift"), "gift");

        //genes
        if (hoveringOverIcon("genes")) {
            List<Text> lines = new LinkedList<>();
            lines.add(new LiteralText("Genes"));
            for (int i = 0; i < villager.GENES.length; i++) {
                String key = VillagerEntityMCA.GENES_NAMES[i].replace("_", ".");
                int value = (int) (villager.GENES[i].get() * 100);
                lines.add(new LiteralText(String.format("%s: %d%%", MCA.localize(key), value)));
            }
            drawHoveringIconText(transform, lines, "genes");
        }

        //happiness
        if (hoveringOverIcon("neutralEmerald")) {
            List<Text> lines = new LinkedList<>();
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
        return father != null || mother != null;
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
        } else if (id.equals("gui.button.familyTree")) {
            MinecraftClient.getInstance().openScreen(new GuiFamilyTree(villager.getUuid()));
        } else if (id.equals("gui.button.work")) {
            activeKey = "work";
            drawWorkButtonMenu();
        } else if (id.equals("gui.button.backarrow")) {
            if (inGiftMode) {
                inGiftMode = false;
                enableAllButtons();
            } else {
                drawMainButtonMenu();
                activeKey = "main";
            }
        } else if (id.equals("gui.button.locations")) {
            activeKey = "locations";
            drawLocationsButtonMenu();
        }
        /* Anything that should notify the server is handled here */
        else if (button.getApiButton().isNotifyServer()) {
            if (button.getApiButton().isTargetServer()) {
                NetworkHandler.sendToServer(new InteractionServerMessage(activeKey, id));
            } else {
                NetworkHandler.sendToServer(new InteractionVillagerMessage(activeKey, id, villager.getUuid()));
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
        API.addButtons("main", this);
    }

    private void drawInteractButtonMenu() {
        clearButtons();
        API.addButtons("interact", this);
    }

    private void drawCommandButtonMenu() {
        clearButtons();
        API.addButtons("command", this);

        int id = villager.moveState.get();
        switch (MoveState.byId(id)) {
            case STAY:
                disableButton("gui.button.stay");
                break;
            case FOLLOW:
                disableButton("gui.button.follow");
                break;
            case MOVE:
                disableButton("gui.button.move");
                break;
        }
    }

    private void drawClothingMenu() {
        clearButtons();
        API.addButtons("clothing", this);
    }

    private void drawWorkButtonMenu() {
        clearButtons();
        API.addButtons("work", this);

        int id = villager.activeChore.get();
        switch (Chore.byId(id)) {
            case NONE:
                disableButton("gui.button.stopworking");
                break;
            case CHOP:
                disableButton("gui.button.chopping");
                break;
            case FISH:
                disableButton("gui.button.fishing");
                break;
            case HUNT:
                disableButton("gui.button.hunting");
                break;
            case HARVEST:
                disableButton("gui.button.harvesting");
                break;
            case PROSPECT:
                break;
        }
    }

    private void drawLocationsButtonMenu() {
        clearButtons();
        API.addButtons("locations", this);
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
        buttons.forEach(b -> {
            if (b instanceof ButtonEx) {
                if (!((ButtonEx) b).getApiButton().getIdentifier().equals("gui.button.backarrow")) {
                    b.active = false;
                }
            } else {
                b.active = false;
            }
        });
    }

    public Map<String, Boolean> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, Boolean> constraints) {
        this.constraints = constraints;
        drawMainButtonMenu();
    }

    public void setParents(String father, String mother) {
        this.father = father;
        this.mother = mother;
    }
}