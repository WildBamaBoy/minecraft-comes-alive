package mca.client.gui;

import mca.api.API;
import mca.api.types.Icon;
import mca.client.gui.component.ButtonEx;
import mca.cobalt.network.NetworkHandler;
import mca.entity.Genetics;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.data.Memories;
import mca.enums.MarriageState;
import mca.network.GetInteractDataRequest;
import mca.network.InteractionServerMessage;
import mca.network.InteractionVillagerMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

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
    private String activeKey = "main";

    public GuiInteract(VillagerEntityMCA villager, PlayerEntity player) {
        super(new LiteralText("Interact"));

        this.villager = villager;
        this.player = player;
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
        addDrawableChild(b);
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
            player.getInventory().selectedSlot = player.getInventory().selectedSlot == 8 ? 0 : player.getInventory().selectedSlot + 1;
        } else if (d > 0) {
            player.getInventory().selectedSlot = player.getInventory().selectedSlot == 0 ? 8 : player.getInventory().selectedSlot - 1;
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
        Icon icon = API.getIcon(key);
        this.drawTexture(transform, (int) (icon.x() / iconScale), (int) (icon.y() / iconScale), icon.u(), icon.v(), 16, 16);
    }

    private void drawHoveringIconText(MatrixStack transform, Text text, String key) {
        Icon icon = API.getIcon(key);
        renderTooltip(transform, text, icon.x() + 16, icon.y() + 20);
    }

    private void drawHoveringIconText(MatrixStack transform, List<Text> text, String key) {
        Icon icon = API.getIcon(key);
        this.renderTooltip(transform, text, icon.x() + 16, icon.y() + 20);
    }

    private void drawIcons(MatrixStack transform) {
        MarriageState marriageState = villager.marriageState.get();
        String marriageIcon =
                marriageState == MarriageState.MARRIED ? "married" :
                        marriageState == MarriageState.ENGAGED ? "engaged" :
                                marriageState == MarriageState.MARRIED_TO_PLAYER ? "marriedToPlayer" :
                                        "notMarried";

        Memories memory = villager.getVillagerBrain().getMemoriesForPlayer(player);
        String heartIcon =
                memory.getHearts() < 0 ? "blackHeart" :
                        memory.getHearts() >= 100 ? "goldHeart" :
                                "redHeart";

        String emeraldIcon = "neutralEmerald";

        transform.push();
        transform.scale(iconScale, iconScale, iconScale);

        RenderSystem.setShaderTexture(0, ICON_TEXTURES);

        drawIcon(transform, marriageIcon);
        drawIcon(transform, heartIcon);
        drawIcon(transform, emeraldIcon);
        drawIcon(transform, "genes");

        if (canDrawParentsIcon()) drawIcon(transform, "parents");
        if (canDrawGiftIcon()) drawIcon(transform, "gift");
        transform.pop();
    }

    private void drawTextPopups(MatrixStack transform) {
        //general information
        VillagerProfession profession = villager.getProfession();

        //name or state tip (gifting, ...)
        int h = 17;
        if (inGiftMode) {
            renderTooltip(transform, new TranslatableText("gui.interact.label.giveGift"), 10, 28);
        } else {
            renderTooltip(transform, villager.getName(), 10, 28);
        }

        //age or profession
        renderTooltip(transform, villager.isBaby() ? villager.getAgeState().getName() : new TranslatableText("entity.minecraft.villager." + profession), 10, 30 + h);

        VillagerBrain brain = villager.getVillagerBrain();

        //mood
        renderTooltip(transform,
                new TranslatableText("gui.interact.label.mood", brain.getMood().getName())
                .formatted(brain.getMoodLevel() < 0 ? Formatting.RED : brain.getMoodLevel() > 0 ? Formatting.GREEN : Formatting.WHITE), 10, 30 + h * 2);

        //personality
        if (hoveringOverText(10, 30 + h * 3, 128)) {
            renderTooltip(transform, brain.getPersonality().getDescription(), 10, 30 + h * 3);
        } else {
            //White as we don't know if a personality is negative
            renderTooltip(transform, new TranslatableText("gui.interact.label.personality", brain.getPersonality().getName()).formatted(Formatting.WHITE), 10, 30 + h * 3);
        }

        //hearts
        if (hoveringOverIcon("redHeart")) {
            int hearts = brain.getMemoriesForPlayer(player).getHearts();
            drawHoveringIconText(transform, new LiteralText(hearts + " hearts"), "redHeart");
        }

        //marriage status
        MarriageState marriageState = villager.marriageState.get();
        Text marriageInfo;
        if (hoveringOverIcon("married")) {
            String spouseName = villager.spouseName.get();
            if (marriageState == MarriageState.MARRIED || marriageState == MarriageState.MARRIED_TO_PLAYER)
                marriageInfo = new TranslatableText("gui.interact.label.married", spouseName);
            else if (marriageState == MarriageState.ENGAGED)
                marriageInfo = new TranslatableText("gui.interact.label.engaged", spouseName);
            else marriageInfo = new TranslatableText("gui.interact.label.notmarried");

            drawHoveringIconText(transform, marriageInfo, "married");
        }

        //parents
        if (canDrawParentsIcon() && hoveringOverIcon("parents")) {
            drawHoveringIconText(transform, new TranslatableText("gui.interact.label.parents",
                    father == null ? new TranslatableText("gui.interact.label.parentUnknown") : father,
                    mother == null ? new TranslatableText("gui.interact.label.parentUnknown") : mother
            ), "parents");
        }

        //gift
        if (canDrawGiftIcon() && hoveringOverIcon("gift"))
            drawHoveringIconText(transform, new TranslatableText("gui.interact.label.gift"), "gift");

        //genes
        if (hoveringOverIcon("genes")) {
            List<Text> lines = new LinkedList<>();
            lines.add(new LiteralText("Genes"));

            for (Genetics.Gene gene : villager.getGenetics()) {
                String key = gene.key().replace("_", ".");
                int value = (int) (gene.get() * 100);
                lines.add(new LiteralText(String.format("%s: %d%%", new TranslatableText(key), value)));
            }

            drawHoveringIconText(transform, lines, "genes");
        }

        //happiness
        if (hoveringOverIcon("neutralEmerald")) {
            List<Text> lines = new LinkedList<>();
            lines.add(new TranslatableText("gui.interact.label.happiness", "0/10"));

            drawHoveringIconText(transform, lines, "neutralEmerald");
        }
    }

    //checks if the mouse hovers over a specified button
    private boolean hoveringOverIcon(String key) {
        Icon icon = API.getIcon(key);
        return hoveringOver(icon.x(), icon.y(), (int) (16 * iconScale), (int) (16 * iconScale));
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
        String id = button.getApiButton().identifier();

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
        else if (button.getApiButton().notifyServer()) {
            if (button.getApiButton().targetServer()) {
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
        clearChildren();
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
        disableButton("gui.button." + villager.getVillagerBrain().getMoveState().name().toLowerCase());
    }

    private void drawClothingMenu() {
        clearButtons();
        API.addButtons("clothing", this);
    }

    private void drawWorkButtonMenu() {
        clearButtons();
        API.addButtons("work", this);
        disableButton("gui.button." + villager.getVillagerBrain().getCurrentJob().name().toLowerCase());
    }

    private void drawLocationsButtonMenu() {
        clearButtons();
        API.addButtons("locations", this);
    }

    private void disableButton(String id) {
        this.children().forEach(b -> {
            if (b instanceof ButtonEx) {
                if (((ButtonEx) b).getApiButton().identifier().equals(id)) {
                    ((ButtonEx)b).active = false;
                }
            }
        });
    }

    private void enableAllButtons() {
        this.children().forEach(b -> {
            if (b instanceof ClickableWidget) {
                ((ClickableWidget)b).active = true;
            }
        });
    }

    private void disableAllButtons() {
        this.children().forEach(b -> {
            if (b instanceof ClickableWidget) {
                if (b instanceof ButtonEx) {
                    if (!((ButtonEx) b).getApiButton().identifier().equals("gui.button.backarrow")) {
                        ((ClickableWidget)b).active = true;
                    }
                } else {
                    ((ClickableWidget)b).active = true;
                }
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