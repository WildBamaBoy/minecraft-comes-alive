package mca.client.gui;

import cobalt.network.NetworkHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import mca.entity.data.Building;
import mca.entity.data.Village;
import mca.network.GetVillageRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

        addButton(new Button(width / 2 - 32, height / 2 + 96, 64, 20, new StringTextComponent("Exit"), (b) -> {
            Minecraft.getInstance().setScreen(null);
        }));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void rectangle(MatrixStack transform, int x0, int y0, int x1, int y1, int color) {
        hLine(transform, x0, y0, x1, color);
        hLine(transform, x0, y1, x1, color);
        vLine(transform, x0, y0, y1, color);
        vLine(transform, x1, y0, y1, color);
    }

    @Override
    public void render(MatrixStack transform, int sizeX, int sizeY, float offset) {
        renderBackground(transform);

        drawCenteredString(transform, font, village == null ? "loading" : village.getName(), width / 2, height / 2 - 110, 0xffffffff);

        rectangle(transform, 100, 100, 200, 200, 0xffffffff);

        if (village != null) {
            transform.pushPose();

            double ox = width / 2.0 - village.getCenter().getX();
            double oy = height / 2.0 - village.getCenter().getZ();
            transform.translate(ox, oy, 0);

            int mouseX = (int) (minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getWidth() + ox);
            int mouseY = (int) (minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getHeight() + oy);

            for (Building building : village.getBuildings().values()) {
                BlockPos p0 = building.getPos0();
                BlockPos p1 = building.getPos1();
                rectangle(transform, p0.getX(), p0.getZ(), p1.getX(), p1.getZ(), 0xffffff);


                if (mouseX >= p0.getX() && mouseX <= p0.getX() && mouseY >= p0.getZ() && mouseY <= p0.getZ()) {
                    renderTooltip(transform, new StringTextComponent("Dis is building"), mouseX, mouseY);
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