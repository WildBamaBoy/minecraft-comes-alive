package mca.client.gui;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import mca.client.gui.widget.RectangleWidget;
import mca.cobalt.network.NetworkHandler;
import mca.resources.Rank;
import mca.network.GetVillageRequest;
import mca.network.ReportBuildingMessage;
import mca.network.SaveVillageMessage;
import mca.resources.data.BuildingType;
import mca.server.world.data.Building;
import mca.server.world.data.Village;
import mca.resources.data.tasks.Task;
import mca.util.compat.RenderSystemCompat;
import mca.util.localization.FlowingText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.util.registry.Registry;

public class BlueprintScreen extends Screen {
    //gui element Y positions
    private final int positionTaxes = -60;
    private final int positionBirth = -10;
    private final int positionMarriage = 40;
    private Village village;
    private int reputation;
    private Rank rank;
    private Set<String> completedTasks;
    private String page;
    private ButtonWidget[] buttonTaxes;
    private ButtonWidget[] buttonBirths;
    private ButtonWidget[] buttonMarriage;
    private ButtonWidget buttonPage;
    private int pageNumber = 0;
    private final List<ButtonWidget> catalogButtons = new LinkedList<>();

    private static final Identifier ICON_TEXTURES = new Identifier("mca:textures/buildings.png");
    private BuildingType selectedBuilding;
    private UUID selectedVillager;

    private int mouseX;
    private int mouseY;

    private Map<Rank, List<Task>> tasks;
    private Map<String, BuildingType> buildingTypes;

    public BlueprintScreen() {
        super(new LiteralText("Blueprint"));
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

    private ButtonWidget[] createValueChanger(int x, int y, int w, int h, Consumer<Boolean> onPress) {
        ButtonWidget[] buttons = new ButtonWidget[3];

        buttons[1] = addButton(new ButtonWidget(x - w / 2, y, w / 4, h,
                new LiteralText("<<"), (b) -> onPress.accept(false)));

        buttons[2] = addButton(new ButtonWidget(x + w / 4, y, w / 4, h,
                new LiteralText(">>"), (b) -> onPress.accept(true)));

        buttons[0] = addButton(new ButtonWidget(x - w / 4, y, w / 2, h,
                new LiteralText(""), (b) -> {
        }));

        return buttons;
    }

    protected void drawBuildingIcon(MatrixStack transform, int x, int y, int u, int v) {
        transform.push();
        transform.translate(x - 6.6, y - 6.6, 0);
        transform.scale(0.66f, 0.66f, 0.66f);
        this.drawTexture(transform, 0, 0, u, v, 20, 20);
        transform.pop();
    }

    @Override
    public void init() {
        NetworkHandler.sendToServer(new GetVillageRequest());
        setPage("waiting");
    }

    private void setPage(String page) {
        if (page.equals("close")) {
            MinecraftClient.getInstance().openScreen(null);
            return;
        }

        this.page = page;

        buttons.clear();
        this.children.clear();

        //page selection
        int bx = width / 2 - 180;
        int by = height / 2 - 56;
        if (!page.equals("empty") && !page.equals("waiting")) {
            for (String p : new String[] {"map", "rank", "catalog", "villagers", "rules", "close"}) {
                ButtonWidget widget = new ButtonWidget(bx, by, 64, 20, new TranslatableText("gui.blueprint." + p), (b) -> setPage(p));
                addButton(widget);
                if (page.equals(p)) {
                    widget.active = false;
                }
                by += 22;
            }
        }

        switch (page) {
            case "map":
                //add building
                bx = width / 2 + 180 - 64 - 16;
                by = height / 2 - 56 + 22 * 3;
                addButton(new ButtonWidget(bx, by, 96, 20, new TranslatableText("gui.blueprint.addBuilding"), (b) -> {
                    NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.ADD));
                    NetworkHandler.sendToServer(new GetVillageRequest());
                }));
                by += 22;

                //remove building
                addButton(new ButtonWidget(bx, by, 96, 20, new TranslatableText("gui.blueprint.removeBuilding"), (b) -> {
                    NetworkHandler.sendToServer(new ReportBuildingMessage(ReportBuildingMessage.Action.REMOVE));
                    NetworkHandler.sendToServer(new GetVillageRequest());
                }));
                break;
            case "rank":
                break;
            case "catalog":
                //list catalog button
                int row = 0;
                int col = 0;
                int size = 21;
                int x = width / 2 - 4 * size - 8;
                int y = (int)(height / 2 - 2.0 * size);
                catalogButtons.clear();
                for (BuildingType bt : buildingTypes.values()) {
                    if (bt.visible()) {
                        TexturedButtonWidget widget = new TexturedButtonWidget(
                                row * size + x - 10, col * size + y - 10, 20, 20, bt.iconU(), bt.iconV() + 20, 20, ICON_TEXTURES, 256, 256, button -> {
                            selectBuilding(bt);
                            button.active = false;
                            catalogButtons.forEach(b -> b.active = true);
                        }, new TranslatableText("buildingType." + bt.name()));
                        catalogButtons.add(addButton(widget));

                        row++;
                        if (row > 4) {
                            row = 0;
                            col++;
                        }
                    }
                }
                break;
            case "villagers":
                addButton(new ButtonWidget(width / 2 - 24 - 20, height / 2 + 54, 20, 20, new LiteralText("<"), (b) -> {
                    if (pageNumber > 0) {
                        pageNumber--;
                    }
                }));
                addButton(new ButtonWidget(width / 2 + 24, height / 2 + 54, 20, 20, new LiteralText(">"), (b) -> {
                    if (pageNumber < Math.ceil(village.getPopulation() / 9.0) - 1) {
                        pageNumber++;
                    }
                }));
                buttonPage = addButton(new ButtonWidget(width / 2 - 24, height / 2 + 54, 48, 20, new LiteralText("0/0)"), (b) -> {
                }));
                break;
            case "rules":
                //taxes
                buttonTaxes = createValueChanger(width / 2, height / 2 + positionTaxes + 10, 80, 20, (b) -> changeTaxes(b ? 10 : -10));
                toggleButtons(buttonTaxes, false);

                //birth threshold
                buttonBirths = createValueChanger(width / 2, height / 2 + positionBirth + 10, 80, 20, (b) -> changePopulationThreshold(b ? 10 : -10));
                toggleButtons(buttonBirths, false);

                //marriage threshold
                buttonMarriage = createValueChanger(width / 2, height / 2 + positionMarriage + 10, 80, 20, (b) -> changeMarriageThreshold(b ? 10 : -10));
                toggleButtons(buttonMarriage, false);
                break;
        }
    }

    private void selectBuilding(BuildingType b) {
        selectedBuilding = b;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack transform, int sizeX, int sizeY, float offset) {
        renderBackground(transform);

        this.mouseX = (int)(client.mouse.getX() * width / client.getWindow().getFramebufferWidth());
        this.mouseY = (int)(client.mouse.getY() * height / client.getWindow().getFramebufferHeight());

        switch (page) {
            case "waiting":
                drawCenteredText(transform, textRenderer, new TranslatableText("gui.blueprint.waiting"), width / 2, height / 2, 0xffaaaaaa);
                break;
            case "empty":
                drawCenteredText(transform, textRenderer, new TranslatableText("gui.blueprint.empty"), width / 2, height / 2, 0xffaaaaaa);
                break;
            case "map":
                renderName(transform);
                renderMap(transform);
                renderStats(transform);
                break;
            case "rank":
                renderTasks(transform);
                renderStats(transform);
                break;
            case "catalog":
                renderCatalog(transform);
                break;
            case "villagers":
                renderVillagers(transform);
                break;
            case "rules":
                renderRules(transform);
                break;
        }

        super.render(transform, sizeX, sizeY, offset);
    }

    private void renderName(MatrixStack transform) {
        //name
        transform.push();
        transform.scale(2.0f, 2.0f, 2.0f);
        drawCenteredText(transform, textRenderer, village.getName(), width / 4, height / 4 - 48, 0xffffffff);
        transform.pop();
    }

    private void renderStats(MatrixStack transform) {
        int x = width / 2 + (page.equals("rank") ? -70 : 105);
        int y = height / 2 - 50;

        //rank
        Text rankStr = new TranslatableText("gui.village.rank." + rank.ordinal());
        int rankColor = rank.ordinal() == 0 ? 0xffff0000 : 0xffffff00;

        textRenderer.drawWithShadow(transform, new TranslatableText("gui.blueprint.currentRank", rankStr), x, y, rankColor);
        textRenderer.drawWithShadow(transform, new TranslatableText("gui.blueprint.reputation", String.valueOf(reputation)), x, y + 11, rank.ordinal() == 0 ? 0xffff0000 : 0xffffffff);
        textRenderer.drawWithShadow(transform, new TranslatableText("gui.blueprint.buildings", village.getBuildings().size()), x, y + 22, 0xffffffff);
        textRenderer.drawWithShadow(transform, new TranslatableText("gui.blueprint.population", village.getPopulation(), village.getMaxPopulation()), x, y + 33, 0xffffffff);
    }

    private void renderMap(MatrixStack transform) {
        int mapSize = 75;
        int y = height / 2 + 8;
        RectangleWidget.drawRectangle(transform, width / 2 - mapSize, y - mapSize, width / 2 + mapSize, y + mapSize, 0xffffff88);

        transform.push();

        RenderSystemCompat.setShaderTexture(0, ICON_TEXTURES);

        //center and scale the map
        float sc = (float)mapSize / village.getSize();
        int mouseLocalX = (int)((mouseX - width / 2.0) / sc + village.getCenter().getX());
        int mouseLocalY = (int)((mouseY - y) / sc + village.getCenter().getZ());
        transform.translate(width / 2.0, y, 0);
        transform.scale(sc, sc, 0.0f);
        transform.translate(-village.getCenter().getX(), -village.getCenter().getZ(), 0);

        //show the players location
        ClientPlayerEntity player = client.player;
        if (player != null) {
            RectangleWidget.drawRectangle(transform, (int)player.getX() - 1, (int)player.getZ() - 1, (int)player.getX() + 1, (int)player.getZ() + 1, 0xffff00ff);
        }

        //buildings
        Building hoverBuilding = null;
        for (Building building : village.getBuildings().values()) {
            BuildingType bt = buildingTypes.get(building.getType());

            if (bt.isIcon()) {
                BlockPos c = building.getCenter();
                drawBuildingIcon(transform, c.getX(), c.getZ(), bt.iconU(), bt.iconV());

                //tooltip
                int margin = 6;
                if (c.getSquaredDistance(new Vec3i(mouseLocalX, c.getY(), mouseLocalY)) < margin * margin) {
                    hoverBuilding = building;
                }
            } else {
                BlockPos p0 = building.getPos0();
                BlockPos p1 = building.getPos1();
                RectangleWidget.drawRectangle(transform, p0.getX(), p0.getZ(), p1.getX(), p1.getZ(), bt.getColor());

                //icon
                if (bt.visible()) {
                    BlockPos c = building.getCenter();
                    drawBuildingIcon(transform, c.getX(), c.getZ(), bt.iconU(), bt.iconV());
                }

                //tooltip
                int margin = 2;
                if (mouseLocalX >= p0.getX() - margin && mouseLocalX <= p1.getX() + margin && mouseLocalY >= p0.getZ() - margin && mouseLocalY <= p1.getZ() + margin) {
                    hoverBuilding = building;
                }
            }
        }

        transform.pop();

        if (hoverBuilding != null) {
            List<Text> lines = new LinkedList<>();

            //name
            BuildingType bt = buildingTypes.get(hoverBuilding.getType());
            lines.add(new TranslatableText("buildingType." + bt.name()));
            lines.add(new TranslatableText("gui.blueprint.size", String.valueOf(hoverBuilding.getSize())));

            //residents
            for (String name : hoverBuilding.getResidents().values()) {
                lines.add(new LiteralText(name));
            }

            //pois
            if (hoverBuilding.getPois().size() > 0) {
                lines.add(new LiteralText(hoverBuilding.getPois().size() + " pois").formatted(Formatting.GRAY));
            }

            //present blocks
            for (Map.Entry<Identifier, Integer> block : hoverBuilding.getBlocks().entrySet()) {
                lines.add(new LiteralText(block.getValue() + " x ").append(getBlockName(block.getKey())).formatted(Formatting.GRAY));
            }

            //render
            renderTooltip(transform, lines, mouseX, mouseY);
        }
    }

    private void renderTasks(MatrixStack transform) {
        if (rank == null) {
            return;
        }

        int y = height / 2 + 5;
        int x = width / 2 - 70;

        //tasks
        for (Task task : tasks.get(rank.promote())) {
            boolean completed = completedTasks.contains(task.getId());
            Text t = task.getTranslatable().formatted(completed ? Formatting.STRIKETHROUGH : Formatting.RESET);
            textRenderer.drawWithShadow(transform, t, x, y, completed ? 0xff88ff88 : 0xffff5555);
            y += 11;
        }
    }

    private void renderCatalog(MatrixStack transform) {
        //title
        transform.push();
        transform.scale(2.0f, 2.0f, 2.0f);
        drawCenteredText(transform, textRenderer, "Building Catalog", width / 4, height / 4 - 52, 0xffffffff);
        transform.pop();

        //explanation
        drawCenteredText(transform, textRenderer, new TranslatableText("Build special buildings by fulfilling those conditions!").formatted(Formatting.GRAY), width / 2, height / 2 - 82, 0xffffffff);

        //building
        int x = width / 2 + 15;
        int y = height / 2 - 50;
        if (selectedBuilding != null) {
            //name
            textRenderer.drawWithShadow(transform, new TranslatableText("buildingType." + selectedBuilding.name()), x, y, selectedBuilding.getColor());

            //description
            List<Text> wrap = FlowingText.wrap(new TranslatableText("buildingType." + selectedBuilding.name() + ".description").formatted(Formatting.GRAY).formatted(Formatting.ITALIC), 150);
            for (Text t : wrap) {
                textRenderer.drawWithShadow(transform, t, x, y + 12, 0xffffffff);
                y += 10;
            }

            //size
            Text size = selectedBuilding.size() == 0 ? new TranslatableText("gui.blueprint.anySize") : new TranslatableText("gui.blueprint.size", String.valueOf(selectedBuilding.size()));
            textRenderer.drawWithShadow(transform, size, x, y + 20, 0xffdddddd);

            //required blocks
            for (Map.Entry<Identifier, Integer> b : selectedBuilding.blockIds().entrySet()) {
                textRenderer.drawWithShadow(transform, new LiteralText(b.getValue() + " x ").append(getBlockName(b.getKey())), x, y + 32, 0xffffffff);
                y += 10;
            }
        } else {
            //help
            List<Text> wrap = FlowingText.wrap(new TranslatableText("gui.blueprint.buildingTypes").formatted(Formatting.GRAY).formatted(Formatting.ITALIC), 150);
            for (Text t : wrap) {
                textRenderer.drawWithShadow(transform, t, x, y, 0xffffffff);
                y += 10;
            }
        }
    }

    private void renderVillagers(MatrixStack transform) {
        int maxPages = (int)Math.ceil(village.getPopulation() / 9.0);
        buttonPage.setMessage(new LiteralText((pageNumber + 1) + "/" + maxPages));

        List<Map.Entry<UUID, String>> villager = village.getBuildings().values().stream()
                .flatMap(b -> b.getResidents().entrySet().stream())
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());

        selectedVillager = null;
        for (int i = 0; i < 9; i++) {
            int index = i + pageNumber * 9;
            if (index < villager.size()) {
                int y = height / 2 - 51 + i * 11;
                boolean hover = isMouseWithin(width / 2 - 50, y - 1, 100, 11);
                drawCenteredText(transform, textRenderer, new LiteralText(villager.get(index).getValue()), width / 2, y, hover ? 0xFFD7D784 : 0xFFFFFFFF);
                if (hover) {
                    selectedVillager = villager.get(index).getKey();
                }
            } else {
                break;
            }
        }
    }

    private void renderRules(MatrixStack transform) {
        buttonTaxes[0].setMessage(new LiteralText(village.getTaxes() + "%"));
        buttonMarriage[0].setMessage(new LiteralText(village.getMarriageThreshold() + "%"));
        buttonBirths[0].setMessage(new LiteralText(village.getPopulationThreshold() + "%"));

        //taxes
        drawCenteredText(transform, textRenderer, new TranslatableText("gui.blueprint.taxes"), width / 2, height / 2 + positionTaxes, 0xffffffff);
        if (!rank.isAtLeast(Rank.MERCHANT)) {
            drawCenteredText(transform, textRenderer, new TranslatableText("gui.blueprint.rankTooLow"), width / 2, height / 2 + positionTaxes + 15, 0xffffffff);
            toggleButtons(buttonTaxes, false);
        } else {
            toggleButtons(buttonTaxes, true);
        }

        //births
        drawCenteredText(transform, textRenderer, new TranslatableText("gui.blueprint.birth"), width / 2, height / 2 + positionBirth, 0xffffffff);
        if (!rank.isAtLeast(Rank.NOBLE)) {
            drawCenteredText(transform, textRenderer, new TranslatableText("gui.blueprint.rankTooLow"), width / 2, height / 2 + positionBirth + 15, 0xffffffff);
            toggleButtons(buttonBirths, false);
        } else {
            toggleButtons(buttonBirths, true);
        }

        //marriages
        drawCenteredText(transform, textRenderer, new TranslatableText("gui.blueprint.marriage"), width / 2, height / 2 + positionMarriage, 0xffffffff);
        if (!rank.isAtLeast(Rank.MAYOR)) {
            drawCenteredText(transform, textRenderer, new TranslatableText("gui.blueprint.rankTooLow"), width / 2, height / 2 + positionMarriage + 15, 0xffffffff);
            toggleButtons(buttonMarriage, false);
        } else {
            toggleButtons(buttonMarriage, true);
        }
    }

    private Text getBlockName(Identifier id) {
        if (Registry.BLOCK.containsId(id)) {
            return new TranslatableText(Registry.BLOCK.get(id).getTranslationKey());
        } else {
            return new TranslatableText("tag." + id.toString());
        }
    }

    private void toggleButtons(ButtonWidget[] buttons, boolean active) {
        for (ButtonWidget b : buttons) {
            b.active = active;
            b.visible = active;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (page.equals("villagers") && selectedVillager != null) {
            MinecraftClient.getInstance().openScreen(new FamilyTreeScreen(selectedVillager));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected boolean isMouseWithin(int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    public void setVillage(Village village) {
        this.village = village;
        if (village == null) {
            setPage("empty");
        } else if (page.equals("waiting")) {
            setPage("map");
        }
    }

    public void setRank(Rank rank, int reputation, Set<String> completedTasks, Map<Rank, List<Task>> tasks, Map<String, BuildingType> buildingTypes) {
        this.rank = rank;
        this.reputation = reputation;
        this.completedTasks = completedTasks;
        this.tasks = tasks;
        this.buildingTypes = buildingTypes;
    }
}
