package mca.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mca.api.API;
import mca.api.cobalt.network.NetworkHandler;
import mca.api.types.BuildingType;
import mca.core.MCA;
import mca.entity.data.Building;
import mca.entity.data.Village;
import mca.enums.Rank;
import mca.network.GetVillageRequest;
import mca.network.ReportBuildingMessage;
import mca.network.SaveVillageMessage;
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
    //gui element Y positions
    private final int positionTaxes = -60;
    private final int positionBirth = -10;
    private final int positionMarriage = 40;
    private final int rankTaxes = 100;
    private final int rankBirth = 1;
    private final int rankMarriage = 1;
    private final int fromCenter = 150;
    private Village village;
    private int reputation;
    private boolean showCatalog;
    private Button[] buttonTaxes;
    private Button[] buttonBirths;
    private Button[] buttonMarriage;

    public GuiBlueprint() {
        super(new StringTextComponent("Blueprint"));
    }

    @Override
    public void tick() {
        super.tick();
    }

    private void saveVillage() {
        NetworkHandler.sendToServer(new SaveVillageMessage(village));
    }

    private void changeTaxes(int d) {
        village.setTaxes(Math.max(0, Math.min(100, village.getTaxes() + d)));
        saveVillage();
    }

    private void changePopulationThreshold(int d) {
        village.setPopulationThreshold(Math.max(0, Math.min(100, village.getPopulationThreshold() + d)));
        saveVillage();
    }

    private void changeMarriageThreshold(int d) {
        village.setMarriageThreshold(Math.max(0, Math.min(100, village.getMarriageThreshold() + d)));
        saveVillage();
    }

    private Button[] createValueChanger(int x, int y, int w, int h, Consumer<Boolean> onPress) {
        Button[] buttons = new Button[3];

        buttons[1] = addButton(new Button(x - w / 2, y, w / 4, h,
                new StringTextComponent("<<"), (b) -> onPress.accept(false)));

        buttons[2] = addButton(new Button(x + w / 4, y, w / 4, h,
                new StringTextComponent(">>"), (b) -> onPress.accept(true)));

        buttons[0] = addButton(new Button(x - w / 4, y, w / 2, h,
                new StringTextComponent(""), (b) -> {
        }));

        return buttons;
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new GetVillageRequest());

        showCatalog = false;

        addButton(new Button(width / 2 - 66, height / 2 + 80, 64, 20, new StringTextComponent("Exit"), (b) -> Minecraft.getInstance().setScreen(null)));
        addButton(new Button(width / 2 + 2, height / 2 + 80, 64, 20, new StringTextComponent("Add Building"), (b) -> {
            NetworkHandler.sendToServer(new ReportBuildingMessage());
            NetworkHandler.sendToServer(new GetVillageRequest());
        }));
        addButton(new Button(width / 2 - fromCenter - 32, height / 2 + 80, 64, 20, new StringTextComponent("Catalog"), (b) -> {
            toggleButtons(buttonTaxes, false);
            toggleButtons(buttonBirths, false);
            toggleButtons(buttonMarriage, false);
            showCatalog = !showCatalog;
        }));

        //taxes
        buttonTaxes = createValueChanger(width / 2 + fromCenter, height / 2 + positionTaxes + 10, 80, 20, (b) -> changeTaxes(b ? 10 : -10));
        toggleButtons(buttonTaxes, false);

        //birth threshold
        buttonBirths = createValueChanger(width / 2 + fromCenter, height / 2 + positionBirth + 10, 80, 20, (b) -> changePopulationThreshold(b ? 10 : -10));
        toggleButtons(buttonBirths, false);

        //marriage threshold
        buttonMarriage = createValueChanger(width / 2 + fromCenter, height / 2 + positionMarriage + 10, 80, 20, (b) -> changeMarriageThreshold(b ? 10 : -10));
        toggleButtons(buttonMarriage, false);
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

        if (showCatalog) {
            //title
            transform.pushPose();
            transform.scale(2.0f, 2.0f, 2.0f);
            drawCenteredString(transform, font, "Building Catalog", width / 4, height / 4 - 55, 0xffffffff);
            transform.popPose();

            //explanation
            drawCenteredString(transform, font, "Build special buildings by fulfilling those conditions", width / 2, height / 2 - 90, 0xffffffff);
            drawCenteredString(transform, font, "Work in Progress - you may build them but they have no effect yet", width / 2, height / 2 - 80, 0xffffffff);

            //buildings
            int row = 0;
            int col = 0;
            int w = 120;
            int h = 55;
            int spacing = 8;
            for (BuildingType bt : API.getBuildingTypes().values()) {
                if (bt.isVisible()) {
                    int x = width / 2 + fromCenter * (col - 1);
                    int y = row * (h + spacing) + 105;
                    rectangle(transform, x - w / 2, y - h / 2, x + w / 2, y + h / 2, 0x88ffffff);
                    drawCenteredString(transform, font, MCA.localize("buildingType." + bt.getName()), x, y - 24, bt.getColor());

                    //size
                    String size = bt.getSize() == 0 ? MCA.localize("gui.building.anySize") : MCA.localize("gui.building.size", String.valueOf(bt.getSize()));
                    drawCenteredString(transform, font, size, x, y - 12, 0xffdddddd);

                    //required blocks
                    int i = 0;
                    for (Map.Entry<String, Integer> b : bt.getBlocks().entrySet()) {
                        i++;
                        drawCenteredString(transform, font, b.getValue() + " x " + getBlockName(b.getKey()), x, y - 12 + 12 * i, 0xffffffff);
                    }

                    col++;
                    if (col == 3) {
                        col = 0;
                        row++;
                    }
                }
            }
        } else if (village != null && minecraft != null) {
            //name
            transform.pushPose();
            transform.scale(2.0f, 2.0f, 2.0f);
            drawCenteredString(transform, font, village.getName(), width / 4, height / 4 - 55, 0xffffffff);
            transform.popPose();

            //population
            drawCenteredString(transform, font, "Buildings: " + village.getBuildings().size(), width / 2, height / 2 - 90, 0xffffffff);
            drawCenteredString(transform, font, "Population: " + village.getPopulation() + " of " + village.getMaxPopulation(), width / 2, height / 2 - 80, 0xffffffff);

            //update text
            buttonTaxes[0].setMessage(new StringTextComponent(village.getTaxes() + "%"));
            buttonBirths[0].setMessage(new StringTextComponent(village.getPopulationThreshold() + "%"));
            buttonMarriage[0].setMessage(new StringTextComponent(village.getMarriageThreshold() + "%"));

            //rank
            Rank rank = village.getRank(reputation);
            String rankStr = MCA.localize("gui.village.rank." + rank.getId());
            int rankColor = rank.getId() == 0 ? 0xffff0000 : 0xffffff00;
            drawCenteredString(transform, font, MCA.localize("gui.village.rank", rankStr), width / 2 - fromCenter, height / 2 - 50 - 15, rankColor);
            drawCenteredString(transform, font, MCA.localize("gui.village.reputation", String.valueOf(reputation)), width / 2 - fromCenter, height / 2 - 50, rank.getId() == 0 ? 0xffff0000 : 0xffffffff);

            //tasks
            String str = MCA.localize("task.reputation", String.valueOf(rank.getReputation()));
            drawCenteredString(transform, font, str, width / 2 - fromCenter, height / 2 - 22, reputation >= rank.getReputation() ? 0xff00ff00 : 0xffff0000);
            for (int i = 0; i < Village.getTaskNames().length; i++) {
                String task = MCA.localize("task." + Village.getTaskNames()[i]);
                drawCenteredString(transform, font, task, width / 2 - fromCenter, height / 2 - 10 + i * 12, village.getTasks()[i] ? 0xff00ff00 : 0xffdddddd);
            }

            //taxes
            drawCenteredString(transform, font, MCA.localize("gui.village.taxes"), width / 2 + fromCenter, height / 2 + positionTaxes, 0xffffffff);
            if (rank.getId() < rankTaxes) {
                drawCenteredString(transform, font, MCA.localize("gui.village.taxesNotImplemented"), width / 2 + fromCenter, height / 2 + positionTaxes + 15, 0xffffffff);
                toggleButtons(buttonTaxes, false);
            } else {
                toggleButtons(buttonTaxes, true);
            }

            drawCenteredString(transform, font, MCA.localize("gui.village.birth"), width / 2 + fromCenter, height / 2 + positionBirth, 0xffffffff);
            if (rank.getId() < rankBirth) {
                drawCenteredString(transform, font, MCA.localize("gui.village.rankTooLow"), width / 2 + fromCenter, height / 2 + positionBirth + 15, 0xffffffff);
                toggleButtons(buttonBirths, false);
            } else {
                toggleButtons(buttonBirths, true);
            }

            drawCenteredString(transform, font, MCA.localize("gui.village.marriage"), width / 2 + fromCenter, height / 2 + positionMarriage, 0xffffffff);
            if (rank.getId() < rankMarriage) {
                drawCenteredString(transform, font, MCA.localize("gui.village.rankTooLow"), width / 2 + fromCenter, height / 2 + positionMarriage + 15, 0xffffffff);
                toggleButtons(buttonMarriage, false);
            } else {
                toggleButtons(buttonMarriage, true);
            }


            //map
            int mapSize = 70;
            rectangle(transform, width / 2 - mapSize, height / 2 - mapSize, width / 2 + mapSize, height / 2 + mapSize, 0xffffff88);

            transform.pushPose();

            //center and scale the map
            float sc = (float) mapSize / (village.getSize() - 24);
            transform.translate(width / 2.0, height / 2.0, 0);
            transform.scale(sc, sc, 0.0f);
            transform.translate(-village.getCenter().getX(), -village.getCenter().getZ(), 0);

            //show the players location
            ClientPlayerEntity player = minecraft.player;
            if (player != null) {
                rectangle(transform, (int) player.getX() - 1, (int) player.getZ() - 1, (int) player.getX() + 1, (int) player.getZ() + 1, 0xffff00ff);
            }

            int mouseRawX = (int) (minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getWidth());
            int mouseRawY = (int) (minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getHeight());
            int mouseX = (int) ((mouseRawX - width / 2.0) / sc + village.getCenter().getX());
            int mouseY = (int) ((mouseRawY - height / 2.0) / sc + village.getCenter().getZ());

            //buildings
            Building hoverBuilding = null;
            for (Building building : village.getBuildings().values()) {
                BlockPos p0 = building.getPos0();
                BlockPos p1 = building.getPos1();
                BuildingType bt = API.getBuildingType(building.getType());
                rectangle(transform, p0.getX(), p0.getZ(), p1.getX(), p1.getZ(), bt.getColor());

                //tooltip
                int margin = 2;
                if (mouseX >= p0.getX() - margin && mouseX <= p1.getX() + margin && mouseY >= p0.getZ() - margin && mouseY <= p1.getZ() + margin) {
                    hoverBuilding = building;
                }
            }

            transform.popPose();

            if (hoverBuilding != null) {
                List<ITextComponent> lines = new LinkedList<>();

                //name
                BuildingType bt = API.getBuildingType(hoverBuilding.getType());
                lines.add(MCA.localizeText("buildingType." + bt.getName()));
                lines.add(MCA.localizeText("gui.building.size", String.valueOf(hoverBuilding.getSize())));

                //residents
                for (String name : hoverBuilding.getResidents().values()) {
                    lines.add(new StringTextComponent(name));
                }

                //present blocks
                for (Map.Entry<String, Integer> block : hoverBuilding.getBlocks().entrySet()) {
                    lines.add(new StringTextComponent(block.getValue() + " x " + getBlockName(block.getKey())));
                }

                //render
                renderComponentTooltip(transform, lines, mouseRawX, mouseRawY);
            }
        }

        super.render(transform, sizeX, sizeY, offset);
    }

    private String getBlockName(String key) {
        //dis some hacking, no time to fix tho
        return MCA.localize("block." + key.replace(":", "."));
    }

    private void toggleButtons(Button[] buttons, boolean active) {
        for (Button b : buttons) {
            b.active = active;
            b.visible = active;
        }
    }

    public void setVillage(Village village) {
        this.village = village;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }
}