package mca.cobalt.registration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;

import mca.cobalt.registration.Registration.PoiFactory;
import mca.cobalt.registration.Registration.ProfessionFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

/**
 * Contains all the crob required to interface with forge's code
 */
public class RegistrationImpl extends Registration.Impl {
    public static final RegistrationImpl IMPL = new RegistrationImpl();

    public static final Map<EntityType<? extends LivingEntity>, Supplier<Builder>> ENTITY_ATTRIBUTES = new HashMap<>();

    private final Map<String, RegistryRepo> repos = new HashMap<>();

    public static void bootstrap() {}

    @SubscribeEvent
    public static void handleEvent(RegistryEvent.Register<Block> event) {

    }

    private RegistryRepo getRepo(String namespace) {
        return repos.computeIfAbsent(namespace, RegistryRepo::new);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> T register(Registry<? super T> registry, Identifier id, T obj) {
        DeferredRegister reg = getRepo(id.getNamespace()).get(registry);
        if (reg != null) {
            reg.register(id.getPath(), () -> obj);
        } else {
            if (obj instanceof IForgeRegistryEntry<?>) {
                ((IForgeRegistryEntry<?>)obj).setRegistryName(id);
            }
            Registry.register(registry, id, obj);
        }
        return obj;
    }

    @Override
    public Supplier<DefaultParticleType> simpleParticle() {
        return () -> new DefaultParticleType(false);
    }

    @Override
    public ItemGroup itemGroup(Identifier id, Supplier<ItemStack> icon) {
        return new ItemGroup(ItemGroup.getGroupCountSafe(), String.format("%s.%s", id.getNamespace(), id.getPath())) {
            @Override
            public ItemStack createIcon() {
                return icon.get();
            }
        };
    }

    @Override
    public Function<Identifier, Tag<Block>> blockTag() {
        return id -> new TagReg<>(id, BlockTags::getTagGroup);
    }

    @Override
    public Function<Identifier, Tag<Item>> itemTag() {
        return id -> new TagReg<>(id, ItemTags::getTagGroup);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Function<Identifier, Activity> activity() {
        return id -> register(Registry.ACTIVITY, id, new Activity(id.toString()));
    }

    @SuppressWarnings("deprecation")
    @Override
    public <T extends Sensor<?>> BiFunction<Identifier, Supplier<T>, SensorType<T>> sensor() {
        return (id, factory) -> register(Registry.SENSOR_TYPE, id, new SensorType<>(factory));
    }

    @SuppressWarnings("deprecation")
    @Override
    public <U> BiFunction<Identifier, Optional<Codec<U>>, MemoryModuleType<U>> memoryModule() {
        return (id, codec) -> register(Registry.MEMORY_MODULE_TYPE, id, new MemoryModuleType<>(codec));
    }

    @Override
    public <T extends LivingEntity> BiFunction<EntityType<T>, Supplier<Builder>, EntityType<T>> defaultEntityAttributes() {
        return (type, attributes) -> {
            ENTITY_ATTRIBUTES.put(type, attributes);
            return type;
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public PoiFactory<PointOfInterestType> poi() {
        return (Identifier id, int ticketCount, int searchDistance, Block...blocks) -> {
            Set<BlockState> setBlocks = Stream.of(blocks).map(Block::getDefaultState).collect(Collectors.toSet());
            return register(Registry.POINT_OF_INTEREST_TYPE, id, new PointOfInterestType(id.toString(), setBlocks, ticketCount, searchDistance));
        };
    }

    @Override
    public ProfessionFactory<VillagerProfession> profession() {
        return (id, poi, sound, items, sites) -> new VillagerProfession(id.toString(), poi, ImmutableSet.copyOf(items),  ImmutableSet.copyOf(sites), sound);
    }

    static class RegistryRepo {
        private final Set<Identifier> skipped = new HashSet<>();
        private final Map<Identifier, DeferredRegister<?>> registries = new HashMap<>();

        private final String namespace;

        public RegistryRepo(String namespace) {
            this.namespace = namespace;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <T> DeferredRegister get(Registry<? super T> registry) {
            Identifier id = registry.getKey().getValue();
            if (!registries.containsKey(id) && !skipped.contains(id)) {
                ForgeRegistry reg = RegistryManager.ACTIVE.getRegistry(id);
                if (reg == null) {
                    skipped.add(id);
                    return null;
                }

                DeferredRegister def = DeferredRegister.create(Objects.requireNonNull(reg, "Registry=" + id), namespace);

                def.register(FMLJavaModLoadingContext.get().getModEventBus());

                registries.put(id, def);
            }

            return registries.get(id);
        }

        void apply(IEventBus bus) {
            registries.values().forEach(bus::register);
        }
    }
}
