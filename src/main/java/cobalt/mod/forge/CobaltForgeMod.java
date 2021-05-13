package cobalt.mod.forge;

import cobalt.minecraft.entity.merchant.villager.CVillagerProfession;
import cobalt.mod.CobaltMod;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Date;

/**
 * Base Cobalt mod supporting Forge. Any mods on Forge wishing to use Cobalt should extend this class.
 */
public abstract class CobaltForgeMod extends CobaltMod {
    private final ArrayList<DeferredRegister<?>> loadedRegistries = new ArrayList<>();

    private final DeferredRegister<Item> itemRegistry;
    private final DeferredRegister<Block> blockRegistry;
    private final DeferredRegister<EntityType<?>> entityRegistry;
    public final DeferredRegister<VillagerProfession> villagerProfessionRegistry;

    public CobaltForgeMod() {
        super();

        // Add any FML event listeners
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStarting);
        MinecraftForge.EVENT_BUS.register(this);

        itemRegistry = getRegistry(ForgeRegistries.ITEMS);
        blockRegistry = getRegistry(ForgeRegistries.BLOCKS);
        entityRegistry = getRegistry(ForgeRegistries.ENTITIES);
        villagerProfessionRegistry = getRegistry(ForgeRegistries.PROFESSIONS);

        loadRegistries();
        registerContent(); // And allow the mod to register all of its content in the constructor.
    }

    /**
     * Gets a Forge registry of the given type. Creates a DeferredRegister and returns it.
     * You should only call getRegistry() once, in an overridden #loadRegistries() method. Save the returned DeferredRegister somewhere.
     * If you call this more than once, you'll be yelled at with a RuntimeException.
     *
     * @param registryType The desired ForgeRegistry to load.
     * @see net.minecraftforge.registries.ForgeRegistries
     */
    public <T extends IForgeRegistryEntry<T>> DeferredRegister<T> getRegistry(IForgeRegistry<T> registryType) {
        DeferredRegister<T> returnRegistry = null;

        // Look up a previously loaded registry first.
        for (DeferredRegister<?> registry : loadedRegistries) {
            ForgeRegistry<?> type = ObfuscationReflectionHelper.getPrivateValue(DeferredRegister.class, registry, "type");
            if (registryType == type) {
                // Initially we would save a loaded registry and recall it from the list when appropriate, but
                // when we did this, only the first object entered in the registry would actually be registered in-game.
                // Now just throw an exception since this is an error on the mod's part.
                throw new RuntimeException("You have loaded a registry twice. Save your instance of the registry in your getRegistry() call. Registration will not work properly when a registry is reloaded from memory.");
            }
        }

        // If we didn't find a previously loaded registry, we create a new deferred register and store it.
        if (returnRegistry == null) {
            returnRegistry = DeferredRegister.create(registryType, getModId());
            returnRegistry.register(FMLJavaModLoadingContext.get().getModEventBus()); // Make sure Forge actually hits our registry.
            loadedRegistries.add(DeferredRegister.create(registryType, getModId()));
        }

        return returnRegistry;
    }

    /**
     * Registers an entity with Forge under this mod's ID.
     *
     * @param factory        Method reference to your entity's constructor. ex: MyEntity::new
     * @param factory  Method reference to your entity renderer's constructor. ex: MyEntityRenderer::new
     * @param classification Your desired EntityClassification for your entity.
     * @param entityId       An identifier for your entity. This is used with your mod ID to define the entity's ResourceLocation.
     * @param width          Your entity's width
     * @param height         Your entity's height
     * @return A RegistryObject containing your registered entity. This <b>CANNOT BE USED IMMEDIATELY</b> as its actual value (.get()) is null.
     * Remember this function uses deferred registers which are fired by Forge when appropriate. You will be able to use the returned
     * RegistryObject in the setup() method and afterwards.
     */
    public <T extends LivingEntity> RegistryObject<EntityType<T>> registerEntity(EntityType.IFactory<T> factory, EntityClassification classification, String entityId, float width, float height) {
        return entityRegistry.register(entityId, () ->
                EntityType.Builder.of(factory, classification).sized(width, height).build(new ResourceLocation(getModId(), entityId).toString())
        );
    }

    /**
     * Registers an item with Forge under this mod's ID.
     *
     * @param itemId A unique identifier for the item.
     * @param cItem  Wrapped Cobalt item to register.
     * @return A RegistryObject containing your registered item. This <b>CANNOT BE USED IMMEDIATELY.</b> See registerEntity().
     */
    public final RegistryObject<Item> registerItem(String itemId, Item cItem) {
        return itemRegistry.register(itemId, () -> cItem);
    }

    /**
     * Registers a profession with Forge.
     *
     * @param name       A unique name for the profession.
     * @param poiType    The profession's PointOfInterestType.
     * @param soundEvent A sound for the profession.
     * @return The registered profession wrapped by Cobalt.
     * @see CVillagerProfession
     */
    public final RegistryObject<VillagerProfession> registerProfession(String name, PointOfInterestType poiType, SoundEvent soundEvent) {
        return villagerProfessionRegistry.register(name, () -> CVillagerProfession.createNew(name, poiType, soundEvent).getMcProfession());
    }

    /*
        FML events that pass to abstract events
    */

    /**
     * Initializes the mod and passes execution to the subclass implementation of onSetup()
     *
     * @param event Forge's FMLCommonSetupEvent
     */
    public final void setup(FMLCommonSetupEvent event) {
        startupTimestamp = new Date().getTime();
        logger = LogManager.getLogger();
        onSetup();
    }

    /**
     * Initializes the mod client-side and passes execution to the subclass implementation of onClientSetup()
     *
     * @param event Forge's FMLClientSetupEvent.
     */
    public final void clientSetup(FMLClientSetupEvent event) {
        onClientSetup();
    }

    public final void serverStarting(FMLServerStartingEvent event) {
        registerCommands(event);
    }

    /**
     * Override and call getRegistry() in this function on any ForgeRegistries your mod uses if Cobalt doesn't provide a register function
     * for your particular object, or if you just want to register it yourself.
     */
    public void loadRegistries() {
    }

    /**
     * Override and register any commands your mod uses here.
     */
    public void registerCommands(FMLServerStartingEvent event) {
    }
}
