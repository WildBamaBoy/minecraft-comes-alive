package mca.client.gui;

import mca.api.API;
import mca.api.types.Button;
import mca.api.types.Icon;
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

public class GuiInteract extends AbstractDynamicScreen {
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

    public GuiInteract(VillagerEntityMCA villager, PlayerEntity player) {
        super(new LiteralText("Interact"));

        this.villager = villager;
        this.player = player;
    }

    @Override
    public Map<String, Boolean> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, Boolean> constraints) {
        this.constraints = constraints;
        setLayout("main");
    }

    public void setParents(String father, String mother) {
        this.father = father;
        this.mother = mother;
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
            NetworkHandler.sendToServer(new InteractionVillagerMessage(getActiveScreen(), "gui.button.gift", villager.getUuid()));
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
        Icon icon = API.getScreenComponents().getIcon(key);
        this.drawTexture(transform, (int) (icon.x() / iconScale), (int) (icon.y() / iconScale), icon.u(), icon.v(), 16, 16);
    }

    private void drawHoveringIconText(MatrixStack transform, Text text, String key) {
        Icon icon = API.getScreenComponents().getIcon(key);
        renderTooltip(transform, text, icon.x() + 16, icon.y() + 20);
    }

    private void drawHoveringIconText(MatrixStack transform, List<Text> text, String key) {
        Icon icon = API.getScreenComponents().getIcon(key);
        renderTooltip(transform, text, icon.x() + 16, icon.y() + 20);
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
        Icon icon = API.getScreenComponents().getIcon(key);
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
        return false;//villager.getVillagerBrain().getMemoriesForPlayer(player).isGiftPresent();
    }

    @Override
    protected void buttonPressed(Button button) {
        String id = button.identifier();

        if (timeSinceLastClick <= 2) {
            return; /* Prevents click-throughs on Mojang's button system */
        }
        timeSinceLastClick = 0;

        /* Progression to different GUIs */
        if (id.equals("gui.button.interact")) {
            setLayout("interact");
        } else if (id.equals("gui.button.command")) {
            setLayout("command");
            disableButton("gui.button." + villager.getVillagerBrain().getMoveState().name().toLowerCase());
        } else if (id.equals("gui.button.clothing")) {
            setLayout("clothing");
        } else if (id.equals("gui.button.familyTree")) {
            MinecraftClient.getInstance().openScreen(new GuiFamilyTree(villager.getUuid()));
        } else if (id.equals("gui.button.work")) {
            setLayout("work");
            disableButton("gui.button." + villager.getVillagerBrain().getCurrentJob().name().toLowerCase());
        } else if (id.equals("gui.button.backarrow")) {
            if (inGiftMode) {
                inGiftMode = false;
                enableAllButtons();
            } else {
                setLayout("main");
            }
        } else if (id.equals("gui.button.locations")) {
            setLayout("locations");
        } else if (button.notifyServer()) {
            /* Anything that should notify the server is handled here */

            if (button.targetServer()) {
                NetworkHandler.sendToServer(new InteractionServerMessage(getActiveScreen(), id));
            } else {
                NetworkHandler.sendToServer(new InteractionVillagerMessage(getActiveScreen(), id, villager.getUuid()));
            }
        } else if (id.equals("gui.button.gift")) {
            this.inGiftMode = true;
            disableAllButtons();
        }
    }
}