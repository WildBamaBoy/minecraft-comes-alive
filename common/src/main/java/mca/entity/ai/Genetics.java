package mca.entity.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import mca.Config;
import mca.entity.VillagerEntityMCA;
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

    private final VillagerEntityMCA entity;

    public Genetics(VillagerEntityMCA entity) {
        this.entity = entity;
    }

    public float getVerticalScaleFactor() {
        return 0.75F + getGene(SIZE, 0) / 2;
    }

    public float getHorizontalScaleFactor() {
        return 0.75F + getGene(WIDTH, 0) / 2;
    }

    public void setGender(Gender gender) {
        entity.setTrackedValue(GENDER, gender);
    }

    public Gender getGender() {
        return entity.getTrackedValue(GENDER);
    }

    public float getBreastSize() {
        return getGender() == Gender.FEMALE ? getGene(BREAST, 0) : 0;
    }

    //returns a float between 0 and 1, weighted at 0.5
    private float centeredRandom() {
        return (float) Math.min(1.0, Math.max(0.0, (random.nextFloat() - 0.5f) * (random.nextFloat() - 0.5f) + 0.5f));
    }


    @Override
    public Iterator<Gene> iterator() {
        return genes.values().iterator();
    }

    public void setGene(GeneType type, float value) {
        genes.computeIfAbsent(type, Gene::new).set(value);
    }

    public float getGene(GeneType type, float def) {
        return getGene(type).map(Gene::get).orElse(def);
    }

    public Optional<Gene> getGene(GeneType key) {
        return Optional.ofNullable(genes.get(key));
    }

    //initializes the genes with random numbers
    public void randomize(Entity entity) {
        genes.values().forEach(Gene::randomize);

        // size is more centered
        setGene(SIZE, centeredRandom());
        setGene(WIDTH, centeredRandom());

        // temperature
        float temp = entity.world.getBiome(entity.getBlockPos()).getTemperature();

        // TODO: that's racist
        // immigrants
        if (random.nextInt(100) < Config.getInstance().immigrantChance) {
            temp = random.nextFloat() * 2.0f - 0.5f;
        }

        // melanin
        setGene(MELANIN, MathHelper.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.4f + 0.1f, 0, 1));
        setGene(HEMOGLOBIN, MathHelper.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.4f + 0.1f, 0, 1));

        // TODO hair tend to have similar values than hair, but the used LUT is a little bit random
        setGene(EUMELANIN, random.nextFloat());
        setGene(PHEOMELANIN, random.nextFloat());
    }

    public void combine(Genetics mother, Genetics father) {
        for (Gene i : genes.values()) {
            i.mutate(mother, father);
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
            float m = mother.getGene(type).map(Gene::get).orElse(0F);
            float f = father.getGene(type).map(Gene::get).orElse(0F);
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
            return o != null && o instanceof GeneType && ((GeneType)o).key().equals(key());
        }
    }
}
