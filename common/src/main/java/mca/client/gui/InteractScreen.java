package mca.client.gui;

import mca.cobalt.network.NetworkHandler;
import mca.entity.VillagerEntityMCA;
import mca.entity.VillagerLike;
import mca.entity.ai.Genetics;
import mca.entity.ai.Memories;
import mca.entity.ai.brain.VillagerBrain;
import mca.entity.ai.relationship.CompassionateEntity;
import mca.entity.ai.relationship.MarriageState;
import mca.network.GetInteractDataRequest;
import mca.network.InteractionDialogueInitMessage;
import mca.network.InteractionDialogueMessage;
import mca.network.InteractionServerMessage;
import mca.network.InteractionVillagerMessage;
import mca.resources.Dialogues;
import mca.resources.data.Question;
import mca.util.compat.RenderSystemCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class InteractScreen extends AbstractDynamicScreen {
    public static final Identifier ICON_TEXTURES = new Identifier("mca:textures/gui.png");

    private final VillagerLike<?> villager;
    private final PlayerEntity player = MinecraftClient.getInstance().player;

    private boolean inGiftMode;
    private int timeSinceLastClick;

    private String father;
    private String mother;

    private Question dialogQuestion;
    private List<String> dialogAnswers;
    private String dialogAnswerHover;
    private List<OrderedText> dialogQuestionText;

    public InteractScreen(VillagerLike<?> villager) {
        super(new LiteralText("Interact"));
        this.villager = villager;
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
        if (villager instanceof VillagerEntityMCA) {
            ((VillagerEntityMCA)villager).getInteractions().stopInteracting();
        }
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new GetInteractDataRequest(villager.asEntity().getUuid()));
    }

    @Override
    public void tick() {
        if (timeSinceLastClick < 100) {
            timeSinceLastClick++;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        super.render(matrices, mouseX, mouseY, tickDelta);

        drawIcons(matrices);
        drawTextPopups(matrices);
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

        // Dialog
        if (button == 0 && dialogAnswerHover != null && dialogQuestion != null) {
            NetworkHandler.sendToServer(new InteractionDialogueMessage(villager.asEntity().getUuid(), dialogQuestion.getId(), dialogAnswerHover));
        }

        // Right mouse button
        if (inGiftMode && button == 1) {
            NetworkHandler.sendToServer(new InteractionVillagerMessage("gui.button.gift", villager.asEntity().getUuid()));
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

    private void drawIcons(MatrixStack transform) {
        Memories memory = villager.getVillagerBrain().getMemoriesForPlayer(player);

        transform.push();
        transform.scale(iconScale, iconScale, iconScale);

        RenderSystemCompat.setShaderTexture(0, ICON_TEXTURES);

        if (villager instanceof CompassionateEntity<?>) {
            MarriageState marriageState = ((CompassionateEntity<?>)villager).getRelationships().getMarriageState();
            drawIcon(transform, marriageState.getIcon());
        }

        drawIcon(transform, memory.getHearts() < 0 ? "blackHeart" : memory.getHearts() >= 100 ? "goldHeart" : "redHeart");
        drawIcon(transform, "neutralEmerald");
        drawIcon(transform, "genes");

        if (canDrawParentsIcon()) {
            drawIcon(transform, "parents");
        }
        if (canDrawGiftIcon()) {
            drawIcon(transform, "gift");
        }
        transform.pop();
    }

    private void drawTextPopups(MatrixStack transform) {
        //general information
        VillagerProfession profession = villager.getVillagerData().getProfession();

        //name or state tip (gifting, ...)
        int h = 17;
        if (inGiftMode) {
            renderTooltip(transform, new TranslatableText("gui.interact.label.giveGift"), 10, 28);
        } else {
            renderTooltip(transform, villager.asEntity().getName(), 10, 28);
        }


        //age or profession
        renderTooltip(transform, villager.asEntity().isBaby() ? villager.getAgeState().getName() : new TranslatableText("entity.minecraft.villager." + profession), 10, 30 + h);

        VillagerBrain<?> brain = villager.getVillagerBrain();

        //mood
        renderTooltip(transform,
                new TranslatableText("gui.interact.label.mood", brain.getMood().getText())
                        .formatted(brain.getMood().getColor()), 10, 30 + h * 2);

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
        if (hoveringOverIcon("married") && villager instanceof CompassionateEntity<?>) {
            String marriageState = ((CompassionateEntity<?>)villager).getRelationships().getMarriageState().base().getIcon().toLowerCase();
            Text spouseName = ((CompassionateEntity<?>)villager).getRelationships().getSpouseName().orElseGet(() -> new TranslatableText("gui.interact.label.parentUnknown"));

            drawHoveringIconText(transform, new TranslatableText("gui.interact.label." + marriageState, spouseName), "married");
        }

        //parents
        if (canDrawParentsIcon() && hoveringOverIcon("parents")) {
            drawHoveringIconText(transform, new TranslatableText("gui.interact.label.parents",
                    father == null ? new TranslatableText("gui.interact.label.parentUnknown") : father,
                    mother == null ? new TranslatableText("gui.interact.label.parentUnknown") : mother
            ), "parents");
        }

        //gift
        if (canDrawGiftIcon() && hoveringOverIcon("gift")) {
            drawHoveringIconText(transform, new TranslatableText("gui.interact.label.gift"), "gift");
        }

        //genes
        if (hoveringOverIcon("genes")) {
            List<Text> lines = new LinkedList<>();
            lines.add(new LiteralText("Genes"));

            for (Genetics.Gene gene : villager.getGenetics()) {
                String key = gene.getType().key().replace("_", ".");
                int value = (int)(gene.get() * 100);
                lines.add(new TranslatableText("gene.tooltip", new TranslatableText(key), value));
            }

            drawHoveringIconText(transform, lines, "genes");
        }

        //happiness
        if (hoveringOverIcon("neutralEmerald") && false) {
            List<Text> lines = new LinkedList<>();
            lines.add(new TranslatableText("gui.interact.label.happiness", "0/10"));

            drawHoveringIconText(transform, lines, "neutralEmerald");
        }

        //dialogue
        if (dialogQuestion != null) {
            //background
            fill(transform, width / 2 - 85, height / 2 - 50 - 10 * dialogQuestionText.size(), width / 2 + 85,
                    height / 2 - 30 + 10 * dialogAnswers.size(), 0x77000000);

            //question
            int i = -dialogQuestionText.size();
            for (OrderedText t : dialogQuestionText) {
                i++;
                textRenderer.drawWithShadow(transform, t, (float)(width / 2 - textRenderer.getWidth(t) / 2), (float)height / 2 - 50 + i * 10, 0xFFFFFFFF);
            }
            dialogAnswerHover = null;

            //separator
            drawHorizontalLine(transform, width / 2 - 75, width / 2 + 75, height / 2 - 40, 0xAAFFFFFF);

            //answers
            int y = height / 2 - 35;
            for (String a : dialogAnswers) {
                boolean hover = hoveringOver(width / 2 - 100, y - 3, 200, 10);
                drawCenteredText(transform, textRenderer, new TranslatableText(dialogQuestion.getTranslationKey(a)), width / 2, y, hover ? 0xFFD7D784 : 0xAAFFFFFF);
                if (hover) {
                    dialogAnswerHover = a;
                }
                y += 10;
            }
        }
    }

    //checks if the mouse hovers over a tooltip
    //tooltips are not rendered on the given coordinates, so we need an offset
    private boolean hoveringOverText(int x, int y, int w) {
        return hoveringOver(x + 8, y - 16, w, 16);
    }

    private boolean canDrawParentsIcon() {
        return father != null || mother != null;
    }

    private boolean canDrawGiftIcon() {
        return false;//villager.getVillagerBrain().getMemoriesForPlayer(player).isGiftPresent();
    }

    public void setDialogue(String dialogue, List<String> answers, boolean silent) {
        dialogQuestion = Dialogues.getInstance().getQuestion(dialogue);
        dialogAnswers = answers;
        TranslatableText translatable = villager.getTranslatable(player, dialogQuestion.getTranslationKey());
        dialogQuestionText = textRenderer.wrapLines(translatable, 160);

        if (!silent) {
            villager.sendChatMessage(translatable, player);
        }
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
            MinecraftClient.getInstance().openScreen(new FamilyTreeScreen(villager.asEntity().getUuid()));
        } else if (id.equals("gui.button.talk")) {
            children.clear();
            buttons.clear();
            NetworkHandler.sendToServer(new InteractionDialogueInitMessage(villager.asEntity().getUuid()));
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
                NetworkHandler.sendToServer(new InteractionServerMessage(id));
            } else {
                NetworkHandler.sendToServer(new InteractionVillagerMessage(id, villager.asEntity().getUuid()));
            }
        } else if (id.equals("gui.button.gift")) {
            this.inGiftMode = true;
            disableAllButtons();
        }
    }
}