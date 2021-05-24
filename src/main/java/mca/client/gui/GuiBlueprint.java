package mca.client.gui;

import cobalt.network.NetworkHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import mca.entity.data.Building;
import mca.entity.data.Village;
import mca.network.GetVillageRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class GuiBlueprint extends Screen {
    private Village village;

    public GuiBlueprint() {
        super(new StringTextComponent("Blueprint"));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new GetVillageRequest());

        addButton(new Button(width / 2 - 32, height / 2 + 96, 64, 20, new StringTextComponent("Exit"), (b) -> Minecraft.getInstance().setScreen(null)));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void rectangle(MatrixStack transform, int x0, int y0, int x1, int y1, int color) {
        hLine(transform, x0, x1, y0, color);
        hLine(transform, x0, x1, y1, color);
        vLine(transform, x0, y0, y1, color);
        vLine(transform, x1, y0, y1, color);
    }

    @Override
    public void render(MatrixStack transform, int sizeX, int sizeY, float offset) {
        renderBackground(transform);

        int fromCenter = 150;

        drawCenteredString(transform, font, village == null ? "loading" : village.getName(), width / 2, height / 2 - 110, 0xffffffff);
        drawCenteredString(transform, font, "map", width / 2, height / 2 - 90, 0x88ffffff);

        drawCenteredString(transform, font, "Rank: King", width / 2 - fromCenter, 100, 0xffffffff);
        drawCenteredString(transform, font, "Reputation: 30", width / 2 - fromCenter, 120, 0xffffffff);

        if (village != null) {
            drawCenteredString(transform, font, "Buildings: " + village.getBuildings().size(), width / 2 - fromCenter, 160, 0xffffffff);
            drawCenteredString(transform, font, "Population: a lot lol", width / 2 - fromCenter, 180, 0xffffffff);
        }


        drawCenteredString(transform, font, "Taxes", width / 2 + fromCenter, 50, 0xffffffff);
        drawCenteredString(transform, font, "you need a storage first", width / 2 + fromCenter, 70, 0xffffffff);

        drawCenteredString(transform, font, "Guards", width / 2 + fromCenter, 100, 0xffffffff);
        drawCenteredString(transform, font, "you need an armory first", width / 2 + fromCenter, 120, 0xffffffff);

        drawCenteredString(transform, font, "Birth Control", width / 2 + fromCenter, 150, 0xffffffff);
        drawCenteredString(transform, font, "you need an infirmary first", width / 2 + fromCenter, 170, 0xffffffff);

        drawCenteredString(transform, font, "Marriage Limit", width / 2 + fromCenter, 200, 0xffffffff);
        drawCenteredString(transform, font, "you need a church first", width / 2 + fromCenter, 220, 0xffffffff);


        rectangle(transform, width / 2 - 75, height / 2 - 75, width / 2 + 75, height / 2 + 75, 0xffffff88);

        if (village != null) {
            transform.pushPose();

            double ox = width / 2.0 - village.getCenter().getX();
            double oy = height / 2.0 - village.getCenter().getZ();

            transform.scale(4096.0f / village.getSize(), 4096.0f / village.getSize(), 0.0f);
            transform.translate(ox, oy, 0);

            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                rectangle(transform, (int) player.getX() - 1, (int) player.getZ() - 1, (int) player.getX() + 1, (int) player.getZ() + 1, 0xffff00ff);
            }

            int mouseX = (int) (minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getWidth() - ox) + 1;
            int mouseY = (int) (minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getHeight() - oy) + 1;

            for (Building building : village.getBuildings().values()) {
                BlockPos p0 = building.getPos0();
                BlockPos p1 = building.getPos1();
                rectangle(transform, p0.getX(), p0.getZ(), p1.getX(), p1.getZ(), 0xffffffff);

                //tooltip
                int margin = 4;
                if (mouseX >= p0.getX() - margin && mouseX <= p1.getX() + margin && mouseY >= p0.getZ() - margin && mouseY <= p1.getZ() + margin) {
                    List<ITextComponent> lines = new LinkedList<>();
                    lines.add(new StringTextComponent(building.getType().name()));

                    for (Map.Entry<String, Integer> block : building.getBlocks().entrySet()) {
                        lines.add(new StringTextComponent(block.getKey() + ": " + block.getValue() + "x"));
                    }

                    renderComponentTooltip(transform, lines, mouseX, mouseY);
                }
            }

            transform.popPose();
        }

        super.render(transform, sizeX, sizeY, offset);
    }

    public void setVillage(Village village) {
        this.village = village;
    }
}