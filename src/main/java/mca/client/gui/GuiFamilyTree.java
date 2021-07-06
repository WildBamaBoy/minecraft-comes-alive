package mca.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mca.cobalt.network.NetworkHandler;
import mca.entity.data.FamilyTreeEntry;
import mca.network.GetFamilyTreeRequest;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiFamilyTree extends Screen {
    private final List<Line> lines = new LinkedList<>();
    private UUID uuid;
    private Map<UUID, FamilyTreeEntry> family;
    public GuiFamilyTree(UUID uuid) {
        super(new StringTextComponent("Family Tree"));

        this.uuid = uuid;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new GetFamilyTreeRequest(uuid));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack transform, int w, int h, float scale) {
        renderBackground(transform);

        drawCenteredString(transform, this.font, this.title, this.width / 2, 10, 16777215);

        for (Line l : lines) {
            l.render(transform);
        }

        super.render(transform, w, h, scale);
    }

    public void setFamilyData(UUID uuid, Map<UUID, FamilyTreeEntry> family) {
        this.uuid = uuid;
        this.family = family;

        rebuildTree();
    }

    private void addButton(UUID uuid, int x, int y, int ox, int oy) {
        if (family.containsKey(uuid)) {
            lines.add(new Line(false, x, ox, (y + oy) / 2));
            lines.add(new Line(true, x, y, (y + oy) / 2));
            lines.add(new Line(true, ox, oy, (y + oy) / 2));
            addButton(uuid, x, y);
        }
    }

    private void addButton(UUID uuid, int x, int y) {
        FamilyTreeEntry e = family.get(uuid);
        if (e != null) {
            addButton(new Button(
                    x - 40, y - 10, 80, 20,
                    new StringTextComponent(e.getName()),
                    (b) -> NetworkHandler.sendToServer(new GetFamilyTreeRequest(uuid)))
            );
        }
    }

    private void rebuildTree() {
        buttons.clear();
        lines.clear();

        int w = 100;
        int h = 40;
        int offset = height / 2 + h / 2;

        //self
        addButton(uuid, width / 2, offset);

        //parents
        UUID father = family.get(uuid).getFather();
        UUID mother = family.get(uuid).getMother();
        addButton(father, width / 2 - w, offset - h, width / 2, offset);
        addButton(mother, width / 2 + w, offset - h, width / 2, offset);

        //grand parents
        if (family.containsKey(father)) {
            addButton(family.get(father).getFather(), width / 2 - w - w / 2, offset - h * 2, width / 2 - w, offset - h);
            addButton(family.get(father).getMother(), width / 2 - w + w / 2, offset - h * 2, width / 2 - w, offset - h);
        }
        if (family.containsKey(mother)) {
            addButton(family.get(mother).getFather(), width / 2 + w - w / 2, offset - h * 2, width / 2 + w, offset - h);
            addButton(family.get(mother).getMother(), width / 2 + w + w / 2, offset - h * 2, width / 2 + w, offset - h);
        }

        //children
        List<UUID> children = family.get(uuid).getChildren();
        if (children.size() > 0) {
            w = w * 4 / children.size();
            int x = width / 2 - w * (children.size() - 1) / 2;
            for (UUID child : children) {
                addButton(child, x, offset + h, width / 2, offset);
                x += w;
            }
        }
    }

    private class Line {
        boolean vertical;
        int x, y, z;

        public Line(boolean vertical, int x, int y, int z) {
            this.vertical = vertical;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void render(MatrixStack transform) {
            if (vertical) {
                vLine(transform, x, y, z, 0xffffffff);
            } else {
                hLine(transform, x, y, z, 0xffffffff);
            }
        }
    }
}
