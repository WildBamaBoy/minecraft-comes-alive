package mca.api;

import com.google.common.base.Charsets;
import com.google.gson.reflect.TypeToken;
import mca.api.types.*;
import mca.client.gui.GuiInteract;
import mca.client.gui.component.ButtonEx;
import mca.core.MCA;
import mca.entity.VillagerEntityMCA;
import mca.enums.Constraint;
import mca.enums.Gender;
import mca.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;

// TODO: There's too much going on here. This should really be broken up.
class ApiData {
    private final Map<String, Gift> gifts = new HashMap<>();

    private final Map<String, APIButton[]> buttons = new HashMap<>();

    private final Map<String, APIIcon> icons = new HashMap<>();

    private final Map<Gender, List<String>> villagerNames = new EnumMap<>(Gender.class);

    private final Map<Gender, Map<String, List<WeightedEntry>>> clothing = new HashMap<>();
    private final Map<Gender, List<Hair>> hair = new HashMap<>();

    private final Map<String, BuildingType> buildingTypes = new HashMap<>();

    private final Map<String, NameSet> nameSets = new HashMap<>();

    private final List<String> supporters = new ArrayList<>();

    Random rng;

    void init() {
        rng = new Random();

        // Load skins
        // Skins are stored in a <Gender, <Profession, List of paths>> map, which is generic enough to allow custom skins etc
        for (Gender g : Gender.values()) {
            clothing.put(g, new HashMap<>());
        }

        for (ClothingGroup gp : Util.readResourceAsJSON("api/clothing.json", ClothingGroup[].class)) {
            for (Gender g : Gender.values()) {
                if (gp.getGender() == Gender.NEUTRAL || gp.getGender() == g) {
                    if (!clothing.get(g).containsKey(gp.profession())) {
                        clothing.get(g).put(gp.profession(), new LinkedList<>());
                    }
                    for (int i = 0; i < gp.count(); i++) {
                        String path = getClothingPath(gp, i);
                        clothing.get(g).get(gp.profession()).add(new WeightedEntry(path, gp.chance()));
                    }
                }
            }
        }

        // Load hair
        for (Gender g : Gender.values()) {
            hair.put(g, new ArrayList<>());
        }

        for (HairGroup hg : Util.readResourceAsJSON("api/hair.json", HairGroup[].class)) {
            for (Gender g : Gender.values()) {
                if (hg.getGender() == Gender.NEUTRAL || hg.getGender() == g) {
                    for (int i = 0; i < hg.count(); i++) {
                        Hair path = getHair(hg, i);
                        hair.get(g).add(path);
                    }
                }
            }
        }

        for (BuildingType bt : Util.readResourceAsJSON("api/buildingTypes.json", BuildingType[].class)) {
            buildingTypes.put(bt.name(), bt);
        }

        nameSets.put("village", Util.readResourceAsJSON("api/names/village.json", NameSet.class));
        supporters.addAll(List.of(Util.readResourceAsJSON("api/supporters.json", String[].class)));

        // Load names
        // TODO: We don't use lang files any more. Convert this to json.
        // TODO: Procedurally-generated names using linguistic patterns.
        try (InputStream namesStream = ChatUtil.class.getResourceAsStream("/assets/mca/lang/names.lang")) {
            // read in all names and process into the correct list
            IOUtils.readLines(namesStream, Charsets.UTF_8).stream().forEach(line -> {
                Gender gender = line.contains("name.male") ? Gender.MALE : line.contains("name.female") ? Gender.FEMALE : null;
                if (gender != null) {
                    villagerNames.computeIfAbsent(gender, g -> new ArrayList<>()).add(line.split("=")[1]);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load all NPC names from file", e);
        }

        // Read in buttons
        buttons.put("main", Util.readResourceAsJSON("api/gui/main.json", APIButton[].class));
        buttons.put("interact", Util.readResourceAsJSON("api/gui/interact.json", APIButton[].class));
        buttons.put("debug", Util.readResourceAsJSON("api/gui/debug.json", APIButton[].class));
        buttons.put("work", Util.readResourceAsJSON("api/gui/work.json", APIButton[].class));
        buttons.put("locations", Util.readResourceAsJSON("api/gui/locations.json", APIButton[].class));
        buttons.put("command", Util.readResourceAsJSON("api/gui/command.json", APIButton[].class));
        buttons.put("clothing", Util.readResourceAsJSON("api/gui/clothing.json", APIButton[].class));

        // Icons
        Type mapType = new TypeToken<Map<String, APIIcon>>() {}.getType();
        icons.putAll(Util.readResourceAsJSON("api/gui/icons.json", mapType));

        // Load gifts and assign to the appropriate map with a key value pair and print warnings on potential issues
        for (Gift gift : Util.readResourceAsJSON("api/gifts.json", Gift[].class)) {
            if (!gift.exists()) {
                MCA.logger.info("Could not find gift item or block in registry: " + gift.name());
            } else {
                gifts.put(gift.name(), gift);
            }
        }
    }

    //returns the clothing group based of gender and profession, or a random one in case of an unknown clothing group
    private List<WeightedEntry> getClothing(VillagerEntityMCA villager) {
        String profession = Objects.requireNonNull(Registry.VILLAGER_PROFESSION.getId(villager.getProfession())).toString();
        Gender gender = villager.getGender();

        if (clothing.get(gender).containsKey(profession)) {
            return clothing.get(gender).get(profession);
        }

        return clothing.get(gender).get("minecraft:none");
    }

    private String getClothingPath(ClothingGroup group, int i) {
        return String.format("mca:skins/clothing/%s/%s/%d.png", group.getGender().getStrName(), group.profession().split(":")[1], i);
    }

    public String getRandomClothing(VillagerEntityMCA villager) {
        List<WeightedEntry> group = getClothing(villager);
        if (group == null) {
            return "";
        }
        double totalChance = group.stream().mapToDouble(a -> a.weight).sum() * rng.nextDouble();
        for (WeightedEntry e : group) {
            totalChance -= e.weight;
            if (totalChance <= 0.0) {
                return e.value;
            }
        }
        return "";
    }

    //returns the next clothing with given offset to current
    public String getNextClothing(VillagerEntityMCA villager, String current, int next) {
        List<WeightedEntry> group = getClothing(villager);

        //look for the current one
        for (int i = 0; i < group.size(); i++) {
            if (group.get(i).value.equals(current)) {
                return group.get(Math.floorMod(i + next, group.size())).value;
            }
        }

        //fallback
        return getRandomClothing(villager);
    }

    private Hair getHair(HairGroup g, int i) {
        String overlay = String.format("mca:skins/hair/%s/%d_overlay.png", g.getGender().getStrName(), i);
        boolean hasOverlay = MinecraftClient.getInstance().getResourceManager().containsResource(new Identifier(overlay));
        return new Hair(
                String.format("mca:skins/hair/%s/%d.png", g.getGender().getStrName(), i),
                hasOverlay ? overlay : ""
        );
    }

    public Hair getRandomHair(VillagerEntityMCA villager) {
        Gender gender = villager.getGender();
        List<Hair> hairs = hair.get(gender);
        if (hairs.isEmpty()) {
            return new Hair();
        }
        return hairs.get(rng.nextInt(hairs.size()));
    }

    //returns the next clothing with given offset to current
    public Hair getNextHair(VillagerEntityMCA villager, Hair current, int next) {
        Gender gender = villager.getGender();
        List<Hair> hairs = hair.get(gender);

        //look for the current one
        for (int i = 0; i < hairs.size(); i++) {
            if (hairs.get(i).texture().equals(current.texture())) {
                return hairs.get(Math.floorMod(i + next, hairs.size()));
            }
        }

        //fallback
        return getRandomHair(villager);
    }

    public Optional<APIButton> getButtonById(String key, String id) {
        return Arrays.stream(buttons.get(key)).filter(b -> b.identifier().equals(id)).findFirst();
    }

    public APIIcon getIcon(String key) {
        if (!icons.containsKey(key)) {
            MCA.logger.info("Icon " + key + " does not exist!");
            icons.put(key, new APIIcon(0, 0, 0, 0));
        }
        return icons.get(key);
    }

    public int getGiftValueFromStack(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        Identifier id = Registry.ITEM.getId(stack.getItem());

        if (id == null) return 0;

        String name = id.toString();
        return gifts.containsKey(name) ? gifts.get(name).value() : 0;
    }

    public String getResponseForGift(ItemStack stack) {
        int value = getGiftValueFromStack(stack);
        return "gift." + (value <= 0 ? "fail" : value <= 5 ? "good" : value <= 10 ? "better" : "best");
    }

    public String getResponseForSaturatedGift(ItemStack stack) {
        return "saturatedGift";
    }

    public String getRandomName(@NotNull Gender gender) {

        List<String> names = villagerNames.computeIfAbsent(gender, g -> new ArrayList<>());

        if (!names.isEmpty()) {
            return names.get(rng.nextInt(names.size()));
        }
        return "";
    }

    public void addButtons(String guiKey, GuiInteract screen) {
        for (APIButton b : buttons.get(guiKey)) {
            ButtonEx guiButton = new ButtonEx(screen, b);
            screen.addExButton(guiButton);

            // Remove the button if we specify it should not be present on constraint failure
            // Otherwise we just mark the button as disabled.
            boolean isValid = b.isValidForConstraint(screen.getConstraints());
            if (!isValid && b.getConstraints().contains(Constraint.HIDE_ON_FAIL)) {
                guiButton.visible = false;
            } else if (!isValid) {
                guiButton.active = false;
            }
        }
    }

    //returns a random generated name for a given name set
    public String getRandomVillageName(String from) {
        if (nameSets.containsKey(from)) {
            NameSet set = nameSets.get(from);
            String first = set.first()[rng.nextInt(set.first().length)];
            String second = set.second()[rng.nextInt(set.second().length)];
            return first.substring(0, 1).toUpperCase() + first.substring(1) + set.separator() + second;

        } else {
            return "unknown names";
        }
    }

    public String getRandomSupporter() {
        if (supporters.isEmpty()) {
            return "Nobody";
        }
        return supporters.get(rng.nextInt(supporters.size()));
    }

    public Map<String, BuildingType> getBuildingTypes() {
        return buildingTypes;
    }

    public BuildingType getBuildingType(String type) {
        return buildingTypes.containsKey(type) ? buildingTypes.get(type) : new BuildingType();
    }

    private class WeightedEntry {
        final String value;
        final float weight;

        public WeightedEntry(String value, float weight) {
            this.value = value;
            this.weight = weight;
        }
    }
}
