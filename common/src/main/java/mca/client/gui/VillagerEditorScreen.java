package mca.client.gui;

import java.util.UUID;
import mca.client.gui.widget.ColorPickerWidget;
import mca.client.gui.widget.GeneSliderWidget;
import mca.cobalt.network.NetworkHandler;
import mca.entity.EntitiesMCA;
import mca.entity.Infectable;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.Genetics;
import mca.entity.ai.ProfessionsMCA;
import mca.entity.ai.relationship.Gender;
import mca.network.VillagerEditorSyncRequest;
import mca.network.getVillagerRequest;
import mca.resources.API;
import mca.resources.ClothingList;
import mca.resources.data.Hair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;

import static mca.entity.VillagerLike.VILLAGER_NAME;

public class VillagerEditorScreen extends Screen {
    private final UUID villagerUUID;
    private NbtCompound villagerData;
    private String page;
    private final VillagerEntityMCA dummy = EntitiesMCA.MALE_VILLAGER.create(MinecraftClient.getInstance().world);
    private static int DATA_WIDTH = 150;

    public VillagerEditorScreen(UUID villagerUUID) {
        super(new TranslatableText("gui.VillagerEditorScreen.title"));
        this.villagerUUID = villagerUUID;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        requestVillagerData();
        setPage("loading");
    }

    private int doubleGeneSliders(int y, Genetics.GeneType... genes) {
        boolean right = false;
        assert dummy != null;
        Genetics genetics = dummy.getGenetics();
        for (Genetics.GeneType g : genes) {
            addButton(new GeneSliderWidget(width / 2 + (right ? DATA_WIDTH / 2 : 0), y, DATA_WIDTH / 2, 20, new TranslatableText(g.getTranslationKey()), genetics.getGene(g), b -> genetics.setGene(g, b.floatValue())));
            if (right) {
                y += 20;
            }
            right = !right;
        }
        return y + 4 + (right ? 20 : 0);
    }

    private void setPage(String page) {
        assert dummy != null;
        this.page = page;

        buttons.clear();
        children.clear();

        //page selection
        String[] pages = {"general", "body", "head", "debug"};
        int w = 64;
        int x = (int)(width / 2.0 - pages.length / 2.0 * w);
        for (String p : pages) {
            addButton(new ButtonWidget(x, height / 2 - 105, w, 20, new TranslatableText(p), sender -> {
                setPage(p);
            })).active = !p.equals(page);
            x += w;
        }

        //close
        addButton(new ButtonWidget(width / 2 - DATA_WIDTH + 20, height / 2 + 85, DATA_WIDTH - 40, 20, new TranslatableText("gui.done"), sender -> {
            syncVillagerData();
            onClose();
        }));

        int y = height / 2 - 80;
        int margin = 40;
        Genetics genetics = dummy.getGenetics();

        switch (page) {
            case "general":
                //name
                TextFieldWidget field = addButton(new TextFieldWidget(this.textRenderer, width / 2, y, DATA_WIDTH, 20, new TranslatableText("structure_block.structure_name")));
                field.setMaxLength(32);
                field.setText(dummy.getDefaultName().asString());
                field.setChangedListener(name -> dummy.setTrackedValue(VILLAGER_NAME, name));
                y += 22;

                //gender
                addButton(new ButtonWidget(width / 2, y, DATA_WIDTH / 2, 20, new TranslatableText("gui.villager_editor.female"), sender -> {
                    dummy.getGenetics().setGender(Gender.FEMALE);
                }));
                addButton(new ButtonWidget(width / 2 + DATA_WIDTH / 2, y, DATA_WIDTH / 2, 20, new TranslatableText("gui.villager_editor.male"), sender -> dummy.getGenetics().setGender(Gender.MALE)));
                y += 22;
                break;
            case "body":
                //genes
                y = doubleGeneSliders(y, Genetics.SIZE, Genetics.WIDTH, Genetics.BREAST, Genetics.SKIN);

                //clothes name
                field = addButton(new TextFieldWidget(this.textRenderer, width / 2, y, DATA_WIDTH, 20, new TranslatableText("structure_block.structure_name")));
                field.setMaxLength(32);
                field.setText(dummy.getClothes());
                field.setChangedListener(dummy::setClothes);
                y += 22;

                //clothes
                addButton(new ButtonWidget(width / 2, y, DATA_WIDTH / 2, 20, new TranslatableText("gui.villager_editor.prevClothing"), b -> {
                    dummy.setClothes(ClothingList.getInstance().getPool(dummy).pickNext(dummy.getClothes(), -1));
                    field.setText(dummy.getClothes());
                }));
                addButton(new ButtonWidget(width / 2 + DATA_WIDTH / 2, y, DATA_WIDTH / 2, 20, new TranslatableText("gui.villager_editor.nextClothing"), b -> {
                    dummy.setClothes(ClothingList.getInstance().getPool(dummy).pickNext(dummy.getClothes(), 1));
                    field.setText(dummy.getClothes());
                }));
                y += 22;

                //skin color
                addButton(new ColorPickerWidget(width / 2 + margin, y, DATA_WIDTH - margin * 2, DATA_WIDTH - margin * 2,
                        genetics.getGene(Genetics.HEMOGLOBIN),
                        genetics.getGene(Genetics.MELANIN),
                        new Identifier("mca:textures/colormap/villager_skin.png"),
                        (vx, vy) -> {
                            genetics.setGene(Genetics.HEMOGLOBIN, vx.floatValue());
                            genetics.setGene(Genetics.MELANIN, vy.floatValue());
                        }));
                y += DATA_WIDTH - margin * 2;

                break;
            case "head":
                //genes
                y = doubleGeneSliders(y, Genetics.FACE);

                //hair name
                field = addButton(new TextFieldWidget(this.textRenderer, width / 2, y, DATA_WIDTH, 20, new TranslatableText("structure_block.structure_name")));
                field.setMaxLength(32);
                field.setText(dummy.getHair().texture());
                field.setChangedListener(name -> {
                    dummy.setHair(new Hair(name, dummy.getHair().overlay()));
                });
                y += 22;

                TextFieldWidget field2 = addButton(new TextFieldWidget(this.textRenderer, width / 2, y, DATA_WIDTH, 20, new TranslatableText("structure_block.structure_name")));
                field2.setMaxLength(32);
                field2.setText(dummy.getHair().overlay());
                field2.setChangedListener(name -> {
                    dummy.setHair(new Hair(dummy.getHair().texture(), name));
                });
                y += 22;

                //hair
                addButton(new ButtonWidget(width / 2, y, DATA_WIDTH / 2, 20, new TranslatableText("gui.villager_editor.prevHair"), b -> {
                    dummy.setHair(API.getHairPool().pickNext(dummy, dummy.getHair(), -1));
                    field.setText(dummy.getHair().texture());
                    field2.setText(dummy.getHair().overlay());
                }));
                addButton(new ButtonWidget(width / 2 + DATA_WIDTH / 2, y, DATA_WIDTH / 2, 20, new TranslatableText("gui.villager_editor.nextHair"), b -> {
                    dummy.setHair(API.getHairPool().pickNext(dummy, dummy.getHair(), 1));
                    field.setText(dummy.getHair().texture());
                    field2.setText(dummy.getHair().overlay());
                }));
                y += 22;

                //hair color
                addButton(new ColorPickerWidget(width / 2 + margin, y, DATA_WIDTH - margin * 2, DATA_WIDTH - margin * 2,
                        genetics.getGene(Genetics.PHEOMELANIN),
                        genetics.getGene(Genetics.EUMELANIN),
                        new Identifier("mca:textures/colormap/villager_hair.png"),
                        (vx, vy) -> {
                            genetics.setGene(Genetics.PHEOMELANIN, vx.floatValue());
                            genetics.setGene(Genetics.EUMELANIN, vy.floatValue());
                        }));
                y += DATA_WIDTH - margin * 2;
                break;
            case "debug":
                //profession
                boolean right = false;
                for (VillagerProfession p : new VillagerProfession[] {
                        VillagerProfession.NONE,
                        ProfessionsMCA.GUARD,
                        ProfessionsMCA.ARCHER,
                        ProfessionsMCA.OUTLAW,
                }) {
                    TranslatableText text = new TranslatableText("entity.minecraft.villager." + p);
                    addButton(new ButtonWidget(width / 2 + (right ? DATA_WIDTH / 2 : 0), y, DATA_WIDTH / 2, 20, text, b -> {
                        NbtCompound compound = new NbtCompound();
                        compound.putString("profession", Registry.VILLAGER_PROFESSION.getId(p).toString());
                        syncVillagerData();
                        NetworkHandler.sendToServer(new VillagerEditorSyncRequest("profession", villagerUUID, compound));
                        requestVillagerData();
                    }));
                    if (right) {
                        y += 20;
                    }
                    right = !right;
                }
                y += 4;

                //infection
                addButton(new GeneSliderWidget(width / 2, y, DATA_WIDTH, 20, new TranslatableText("gui.villager_editor.infection"), dummy.getInfectionProgress() / Infectable.MAX_INFECTION, b -> {
                    dummy.setInfected(b > 0);
                    dummy.setInfectionProgress(b.floatValue() * Infectable.MAX_INFECTION);
                }));
                y += 22;
                break;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        fill(matrices, 0, 20, width, height - 20, 0x66000000);

        if (dummy == null) {
            return;
        }

        InventoryScreen.drawEntity(width / 2 - DATA_WIDTH / 2, height / 2 + 70, 60, 0, 0, dummy);

        super.render(matrices, mouseX, mouseY, delta);
    }

    public void setVillagerData(NbtCompound data) {
        villagerData = data.getCompound(villagerUUID.toString());
        if (dummy != null) {
            dummy.readCustomDataFromNbt(villagerData);
        }
        if (page.equals("loading")) {
            setPage("general");
        }
    }

    private void requestVillagerData() {
        NetworkHandler.sendToServer(new getVillagerRequest(villagerUUID));
    }

    private void syncVillagerData() {
        assert dummy != null;
        NbtCompound nbt = new NbtCompound();
        ((MobEntity)dummy).writeCustomDataToNbt(nbt);
        NetworkHandler.sendToServer(new VillagerEditorSyncRequest("sync", villagerUUID, nbt));
    }
}
