package mca.cobalt.registration;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import mca.cobalt.registration.Registration.PoiFactory;
import mca.cobalt.registration.Registration.ProfessionFactory;
import mca.mixin.MixinSensorType;
import mca.mixin.MixinActivity;
import mca.mixin.MixinMemoryModuleType;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.villager.VillagerProfessionBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.tag.TagRegistry;
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
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class RegistrationImpl extends Registration.Impl {
    @Override
    public <T> T register(Registry<? super T> registry, Identifier id, T obj) {
        return Registry.register(registry, id, obj);
    }

    @Override
    public Supplier<DefaultParticleType> simpleParticle() {
        return FabricParticleTypes::simple;
    }

    @Override
    public ItemGroup itemGroup(Identifier id, Supplier<ItemStack> icon) {
        return FabricItemGroupBuilder.create(id).icon(icon).build();
    }

    @Override
    public Function<Identifier, Tag<Block>> blockTag() {
        return TagRegistry::block;
    }

    @Override
    public Function<Identifier, Tag<Item>> itemTag() {
        return TagRegistry::item;
    }

    @Override
    public Function<Identifier, Activity> activity() {
        return id -> MixinActivity.register(id.toString());
    }

    @Override
    public <T extends Sensor<?>> BiFunction<Identifier, Supplier<T>, SensorType<T>> sensor() {
        return (id, factory) -> MixinSensorType.register(id.toString(), factory);
    }

    @Override
    public <U> BiFunction<Identifier, Optional<Codec<U>>, MemoryModuleType<U>> memoryModule() {
        return (id, codec) -> register(Registry.MEMORY_MODULE_TYPE, id, MixinMemoryModuleType.init(codec));
    }

    @Override
    public <T extends LivingEntity> BiFunction<EntityType<T>, Supplier<Builder>, EntityType<T>> defaultEntityAttributes() {
        return (type, attributes) -> {
            FabricDefaultAttributeRegistry.register(type, attributes.get());
            return type;
        };
    }

    @Override
    public PoiFactory<PointOfInterestType> poi() {
        return PointOfInterestHelper::register;
    }

    @Override
    public ProfessionFactory<VillagerProfession> profession() {
        return (id, poi, sound, items, sites) -> VillagerProfessionBuilder.create().id(id).workstation(poi).workSound(sound).harvestableItems(items).secondaryJobSites(sites).build();
    }
}
