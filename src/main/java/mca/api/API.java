package mca.api;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import mca.api.types.*;
import mca.client.gui.component.GuiButtonEx;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumConstraint;
import mca.enums.EnumGender;
import mca.util.Util;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Class API handles interaction with MCA's configurable options via JSON in the resources folder
 */
public class API {
    private static final Map<String, Gift> giftMap = new HashMap<>();
    private static final Map<String, APIButton[]> buttonMap = new HashMap<>();
    private static final Map<String, APIIcon> iconMap = new HashMap<>();
    private static final List<String> maleNames = new ArrayList<>();
    private static final List<String> femaleNames = new ArrayList<>();
    private static final List<ClothingGroup> clothing = new ArrayList<>();
    private static final List<HairGroup> hair = new ArrayList<>();
    private static Random rng;

    /**
     * Performs initialization of the API
     */
    public static void init() {
        rng = new Random();

        // Load skins
        Collections.addAll(clothing, Util.readResourceAsJSON("api/clothing.json", ClothingGroup[].class));
        Collections.addAll(hair, Util.readResourceAsJSON("api/hair.json", HairGroup[].class));

        // Load names
        InputStream namesStream = StringUtils.class.getResourceAsStream("/assets/mca/lang/names.lang");
        try {
            // read in all names and process into the correct list
            List<String> lines = IOUtils.readLines(namesStream, Charsets.UTF_8);
            lines.stream().filter((l) -> l.contains("name.male")).forEach((l) -> maleNames.add(l.split("\\=")[1]));
            lines.stream().filter((l) -> l.contains("name.female")).forEach((l) -> femaleNames.add(l.split("\\=")[1]));
        } catch (Exception e) {
            MCA.getLog().fatal(e);
            throw new RuntimeException("Failed to load all NPC names from file", e);
        }

        // Read in buttons
        buttonMap.put("main", Util.readResourceAsJSON("api/gui/main.json", APIButton[].class));
        buttonMap.put("interact", Util.readResourceAsJSON("api/gui/interact.json", APIButton[].class));
        buttonMap.put("debug", Util.readResourceAsJSON("api/gui/debug.json", APIButton[].class));
        buttonMap.put("work", Util.readResourceAsJSON("api/gui/work.json", APIButton[].class));
        buttonMap.put("locations", Util.readResourceAsJSON("api/gui/locations.json", APIButton[].class));
        buttonMap.put("command", Util.readResourceAsJSON("api/gui/command.json", APIButton[].class));
        buttonMap.put("clothing", Util.readResourceAsJSON("api/gui/clothing.json", APIButton[].class));

        // Icons
        Type mapType = new TypeToken<Map<String, APIIcon>>() {}.getType();
        iconMap.putAll((new Gson()).fromJson(Util.readResource("api/gui/icons.json"), mapType));

        // Load gifts and assign to the appropriate map with a key value pair and print warnings on potential issues
        Gift[] gifts = Util.readResourceAsJSON("api/gifts.json", Gift[].class);
        for (Gift gift : gifts) {
            if (!gift.exists()) {
                MCA.getLog().warn("Could not find gift item or block in registry: " + gift.getName());
            } else {
                giftMap.put(gift.getName(), gift);
            }
        }
    }

    //returns the clothing group based of gender and profession
    private static Optional<ClothingGroup> getClothingGroup(EntityVillagerMCA villager) {
        VillagerRegistry.VillagerProfession profession = villager.getProfessionForge();
        EnumGender gender = EnumGender.byId(villager.get(EntityVillagerMCA.GENDER));

        return clothing.stream()
                .filter(g -> g.getGender() == gender && profession.getRegistryName() != null && g.getProfession().equals(profession.getRegistryName().toString()))
                .findFirst();
    }

    /**
     * Returns a random skin based on the profession and gender provided.
     *
     * @param villager The villager who will be assigned the random skin.
     * @return String location of the random skin
     */
    public static String getRandomClothing(EntityVillagerMCA villager) {
        //Default skin behavior
        Optional<ClothingGroup> group = getClothingGroup(villager);

        return group.map(g -> g.getPaths()[rng.nextInt(g.getPaths().length)]).orElseGet(() -> {
            ClothingGroup randomGroup = null;
            EnumGender gender = EnumGender.byId(villager.get(EntityVillagerMCA.GENDER));
            while (randomGroup == null || randomGroup.getGender() != gender) {
                randomGroup = clothing.get(rng.nextInt(clothing.size()));
            }
            return randomGroup.getPaths()[rng.nextInt(randomGroup.getPaths().length)];
        });
    }

    //returns the next clothing
    public static String getNextClothing(EntityVillagerMCA villager, String current) {
        return getNextClothing(villager, current, 1);
    }

    //returns the next clothing with given offset to current
    public static String getNextClothing(EntityVillagerMCA villager, String current, int next) {
        Optional<ClothingGroup> group = getClothingGroup(villager);

        return group.map(g -> {
            String[] arr = g.getPaths();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].equals(current)) {
                    return arr[Math.floorMod(i + next, arr.length)];
                }
            }
            return current;
        }).orElse(current);
    }

    /**
     * Returns a random hair and optional overlay based on the gender provided.
     *
     * @param villager The villager who will be assigned the hair.
     * @return String location of the random skin
     */
    public static Hair getRandomHair(EntityVillagerMCA villager) {
        EnumGender gender = EnumGender.byId(villager.get(EntityVillagerMCA.GENDER));
        Optional<HairGroup> group = hair.stream().filter(g -> g.getGender() == gender).findFirst();

        return group.map(g -> g.getPaths()[rng.nextInt(g.getPaths().length)]).orElse(new Hair());
    }

    //returns the next clothing
    public static Hair getNextHair(EntityVillagerMCA villager, Hair current) {
        return getNextHair(villager, current, 1);
    }

    //returns the next clothing with given offset to current
    public static Hair getNextHair(EntityVillagerMCA villager, Hair current, int next) {
        EnumGender gender = EnumGender.byId(villager.get(EntityVillagerMCA.GENDER));
        Optional<HairGroup> group = hair.stream().filter(g -> g.getGender() == gender).findFirst();

        return group.map(g -> {
            Hair[] arr = g.getPaths();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].equals(current)) {
                    return arr[Math.floorMod(i + next, arr.length)];
                }
            }
            return current;
        }).orElse(current);
    }

    /**
     * Returns an API button based on its ID
     *
     * @param id String id matching the targeted button
     * @return Instance of APIButton matching the ID provided
     */
    public static Optional<APIButton> getButtonById(String key, String id) {
        return Arrays.stream(buttonMap.get(key)).filter(b -> b.getIdentifier().equals(id)).findFirst();
    }

    /**
     * Returns an API icon based on its key
     *
     * @param key String key of icon
     * @return Instance of APIIcon matching the ID provided
     */
    public static APIIcon getIcon(String key) {
        if (!iconMap.containsKey(key)) {
            MCA.getLog().error("Icon " + key + " does not exist!");
            iconMap.put(key, new APIIcon(0, 0, 0, 0));
        }
        return iconMap.get(key);
    }

    /**
     * Returns the value of a gift from an ItemStack
     *
     * @param stack ItemStack containing the gift item
     * @return int value determining the gift value of a stack
     */
    public static int getGiftValueFromStack(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.getItem().getRegistryName() == null) return 0;

        String name = stack.getItem().getRegistryName().toString();
        return giftMap.containsKey(name) ? giftMap.get(name).getValue() : -5;
    }

    /**
     * Returns the proper response type based on a gift provided
     *
     * @param stack ItemStack containing the gift item
     * @return String value of the appropriate response type
     */
    public static String getResponseForGift(ItemStack stack) {
        int value = getGiftValueFromStack(stack);
        return "gift." + (value <= 0 ? "fail" : value <= 5 ? "good" : value <= 10 ? "better" : "best");
    }

    /**
     * Gets a random name based on the gender provided.
     *
     * @param gender The gender the name should be appropriate for.
     * @return A gender appropriate name based on the provided gender.
     */
    public static String getRandomName(@Nonnull EnumGender gender) {
        if (gender == EnumGender.MALE) return maleNames.get(rng.nextInt(maleNames.size()));
        else if (gender == EnumGender.FEMALE) return femaleNames.get(rng.nextInt(femaleNames.size()));
        return "";
    }

    /**
     * Adds API buttons to the GUI screen provided.
     *
     * @param guiKey   String key for the GUI's buttons
     * @param villager Optional EntityVillagerMCA the GuiScreen has been opened on
     * @param player   EntityPlayer who has opened the GUI
     * @param screen   GuiScreen instance the buttons should be added to
     */
    public static void addButtons(String guiKey, @Nullable EntityVillagerMCA villager, EntityPlayer player, GuiScreen screen) {
        List<GuiButton> buttonList = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, screen, Constants.GUI_SCREEN_BUTTON_LIST_FIELD_INDEX);
        for (APIButton b : buttonMap.get(guiKey)) {
            GuiButtonEx guiButton = new GuiButtonEx(screen, b);
            buttonList.add(guiButton);

            // Ensure that if a constraint is attached to the button
            if (villager == null && b.getConstraints().size() > 0) {
                MCA.getLog().error("No villager provided for list of buttons with constraints! Button ID:" + b.getIdentifier());
                continue;
            }

            // Remove the button if we specify it should not be present on constraint failure
            // Otherwise we just mark the button as disabled.
            boolean isValid = b.isValidForConstraint(villager, player);
            if (!isValid && b.getConstraints().contains(EnumConstraint.HIDE_ON_FAIL)) buttonList.remove(guiButton);
            else if (!isValid) guiButton.enabled = false;
        }
    }

    /**
     * Returns an instance of the button linked to the given ID on the provided GuiScreen
     *
     * @param id     String id of the button desired
     * @param screen GuiScreen containing the button
     * @return GuiButtonEx matching the provided id
     */
    public static Optional<GuiButtonEx> getButton(String id, GuiScreen screen) {
        List<GuiButton> buttonList = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, screen, Constants.GUI_SCREEN_BUTTON_LIST_FIELD_INDEX);
        Optional<GuiButton> button = buttonList.stream().filter(
                (b) -> b instanceof GuiButtonEx && ((GuiButtonEx) b).getApiButton().getIdentifier().equals(id)).findFirst();

        return button.map(guiButton -> (GuiButtonEx) guiButton);
    }
}
