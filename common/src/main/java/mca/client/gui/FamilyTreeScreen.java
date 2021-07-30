package mca.client.gui;

import mca.cobalt.network.NetworkHandler;
import mca.entity.ai.relationship.family.FamilyTreeNode;
import mca.network.GetFamilyTreeRequest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public class FamilyTreeScreen extends Screen {
    private static final int HORIZONTAL_SPACING = 20;
    private static final int VERTICAL_SPACING = 60;

    private UUID focusedEntityId;

    private Map<UUID, FamilyTreeNode> family = new HashMap<>();

    @Nullable
    private TreeNode tree;

    private int scrollX;
    private int scrollY;

    private final Screen parent;

    public FamilyTreeScreen(UUID entityId) {
        super(new LiteralText("Family Tree"));
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

    private void focusEntity(UUID id) {
        if (!family.containsKey(focusedEntityId)) {
            NetworkHandler.sendToServer(new GetFamilyTreeRequest(id));
        }
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        fill(matrices, 0, 30, width, height - 30, 0x66000000);

        if (tree != null) {
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
            matrices.translate(scrollX + width / 2, scrollY + height / 2, 0);
            tree.render(matrices);
            matrices.pop();

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        drawCenteredText(matrices, textRenderer, title, width / 2, 10, 16777215);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void rebuildTree() {
        scrollX = 14;
        scrollY = -69;
        FamilyTreeNode focusedNode = family.get(focusedEntityId);

        if (focusedNode != null) {
            tree = null; // garbage collect
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

        private List<Text> label = new ArrayList<>();

        private List<TreeNode> children = new ArrayList<>();

        private TreeNode spouse;

        public TreeNode() {
            this.label.add(new LiteralText("???"));
        }

        public TreeNode(FamilyTreeNode node, boolean recurse) {
            this.label.add(new LiteralText(node.getName()).formatted(node.gender().getColor()));
            this.label.add(new TranslatableText("entity.minecraft.villager." + node.getProfession()).formatted(Formatting.GRAY));
            if (recurse) {
                node.children().forEach(child -> {
                    FamilyTreeNode e = family.get(child);
                    if (e != null) {
                        children.add(new TreeNode(e, true));
                    }
                });
            }
        }

        public void render(MatrixStack matrices) {
            int childrenStartX = -getWidth() / 2;

            for (int i = 0; i < children.size(); i++) {
                TreeNode node = children.get(i);

                childrenStartX += (node.getWidth() + HORIZONTAL_SPACING) / 2;

                drawHook(matrices, childrenStartX + HORIZONTAL_SPACING / 2, VERTICAL_SPACING);

                matrices.push();
                matrices.translate(childrenStartX + HORIZONTAL_SPACING / 2, VERTICAL_SPACING, 0);
                node.render(matrices);
                matrices.pop();

                childrenStartX += (node.getWidth() + HORIZONTAL_SPACING) / 2;
            }

            renderTooltip(matrices, label, -12 - labelWidth / 2, 12);

            if (spouse != null) {
                matrices.push();
                matrices.translate(-spouse.getWidth() - this.labelWidth + 5, 0, 0);
                drawHorizontalLine(matrices, 0, spouse.getWidth(), 3, 0xffffffff);
                spouse.render(matrices);
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
                labelWidth = label.stream().mapToInt(textRenderer::getWidth).max().orElse(0);
                width = Math.max(labelWidth + 10, children.stream().mapToInt(TreeNode::getWidth).sum()) + (HORIZONTAL_SPACING / 2);
                if (spouse != null) {
                    width += spouse.getWidth() + HORIZONTAL_SPACING;
                }
            }
            return width;
        }
    }
}
