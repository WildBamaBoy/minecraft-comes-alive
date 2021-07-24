package mca.resources;

import mca.MCA;
import mca.entity.ai.ProfessionsMCA;
import mca.resources.Resources.BrokenResourceException;
import net.minecraft.resource.ResourceManager;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

    public static String getRandomZombieWord() {
        return instance.pickZombieWord();
    }

    public static Random getRng() {
        return instance.rng;
    }

    static class Data {
        final Random rng = new Random();

        final GiftList gifts = new GiftList();

        final ClothingList clothing = new ClothingList(rng);
        final HairList hair = new HairList(rng);

        final VillageComponents villageComponents = new VillageComponents(rng);

        private final List<String> supporters = new ArrayList<>();

        private final List<String> zombieWords = new ArrayList<>();

        void init(ResourceManager manager) {
            try {
                clothing.load(manager);
                hair.load(manager);
                villageComponents.load(manager);
                gifts.load(manager);

                supporters.addAll(Arrays.asList(Resources.read("api/supporters.json", String[].class)));
                zombieWords.addAll(Arrays.asList(Resources.read("api/zombieWords.json", String[].class)));
            } catch (BrokenResourceException e) {
                MCA.LOGGER.error("Could not load MCA resources", e);
            }
        }

        public String pickSupporter() {
            return PoolUtil.pickOne(supporters, "nobody", rng);
        }

        public String pickZombieWord() {
            return PoolUtil.pickOne(zombieWords, "?", rng);
        }
    }
}
