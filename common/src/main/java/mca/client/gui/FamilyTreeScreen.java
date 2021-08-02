package mca.client.gui;

import mca.cobalt.network.NetworkHandler;
import mca.entity.ai.relationship.MarriageState;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.network.GetFamilyTreeRequest;
import mca.util.compat.RenderSystemCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public class FamilyTreeScreen extends Screen {
    private static final int HORIZONTAL_SPACING = 20;
    private static final int VERTICAL_SPACING = 60;

    private static final int SPOUSE_HORIZONTAL_SPACING = 50;

    private UUID focusedEntityId;

    private Map<UUID, FamilyTreeNode> family = new HashMap<>();

    private final Map<UUID, TreeNode> nodes = new HashMap<>();

    private final TreeNode emptyNode = new TreeNode();

    private TreeNode tree = emptyNode;

    @Nullable
    private TreeNode focused;

    private int scrollX;
    private int scrollY;

    private final Screen parent;

    public FamilyTreeScreen(UUID entityId) {
        super(new TranslatableText("gui.family_tree.title"));
        this.focusedEntityId = entityId;
        this.parent = MinecraftClient.getInstance().currentScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void setFamilyData(UUID uuid, Map<UUID, FamilyTreeNode> family) {
        this.focusedEntityId = uuid;
        this.family.putAll(family);
        rebuildTree();
    }

    private boolean focusEntity(UUID id) {
        focusedEntityId = id;

        NetworkHandler.sendToServer(new GetFamilyTreeRequest(id));

        return false;
    }

    @Override
    public void init() {
        focusEntity(focusedEntityId);

        addButton(new ButtonWidget(width / 2 - 100, height - 25, 200, 20, new TranslatableText("gui.done"), sender -> {
            onClose();
        }));
    }

    @Override
    public void onClose() {
        client.openScreen(parent);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            scrollX += deltaX;
            scrollY += deltaY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && focused != null) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1));
            if (focusEntity(focused.id)) {
                rebuildTree();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        fill(matrices, 0, 30, width, height - 30, 0x66000000);

        focused = null;

        Window window = MinecraftClient.getInstance().getWindow();
        double f = window.getScaleFactor();
        int windowHeight = (int)Math.round(window.getScaledHeight() * f);

        int x = 0;
        int y = (int)(30 * f);
        int w = (int)(width * f);
        int h = (int)((height - 60) * f);

        GL11.glScissor(x, windowHeight - h - y, w, h);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        matrices.push();

        int xx = scrollX + width / 2;
        int yy = scrollY + height / 2;
        matrices.translate(xx, yy, 0);
        tree.render(matrices, mouseX - xx, mouseY - yy);
        matrices.pop();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        FamilyTreeNode selected = family.get(focusedEntityId);

        Text label = selected == null ? title : new LiteralText(selected.getName()).append("'s ").append(title);

        drawCenteredText(matrices, textRenderer, label, width / 2, 10, 16777215);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void rebuildTree() {
        scrollX = 14;
        scrollY = -69;
        FamilyTreeNode focusedNode = family.get(focusedEntityId);

        // garbage collect
        focused = null;
        tree = emptyNode;
        nodes.clear();

        if (focusedNode != null) {
            tree = insertParents(new TreeNode(focusedNode, true), focusedNode, 2);
        }
    }

    private TreeNode insertParents(TreeNode root, FamilyTreeNode focusedNode, int levels) {
        @Nullable FamilyTreeNode father = family.get(focusedNode.father());
        @Nullable FamilyTreeNode mother = family.get(focusedNode.mother());

        @Nullable FamilyTreeNode newRoot = father != null ? father : mother;

        TreeNode fNode = newRoot == null ? new TreeNode() : new TreeNode(newRoot, false);
        fNode.children.add(root);

        @Nullable FamilyTreeNode spouse = newRoot == father ? mother : father;

        fNode.spouse = spouse == null ? new TreeNode() : new TreeNode(spouse, false);

        if (newRoot != null && levels > 0) {
            return insertParents(fNode, newRoot, levels - 1);
        }

        return fNode;
    }

    private final class TreeNode {
        private boolean widthComputed;

        private int width;

        private int labelWidth;

        private final List<Text> label = new ArrayList<>();

        private final List<TreeNode> children = new ArrayList<>();

        private Bounds bounds;

        TreeNode spouse;

        final UUID id;

        final boolean deceased;

        @Nullable
        private TreeNode previous;

        private TreeNode() {
            this.id = null;
            this.deceased = false;
            this.label.add(new LiteralText("???"));
        }

        public TreeNode(FamilyTreeNode node, boolean recurse) {
            this(node, new HashSet<>(), recurse);
        }

        public TreeNode(FamilyTreeNode node, Set<UUID> parsed, boolean recurse) {
            nodes.put(node.id(), this);
            this.id = node.id();
            this.deceased = node.isDeceased();
            this.label.add(new LiteralText(node.getName().isEmpty() ? "???" : node.getName()).formatted(node.gender().getColor()));
            this.label.add(new TranslatableText("entity.minecraft.villager." + node.getProfession()).formatted(Formatting.GRAY));

            FamilyTreeNode father = family.get(node.father());
            FamilyTreeNode mother = family.get(node.mother());
            if ((father == null || father.isDeceased()) && (mother == null || mother.isDeceased())) {
                this.label.add(new TranslatableText("gui.family_tree.label.orphan").formatted(Formatting.GRAY));
            }

            if (node.getMarriageState() != MarriageState.SINGLE) {
                this.label.add(new TranslatableText("marriage." + node.getMarriageState().base().getIcon()));
            }

            if (recurse) {
                node.children().forEach(child -> {
                    FamilyTreeNode e = family.get(child);
                    if (e != null) {
                        children.add(new TreeNode(e, parsed, parsed.add(child)));
                    }
                });
            }

            FamilyTreeNode spouse = family.get(node.spouse());

            if (spouse != null) {
                this.spouse = new TreeNode(spouse, parsed, false);
            } else if (!children.isEmpty()) {
                this.spouse = new TreeNode();
            }
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY) {

            Bounds bounds = getBounds();

            boolean isFocused = id != null && bounds.contains(mouseX, mouseY);

            if (isFocused) {
                focused = this;
            }

            int childrenStartX = -getWidth() / 2;

            for (int i = 0; i < children.size(); i++) {
                TreeNode node = children.get(i);

                childrenStartX += (node.getWidth() + HORIZONTAL_SPACING) / 2;

                int x = childrenStartX + HORIZONTAL_SPACING / 2;
                int y = VERTICAL_SPACING;

                drawHook(matrices, x, y);

                matrices.push();
                matrices.translate(x, y, 0);
                node.render(matrices, mouseX - x,  mouseY - y);
                matrices.pop();

                childrenStartX += (node.getWidth() + HORIZONTAL_SPACING) / 2;
            }

            matrices.push();
            matrices.translate(0, 0, 400);

            int fillColor = isFocused ? 0xF0100040 : 0xF0100010;
            int borderColor = isFocused ? 0xFF28007F : 1347420415;

            fill(matrices, bounds.left, bounds.top + 1, bounds.left + 1, bounds.bottom - 1, fillColor);
            fill(matrices, bounds.right - 1, bounds.top + 1, bounds.right, bounds.bottom - 1, fillColor);
            fill(matrices, bounds.left + 1, bounds.top, bounds.right - 1, bounds.bottom, fillColor);

            fill(matrices, bounds.left + 1, bounds.top + 1, bounds.left + 2, bounds.bottom - 1, borderColor);
            fill(matrices, bounds.right - 2, bounds.top + 1, bounds.right - 1, bounds.bottom - 1, borderColor);

            fill(matrices, bounds.left + 2, bounds.top + 1, bounds.right - 2, bounds.top + 2, borderColor);
            fill(matrices, bounds.left + 2, bounds.bottom - 2, bounds.right - 2, bounds.bottom - 1, borderColor);

            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

            int l = bounds.top + 5;
            int k = bounds.left + 6;

            if (deceased) {
                k += 20;
            }

            Matrix4f matrix4f = matrices.peek().getModel();

            TextRenderer r = MinecraftClient.getInstance().textRenderer;

            for (int s = 0; s < label.size(); ++s) {
                Text line = label.get(s);
                if (line != null) {
                    r.draw(line, k, l, -1, true, matrix4f, immediate, false, 0, 15728880);
                }

                if (s == 0) {
                    l += 2;
                }

                l += 10;
            }

            immediate.draw();
            matrices.pop();

            RenderSystemCompat.setShaderTexture(0, InteractScreen.ICON_TEXTURES);

            if (deceased) {
                drawTexture(matrices, bounds.left + 6, bounds.top + 6, 0, 16, 16, 16, 16, 256, 256);

                if (isFocused && mouseX <= bounds.left + 20) {
                    matrices.push();
                    matrices.translate(0, 0, 20);
                    renderTooltip(matrices, new TranslatableText("gui.family_tree.label.deceased"), mouseX, mouseY);
                    matrices.pop();
                }
            }

            if (spouse != null) {
                int x = bounds.left - SPOUSE_HORIZONTAL_SPACING;
                int y = bounds.top + bounds.bottom / 2;

                drawHorizontalLine(matrices, x, bounds.left - 1, y, 0xffffffff);

                drawTexture(matrices, bounds.left - SPOUSE_HORIZONTAL_SPACING / 2 - 8, y - 8, 0, 0, 0, 16, 16, 256, 256);

                y -= spouse.label.size() * textRenderer.fontHeight / 2;
                x -= spouse.getWidth() / 2 - 6;

                matrices.push();
                matrices.translate(x, y, 0);

                spouse.render(matrices, mouseX - x, mouseY - y);
                matrices.pop();
            }
        }

        private void drawHook(MatrixStack matrices, int endX, int endY) {
            int midY = endY / 2;

            drawVerticalLine(matrices, 0, 0, midY, 0xffffffff);
            drawHorizontalLine(matrices, 0, endX, midY, 0xffffffff);
            drawVerticalLine(matrices, endX, midY, endY, 0xffffffff);
        }

        public int getWidth() {
            if (!widthComputed) {
                widthComputed = true;
                labelWidth = label.stream().mapToInt(textRenderer::getWidth).max().orElse(0);
                if (deceased) {
                    labelWidth += 20;
                }
                width = Math.max(labelWidth + 10, children.stream().mapToInt(TreeNode::getWidth).sum()) + (HORIZONTAL_SPACING / 2);
                if (spouse != null) {
                    width += spouse.getWidth() + SPOUSE_HORIZONTAL_SPACING;
                }
            }
            return width;
        }

        public Bounds getBounds() {
            if (bounds == null) {
                getWidth();

                int padding = 4;
                bounds = new Bounds(
                        (-labelWidth / 2) - padding,
                        (labelWidth / 2) + padding * 2,
                        -padding,
                        textRenderer.fontHeight * label.size() + padding * 2
                );
            }
            return bounds;
        }
    }

    static final class Bounds {
        final int left;
        final int right;
        final int top;
        final int bottom;

        public Bounds(int left, int right, int top, int bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }

        public Bounds add(int x, int y) {
            return new Bounds(left + x, right + x, top + y, bottom + y);
        }

        public boolean contains(int mouseX, int mouseY) {
            return mouseX >= left
                && mouseY >= top
                && mouseX <= right
                && mouseY <= bottom;
        }
    }
}
