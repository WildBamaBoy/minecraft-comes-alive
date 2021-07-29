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
    static final Random rng = new Random();
    static Data instance = new Data();

    public static HairList getHairPool() {
        return instance.hair;
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
        return rng;
    }

    static class Data {
        final HairList hair = new HairList();

        final VillageComponents villageComponents = new VillageComponents(rng);

        private final List<String> supporters = new ArrayList<>();

        private final List<String> zombieWords = new ArrayList<>();

        void init(ResourceManager manager) {
            try {
                hair.load(manager);
                villageComponents.load(manager);

                supporters.addAll(Arrays.asList(Resources.read("api/names/supporters.json", String[].class)));
                zombieWords.addAll(Arrays.asList(Resources.read("api/names/zombie_words.json", String[].class)));
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
