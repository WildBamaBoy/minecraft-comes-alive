package mca.entity.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import mca.Config;
import mca.entity.VillagerLike;
import mca.entity.ai.relationship.Gender;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CDataParameter;
import mca.util.network.datasync.CEnumParameter;
import mca.util.network.datasync.CParameter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Villagerized Genetic Diversity.
 */
public class Genetics implements Iterable<Genetics.Gene> {
    private static final Set<GeneType> GENOMES = new HashSet<>();

    public static final GeneType SIZE = new GeneType("gene_size");
    public static final GeneType WIDTH = new GeneType("gene_width");
    public static final GeneType BREAST = new GeneType("gene_breast");
    public static final GeneType MELANIN = new GeneType("gene_melanin");
    public static final GeneType HEMOGLOBIN = new GeneType("gene_hemoglobin");
    public static final GeneType EUMELANIN = new GeneType("gene_eumelanin");
    public static final GeneType PHEOMELANIN = new GeneType("gene_pheomelanin");
    public static final GeneType SKIN = new GeneType("gene_skin");
    public static final GeneType FACE = new GeneType("gene_face");

    private static final CEnumParameter<Gender> GENDER = CParameter.create("gender", Gender.UNASSIGNED);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        GENOMES.forEach(g -> builder.addAll(g.getParam()));
        return builder.addAll(GENDER);
    }

    private final Random random = new Random();

    private final Map<GeneType, Gene> genes = new HashMap<>();

    private final VillagerLike<?> entity;

    public Genetics(VillagerLike<?> entity) {
        this.entity = entity;
    }

    public float getVerticalScaleFactor() {
        return 0.75F + getGene(SIZE) / 2;
    }

    public float getHorizontalScaleFactor() {
        return 0.75F + getGene(WIDTH) / 2;
    }

    public void setGender(Gender gender) {
        entity.setTrackedValue(GENDER, gender);
    }

    public Gender getGender() {
        return entity.getTrackedValue(GENDER);
    }

    public float getBreastSize() {
        return getGender() == Gender.FEMALE ? getGene(BREAST) : 0;
    }

    @Override
    public Iterator<Gene> iterator() {
        return genes.values().iterator();
    }

    public void setGene(GeneType type, float value) {
        getGenome(type).set(value);
    }

    public float getGene(GeneType type) {
        return getGenome(type).get();
    }

    public Gene getGenome(GeneType type) {
        return genes.computeIfAbsent(type, Gene::new);
    }

    //initializes the genes with random numbers
    public void randomize(Entity entity) {
        for (GeneType type : GENOMES) {
            getGenome(type).randomize();
        }

        // size is more centered
        setGene(SIZE, centeredRandom());
        setGene(WIDTH, centeredRandom());

        // temperature
        float temp = entity.world.getBiome(entity.getBlockPos()).getTemperature();

        // immigrants
        if (random.nextInt(100) < Config.getInstance().immigrantChance) {
            temp = random.nextFloat() * 2 - 0.5F;
        }

        setGene(MELANIN, temperatureBaseRandom(temp));
        setGene(HEMOGLOBIN, temperatureBaseRandom(temp));

        setGene(EUMELANIN, random.nextFloat());
        setGene(PHEOMELANIN, random.nextFloat());
    }

    /**
     * Produces a float between 0 and 1, weighted at 0.5
     */
    private float centeredRandom() {
        return Math.min(1, Math.max(0, (random.nextFloat() - 0.5F) * (random.nextFloat() - 0.5F) + 0.5F));
    }

    private float temperatureBaseRandom(float temp) {
        return MathHelper.clamp((random.nextFloat() - 0.5F) * 0.5F + temp * 0.4F + 0.1F, 0, 1);
    }

    public void combine(Genetics mother, Genetics father) {
        for (GeneType type : GENOMES) {
            getGenome(type).mutate(mother, father);
        }
    }

    public void combine(Optional<Genetics> mother, Optional<Genetics> father) {
        if (mother.isPresent() != father.isPresent()) {
            Genetics single = mother.orElse(father.orElse(null));
            if (single != null) {
                combine(single, single);
            }
        } else if (mother.isPresent()) {
            combine(mother.get(), father.get());
        }
    }

    public class Gene {
        private final GeneType type;

        public Gene(GeneType type) {
            this.type = type;
        }

        public GeneType getType() {
            return type;
        }

        public float get() {
            return entity.getTrackedValue(type.parameter);
        }

        public void set(float value) {
            entity.setTrackedValue(type.parameter, value);
        }

        public void randomize() {
            set(random.nextFloat());
        }

        public void mutate(Genetics mother, Genetics father) {
            float m = mother.getGene(type);
            float f = father.getGene(type);
            float interpolation = random.nextFloat();
            float mutation = (random.nextFloat() - 0.5f) * 0.2f;
            float g = m * interpolation + f * (1.0f - interpolation) + mutation;

            set((float) Math.min(1.0, Math.max(0.0, g)));
        }
    }

    public static class GeneType implements Comparable<GeneType> {
        private final String key;
        private final CDataParameter<Float> parameter;

        GeneType(String key) {
            this.key = key;
            parameter = CParameter.create(key, 0F);
            GENOMES.add(this);
        }

        public String key() {
            return key;
        }

        public String getTranslationKey() {
            return key().replace("_", ".");
        }

        public CDataParameter<Float> getParam() {
            return parameter;
        }

        @Override
        public int compareTo(GeneType o) {
            return key().compareTo(o.key());
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GeneType && ((GeneType)o).key().equals(key());
        }
    }
}
