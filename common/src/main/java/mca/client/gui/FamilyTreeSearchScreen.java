package mca.client.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import mca.cobalt.network.NetworkHandler;
import mca.network.FamilyTreeUUIDLookup;
import mca.resources.data.SerializablePair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class FamilyTreeSearchScreen extends Screen {
    static int DATA_WIDTH = 120;

    private List<SerializablePair<UUID, SerializablePair<String, String>>> list = new LinkedList<>();
    private ButtonWidget buttonPage;
    private int pageNumber;

    private UUID selectedVillager;

    private int mouseX;
    private int mouseY;

    public FamilyTreeSearchScreen() {
        super(new TranslatableText("gui.family_tree.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        TextFieldWidget field = addButton(new TextFieldWidget(this.textRenderer, width / 2 - DATA_WIDTH / 2, height / 2 - 80, DATA_WIDTH, 18, new TranslatableText("structure_block.structure_name")));
        field.setMaxLength(32);
        field.setChangedListener(this::searchVillager);
        field.setTextFieldFocused(true);
        setFocused(field);

        addButton(new ButtonWidget(width / 2 - 44, height / 2 + 82, 88, 20, new TranslatableText("gui.done"), sender -> {
            onClose();
        }));

        addButton(new ButtonWidget(width / 2 - 24 - 20, height / 2 + 60, 20, 20, new LiteralText("<"), (b) -> {
            if (pageNumber > 0) {
                pageNumber--;
            }
        }));
        addButton(new ButtonWidget(width / 2 + 24, height / 2 + 60, 20, 20, new LiteralText(">"), (b) -> {
            if (pageNumber < Math.ceil(list.size() / 9.0) - 1) {
                pageNumber++;
            }
        }));
        buttonPage = addButton(new ButtonWidget(width / 2 - 24, height / 2 + 60, 48, 20, new LiteralText("0/0)"), (b) -> {
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        assert client != null;
        this.mouseX = (int)(client.mouse.getX() * width / client.getWindow().getFramebufferWidth());
        this.mouseY = (int)(client.mouse.getY() * height / client.getWindow().getFramebufferHeight());

        fill(matrices, width / 2 - DATA_WIDTH / 2 - 10, height / 2 - 110, width / 2 + DATA_WIDTH / 2 + 10, height / 2 + 110, 0x66000000);

        renderBackground(matrices);

        renderVillagers(matrices);

        drawCenteredText(matrices, textRenderer, new TranslatableText("gui.title.family_tree"), width / 2, height / 2 - 100, 16777215);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderVillagers(MatrixStack transform) {
        int maxPages = (int)Math.ceil(list.size() / 9.0);
        buttonPage.setMessage(new LiteralText((pageNumber + 1) + "/" + maxPages));

        selectedVillager = null;
        for (int i = 0; i < 9; i++) {
            int index = i + pageNumber * 9;
            if (index < list.size()) {
                int y = height / 2 - 52 + i * 12;
                boolean hover = isMouseWithin(width / 2 - 50, y - 1, 100, 12);
                SerializablePair<UUID, SerializablePair<String, String>> pair = list.get(index);
                String left = pair.getRight().getLeft();
                String right = pair.getRight().getRight();

                Text text;
                if (left.isEmpty() && right.isEmpty()) {
                    text = new TranslatableText("gui.family_tree.child_of_0");
                } else if (left.isEmpty()) {
                    text = new TranslatableText("gui.family_tree.child_of_1", right);
                } else if (right.isEmpty()) {
                    text = new TranslatableText("gui.family_tree.child_of_1", left);
                } else {
                    text = new TranslatableText("gui.family_tree.child_of_2", left, right);
                }

                drawCenteredText(transform, textRenderer, text, width / 2, y, hover ? 0xFFD7D784 : 0xFFFFFFFF);
                if (hover) {
                    selectedVillager = pair.getLeft();
                }
            } else {
                break;
            }
        }
    }

    private void searchVillager(String v) {
        if (!v.isEmpty()) {
            NetworkHandler.sendToServer(new FamilyTreeUUIDLookup(v));
        }
    }

    public void setList(List<SerializablePair<UUID, SerializablePair<String, String>>> list) {
        this.list = list;
    }

    protected boolean isMouseWithin(int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedVillager != null) {
            MinecraftClient.getInstance().openScreen(new FamilyTreeScreen(selectedVillager));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
