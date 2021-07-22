package mca.cobalt.registration;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class Registration {
    private static Impl INSTANCE;

    public static <T> T register(Registry<? super T> registry, Identifier id, T obj) {
        return INSTANCE.register(registry, id, obj);
    }

    public static class ObjectBuilders {
        public static class ItemGroups {
            public static ItemGroup create(Identifier id, Supplier<ItemStack> icon) {
                return INSTANCE.itemGroup(id, icon);
            }
        }

        public static class DefaultEntityAttributes {
            public static <T extends LivingEntity> EntityType<T> add(EntityType<T> type, Supplier<Builder> attributes) {
                return INSTANCE.<T>defaultEntityAttributes().apply(type, attributes);
            }
        }

        public static class Particles {
            public static DefaultParticleType simpleParticle() {
                return INSTANCE.simpleParticle().get();
            }
        }

        public static class Tags {
            public static Tag<Block> block(Identifier id) {
                return INSTANCE.blockTag().apply(id);
            }

            public static Tag<Item> item(Identifier id) {
                return INSTANCE.itemTag().apply(id);
            }
        }

        public static class Activities {
            public static Activity create(Identifier id) {
                return INSTANCE.activity().apply(id);
            }
        }

        public static class Sensors {
            public static <T extends Sensor<?>> SensorType<T> create(Identifier id, Supplier<T> factory) {
                return INSTANCE.<T>sensor().apply(id, factory);
            }
        }

        public static class MemoryModules {
            public static <U> MemoryModuleType<U> create(Identifier id, Optional<Codec<U>> codec) {
                return INSTANCE.<U>memoryModule().apply(id, codec);
            }
        }

        public static class Poi {
            public static PointOfInterestType create(Identifier id, int ticketCount, int searchDistance, Block... blocks) {
                return INSTANCE.poi().apply(id, ticketCount, searchDistance, blocks);
            }
        }

        public static class Profession {
            public static ProfessionFactory<VillagerProfession> creator() {
                return INSTANCE.profession();
            }
        }
    }

    public static abstract class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T> T register(Registry<? super T> registry, Identifier id, T obj);

        public abstract ItemGroup itemGroup(Identifier id, Supplier<ItemStack> icon);

        public abstract Supplier<DefaultParticleType> simpleParticle();

        public abstract Function<Identifier, Tag<Block>> blockTag();

        public abstract Function<Identifier, Tag<Item>> itemTag();

        public abstract Function<Identifier, Activity> activity();

        public abstract <T extends Sensor<?>> BiFunction<Identifier, Supplier<T>, SensorType<T>> sensor();

        public abstract <U> BiFunction<Identifier, Optional<Codec<U>>, MemoryModuleType<U>> memoryModule();

        public abstract <T extends LivingEntity> BiFunction<EntityType<T>, Supplier<Builder>, EntityType<T>> defaultEntityAttributes();

        public abstract PoiFactory<PointOfInterestType> poi();

        public abstract ProfessionFactory<VillagerProfession> profession();
    }

    protected interface PoiFactory<T> {
        T apply(Identifier id, int ticketCount, int searchDistance, Block...blocks);
    }

    public interface ProfessionFactory<T> {
        T apply(Identifier id, PointOfInterestType workStation, @Nullable SoundEvent workSound,
                Iterable<Item> gatherableItems,
                Iterable<Block> secondaryJobSites);
    }
}
