package mca.entity.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import mca.Config;
import mca.entity.ai.relationship.Gender;
import mca.util.network.datasync.CDataManager;
import mca.util.network.datasync.CEnumParameter;
import mca.util.network.datasync.CFloatParameter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.MathHelper;

/**
 * Villagerized Genetic Diversity.
 */
public class Genetics implements Iterable<Genetics.Gene> {

    public static Genetics create(CDataManager data) {
        return new Genetics(data);
    }

    private final Random random = new Random();

    private final CDataManager data;

    private final Map<String, Gene> genes = new HashMap<>();

    // genes
    public final Gene size;
    public final Gene width;
    public final Gene breast;
    public final Gene melanin;
    public final Gene hemoglobin;
    public final Gene eumelanin;
    public final Gene pheomelanin;
    public final Gene skin;
    public final Gene face;

    public final CEnumParameter<Gender> gender;

    private Genetics(CDataManager data) {
        this.data = data;
        size = withGene("gene_size");
        width = withGene("gene_width");
        breast = withGene("gene_breast");
        melanin = withGene("gene_melanin");
        hemoglobin = withGene("gene_hemoglobin");
        eumelanin = withGene("gene_eumelanin");
        pheomelanin = withGene("gene_pheomelanin");
        skin = withGene("gene_skin");
        face = withGene("gene_face");
        gender = data.newEnum("gender", Gender.UNASSIGNED);
    }

    protected Gene withGene(String key) {
        return new Gene(key);
    }

    public float getVerticalScaleFactor() {
        return 0.75F + size.get() / 2;
    }

    public float getHorizontalScaleFactor() {
        return 0.75F + width.get() / 2;
    }

    public void setGender(Gender gender) {
        this.gender.set(gender);
    }

    public Gender getGender() {
        return gender.get();
    }

    public float getBreastSize() {
        return getGender() == Gender.FEMALE ? breast.get() : 0;
    }

    //returns a float between 0 and 1, weighted at 0.5
    private float centeredRandom() {
        return (float) Math.min(1.0, Math.max(0.0, (random.nextFloat() - 0.5f) * (random.nextFloat() - 0.5f) + 0.5f));
    }


    @Override
    public Iterator<Gene> iterator() {
        return genes.values().iterator();
    }

    public Optional<Gene> getGene(String key) {
        return Optional.ofNullable(genes.get(key));
    }

    //initializes the genes with random numbers
    public void randomize(Entity entity) {
        genes.values().forEach(Gene::randomize);

        // size is more centered
        size.set(centeredRandom());
        width.set(centeredRandom());

        // temperature
        float temp = entity.world.getBiome(entity.getBlockPos()).getTemperature();

        // TODO: that's racist
        // immigrants
        if (random.nextInt(100) < Config.getInstance().immigrantChance) {
            temp = random.nextFloat() * 2.0f - 0.5f;
        }

        // melanin
        melanin.set(MathHelper.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.4f + 0.1f, 0, 1));
        hemoglobin.set(MathHelper.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.4f + 0.1f, 0, 1));

        // TODO hair tend to have similar values than hair, but the used LUT is a little bit random
        eumelanin.set(random.nextFloat());
        pheomelanin.set(random.nextFloat());
    }

    public void combine(Genetics mother, Genetics father) {
        for (Gene i : genes.values()) {
            i.mutate(mother, father);
        }
    }

    public class Gene {
        private final String key;
        private final CFloatParameter parameter;

        Gene(String key) {
            this.key = key;
            parameter = data.newFloat(key);
            genes.put(key, this);
        }

        public boolean equals(TrackedData<?> par) {
            return parameter.getParam().equals(par);
        }

        public String key() {
            return key;
        }

        public float get() {
            return parameter.get();
        }

        public void set(float value) {
            parameter.set(value);
        }

        public void randomize() {
            parameter.set(random.nextFloat());
        }

        public void mutate(Genetics mother, Genetics father) {
            float m = mother.getGene(key).map(Gene::get).orElse(0F);
            float f = father.getGene(key).map(Gene::get).orElse(0F);
            float interpolation = random.nextFloat();
            float mutation = (random.nextFloat() - 0.5f) * 0.2f;
            float g = m * interpolation + f * (1.0f - interpolation) + mutation;

            set((float) Math.min(1.0, Math.max(0.0, g)));
        }
    }
}
