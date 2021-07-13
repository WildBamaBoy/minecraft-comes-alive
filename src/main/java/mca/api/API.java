package mca.api;

import mca.core.minecraft.ProfessionsMCA;
import net.minecraft.village.VillagerProfession;
import java.util.*;

/**
 * Class API handles interaction with MCAs configurable options via JSON in the resources folder
 */
public class API {
    static Data instance = new Data();

    public static ClothingList getClothingPool() {
        return instance.clothing;
    }

    public static HairList getHairPool() {
        return instance.hair;
    }

    public static GuiComponents getScreenComponents() {
        return instance.guiComponents;
    }

    public static GiftList getGiftPool() {
        return instance.gifts;
    }

    public static VillagerProfession randomProfession() {
        return ProfessionsMCA.randomProfession();
    }

    public static VillageComponents getVillagePool() {
        return instance.villageComponents;
    }

    public static String getRandomSupporter() {
        return instance.pickSupporter();
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

            supporters.addAll(List.of(Resources.read("api/supporters.json", String[].class)));
        }

        public String pickSupporter() {
            return PoolUtil.pickOne(supporters, "nobody", rng);
        }
    }
}
