package mca.client.gui;

import cobalt.network.NetworkHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import mca.core.MCA;
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
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class GuiBlueprint extends Screen {
    private Village village;
    private int reputation;

    //gui element Y positions
    private final int positionTaxes = -50;
    private final int positionBirth = 0;
    private final int positionMarriage = 50;

    private final int fromCenter = 150;

    private Button buttonTaxes;
    private Button buttonBirths;
    private Button buttonMarriage;

    public GuiBlueprint() {
        super(new StringTextComponent("Blueprint"));
    }

    @Override
    public void tick() {
        super.tick();
    }

    private void changeTaxes(int d) {
        village.setTaxes(Math.max(0, Math.min(100, village.getTaxes() + d)));
    }

    private void changePopulationThreshold(int d) {
        village.setPopulationThreshold(Math.max(0, Math.min(100, village.getPopulationThreshold() + d)));
    }

    private void changeMarriageThreshold(int d) {
        village.setMarriageThreshold(Math.max(0, Math.min(100, village.getMarriageThreshold() + d)));
    }

    private Button createValueChanger(int x, int y, int w, int h, Consumer<Boolean> onPress) {
        addButton(new Button(x - w / 5 * 3, y, w / 5, h,
                new StringTextComponent("<<"), (b) -> onPress.accept(false)));

        addButton(new Button(x + w / 5 * 2, y, w / 5, h,
                new StringTextComponent(">>"), (b) -> onPress.accept(true)));

        return addButton(new Button(x - w / 5 * 2, y, w / 5 * 4, h,
                new StringTextComponent(""), (b) -> {
        }));
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new GetVillageRequest());

        addButton(new Button(width / 2 - 32, height / 2 + 96, 64, 20, new StringTextComponent("Exit"), (b) -> Minecraft.getInstance().setScreen(null)));

        //taxes
        buttonTaxes = createValueChanger(width / 2 + fromCenter, height / 2 + positionTaxes + 20, 100, 20, (b) -> {
            changeTaxes(b ? 10 : -10);
        });

        //birth theshold
        buttonBirths = createValueChanger(width / 2 + fromCenter, height / 2 + positionBirth + 20, 100, 20, (b) -> {
            changePopulationThreshold(b ? 10 : -10);
        });

        //marriage theshold
        buttonMarriage = createValueChanger(width / 2 + fromCenter, height / 2 + positionMarriage + 20, 100, 20, (b) -> {
            changeMarriageThreshold(b ? 10 : -10);
        });
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

        //update text
        buttonTaxes.setMessage(new StringTextComponent(village.getTaxes() + "%"));
        buttonBirths.setMessage(new StringTextComponent(village.getPopulationThreshold() + "%"));
        buttonMarriage.setMessage(new StringTextComponent(village.getMarriageThreshold() + "%"));

        //name
        drawCenteredString(transform, font, village == null ? "loading" : village.getName(), width / 2, height / 2 - 110, 0xffffffff);
        if (village != null) {
            drawCenteredString(transform, font, "Buildings: " + village.getBuildings().size(), width / 2, -80, 0xffffffff);
            drawCenteredString(transform, font, "Population: " + village.getPopulation() + " of " + village.getMaxPopulation(), width / 2, -60, 0xffffffff);
        }

        //rank
        if (village != null) {
            int rank = village.getRank(reputation);
            String rankStr = MCA.localize("gui.village.rank" + rank);
            int rankColor = rank == 0 ? 0xffff0000 : 0xffffff00;
            drawCenteredString(transform, font, MCA.localize("gui.village.rank", rankStr), width / 2 - fromCenter, 100, rankColor);
            drawCenteredString(transform, font, MCA.localize("gui.village.reputation", String.valueOf(reputation)), width / 2 - fromCenter, 120, rank == 0 ? 0xffff0000 : 0xffffffff);

            //taxes
            drawCenteredString(transform, font, MCA.localize("gui.village.taxes"), width / 2 + fromCenter, height / 2 + positionTaxes, 0xffffffff);
            drawCenteredString(transform, font, MCA.localize("gui.village.rankTooLow"), width / 2 + fromCenter, height / 2 + positionTaxes + 20, 0xffffffff);

            drawCenteredString(transform, font, MCA.localize("gui.village.birth"), width / 2 + fromCenter, height / 2 + positionBirth, 0xffffffff);
            drawCenteredString(transform, font, MCA.localize("gui.village.rankTooLow"), width / 2 + fromCenter, height / 2 + positionBirth + 20, 0xffffffff);

            drawCenteredString(transform, font, MCA.localize("gui.village.marriage"), width / 2 + fromCenter, height / 2 + positionMarriage, 0xffffffff);
            drawCenteredString(transform, font, MCA.localize("gui.village.rankTooLow"), width / 2 + fromCenter, height / 2 + positionMarriage + 20, 0xffffffff);
        }


        //map
        drawCenteredString(transform, font, "map", width / 2, height / 2 - 90, 0x88ffffff);
        rectangle(transform, width / 2 - 75, height / 2 - 75, width / 2 + 75, height / 2 + 75, 0xffffff88);

        if (village != null && minecraft != null) {
            transform.pushPose();

            double ox = width / 2.0 - village.getCenter().getX();
            double oy = height / 2.0 - village.getCenter().getZ();

            //center and scale the map
            transform.scale(4096.0f / village.getSize(), 4096.0f / village.getSize(), 0.0f);
            transform.translate(ox, oy, 0);

            //add the players lactation
            ClientPlayerEntity player = minecraft.player;
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

                    //name
                    lines.add(new StringTextComponent(building.getType().name()));

                    //debug
                    for (Map.Entry<String, Integer> block : building.getBlocks().entrySet()) {
                        lines.add(new StringTextComponent(block.getKey() + ": " + block.getValue() + "x"));
                    }

                    //render
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

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }
}