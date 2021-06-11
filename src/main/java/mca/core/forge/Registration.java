package mca.core.forge;

import mca.core.MCA;
import mca.core.minecraft.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = create(ForgeRegistries.BLOCKS);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = create(ForgeRegistries.CONTAINERS);
    public static final DeferredRegister<Item> ITEMS = create(ForgeRegistries.ITEMS);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = create(ForgeRegistries.RECIPE_SERIALIZERS);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = create(ForgeRegistries.TILE_ENTITIES);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = create(ForgeRegistries.PARTICLE_TYPES);
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = create(ForgeRegistries.MEMORY_MODULE_TYPES);
    public static final DeferredRegister<Activity> ACTIVITIES = create(ForgeRegistries.ACTIVITIES);
    public static final DeferredRegister<Schedule> SCHEDULES = create(ForgeRegistries.SCHEDULES);
    public static final DeferredRegister<PointOfInterestType> POI_TYPES = create(ForgeRegistries.POI_TYPES);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = create(ForgeRegistries.PROFESSIONS);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = create(ForgeRegistries.SOUND_EVENTS);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = create(ForgeRegistries.ENTITIES);

    public static void register() {
        EntitiesMCA.init();
        BlocksMCA.register();
        ContainerTypesMCA.register();
        ItemsMCA.register();
        RecipeSerializersMCA.register();
        TileEntityTypesMCA.register();
        MemoryModuleTypeMCA.init();
        ActivityMCA.init();
        ParticleTypesMCA.init();
        SchedulesMCA.init();
        MessagesMCA.register();
        ProfessionsMCA.register();
        //TODO (PointOfInterestTypeMCA, SoundsMCA) register()
        PointOfInterestTypeMCA.register();
        SoundsMCA.register();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modEventBus);
        PROFESSIONS.register(modEventBus);
        BLOCKS.register(modEventBus);
        CONTAINERS.register(modEventBus);
        ITEMS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);
        POI_TYPES.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        MEMORY_MODULE_TYPES.register(modEventBus);
        ACTIVITIES.register(modEventBus);
        SCHEDULES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
    }

    private static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> create(IForgeRegistry<T> registry) {
        return DeferredRegister.create(registry, MCA.MOD_ID);
    }
}
