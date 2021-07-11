package mca.api;

import mca.api.types.*;
import mca.client.gui.GuiInteract;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.VillagerEntityMCA;
import mca.enums.Gender;
import mca.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class API handles interaction with MCAs configurable options via JSON in the resources folder
 */
public class API {
    static Data instance = new Data();

    /**
     * Returns a random skin based on the profession and gender provided.
     *
     * @param villager The villager who will be assigned the random skin.
     * @return String location of the random skin
     */
    public static String getRandomClothing(VillagerEntityMCA villager) {
        return instance.clothing.pickOne(villager);
    }

    //returns the next clothing
    public static String getNextClothing(VillagerEntityMCA villager, String current) {
        return getNextClothing(villager, current, 1);
    }

    //returns the next clothing with given offset to current
    public static String getNextClothing(VillagerEntityMCA villager, String current, int next) {
        return instance.clothing.getNext(villager, current, next);
    }

    /**
     * Returns a random hair and optional overlay based on the gender provided.
     *
     * @param villager The villager who will be assigned the hair.
     * @return String location of the random skin
     */
    public static Hair getRandomHair(VillagerEntityMCA villager) {
        return instance.hair.getRandomHair(villager);
    }

    //returns the next clothing
    public static Hair getNextHair(VillagerEntityMCA villager, Hair current) {
        return getNextHair(villager, current, 1);
    }

    //returns the next clothing with given offset to current
    public static Hair getNextHair(VillagerEntityMCA villager, Hair current, int next) {
        return instance.hair.getNextHair(villager, current, next);
    }

    /**
     * Returns an API button based on its ID
     *
     * @param id String id matching the targeted button
     * @return Instance of APIButton matching the ID provided
     */
    public static Optional<Button> getButtonById(String key, String id) {
        return instance.guiComponents.getButtonById(key, id);
    }

    /**
     * Returns an API icon based on its key
     *
     * @param key String key of icon
     * @return Instance of APIIcon matching the ID provided
     */
    public static Icon getIcon(String key) {
        return instance.guiComponents.getIcon(key);
    }

    /**
     * Returns the value of a gift from an ItemStack
     *
     * @param stack ItemStack containing the gift item
     * @return int value determining the gift value of a stack
     */
    public static int getGiftValueFromStack(ItemStack stack) {
        return instance.gifts.getGiftValueFromStack(stack);
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

    public static String getResponseForSaturatedGift(ItemStack stack) {
        return "saturatedGift";
    }

    /**
     * Gets a random name based on the gender provided.
     *
     * @param gender The gender the name should be appropriate for.
     * @return A gender appropriate name based on the provided gender.
     */
    public static String getRandomName(@NotNull Gender gender) {
        return instance.villageComponents.pickCitizenName(gender);
    }

    /**
     * Adds API buttons to the GUI screen provided.
     *
     * @param guiKey String key for the GUI's buttons
     * @param screen Screen instance the buttons should be added to
     */
    public static void addButtons(String guiKey, GuiInteract screen) {
        instance.guiComponents.addButtons(guiKey, screen);
    }

    public static VillagerProfession randomProfession() {
        return ProfessionsMCA.randomProfession();
    }

    //returns a random generated name for a given name set
    public static String getRandomVillageName(String from) {
        return instance.villageComponents.pickVillageName(from);
    }

    public static String getRandomSupporter() {
        return instance.pickSupporter();
    }

    public static Map<String, BuildingType> getBuildingTypes() {
        return instance.villageComponents.getBuildingTypes();
    }

    public static BuildingType getBuildingType(String type) {
        return instance.villageComponents.getBuildingType(type);
    }

    public static Random getRng() {
        return instance.rng;
    }

    static class Data {
        final Random rng = new Random();

        final GiftList gifts = new GiftList();

        final ClothingList clothing = new ClothingList(rng);
        final HairList hair = new HairList(rng);

        final GuiComponents guiComponents = new GuiComponents();
        final VillageComponents villageComponents = new VillageComponents(rng);

        private final List<String> supporters = new ArrayList<>();

        void init() {
            clothing.load();
            hair.load();
            villageComponents.load();
            guiComponents.load();
            gifts.load();

            supporters.addAll(List.of(Util.readResourceAsJSON("api/supporters.json", String[].class)));
        }

        public String pickSupporter() {
            return PoolUtil.pickOne(supporters, "nobody", rng);
        }
    }
}
