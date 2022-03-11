package mca.entity;

import java.util.Optional;
import java.util.OptionalInt;

import mca.MCA;
import mca.entity.ai.relationship.Gender;
import mca.resources.API;
import mca.util.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;

public class ZombieVillagerFactory {
    private final World world;

    private Optional<String> name = Optional.empty();
    private Optional<Gender> gender = Optional.empty();

    private Optional<VillagerProfession> profession = Optional.empty();
    private Optional<VillagerType> type = Optional.empty();
    private OptionalInt level = OptionalInt.empty();

    private Optional<Vec3d> position = Optional.empty();

    private ZombieVillagerFactory(World world) {
        this.world = world;
    }

    public static ZombieVillagerFactory newVillager(World world) {
        return new ZombieVillagerFactory(world);
    }

    public ZombieVillagerFactory withGender(Gender gender) {
        this.gender = Optional.ofNullable(gender);
        return this;
    }

    public ZombieVillagerFactory withType(VillagerType type) {
        this.type = Optional.ofNullable(type);
        return this;
    }

    public ZombieVillagerFactory withProfession(VillagerProfession prof) {
        this.profession = Optional.ofNullable(prof);
        return this;
    }

    public ZombieVillagerFactory withProfession(VillagerProfession prof, int level) {
        withProfession(prof);
        this.level = OptionalInt.of(level);
        return this;
    }

    public ZombieVillagerFactory withName(String name) {
        this.name = Optional.ofNullable(name);
        return this;
    }

    public ZombieVillagerFactory withPosition(double x, double y, double z) {
        return withPosition(new Vec3d(x, y, z));
    }

    public ZombieVillagerFactory withPosition(Entity entity) {
        return withPosition(entity.getX(), entity.getY(), entity.getZ());
    }

    public ZombieVillagerFactory withPosition(Vec3d pos) {
        position = Optional.of(pos);
        return this;
    }

    public ZombieVillagerFactory withPosition(BlockPos pos) {
        return withPosition(Vec3d.ofBottomCenter(pos.up()));
    }

    public ZombieVillagerFactory spawn(SpawnReason reason) {
        if (!position.isPresent()) {
            MCA.LOGGER.info("Attempted to spawn villager without a position being set!");
        }

        WorldUtils.spawnEntity(world, build(), reason);
        return this;
    }

    public ZombieVillagerEntityMCA build() {
        Gender gender = this.gender.orElseGet(Gender::getRandom);
        ZombieVillagerEntityMCA zombie = gender.getZombieType().create(world);
        zombie.getGenetics().setGender(gender);
        zombie.setName(name.orElseGet(() -> API.getVillagePool().pickCitizenName(gender)));
        position.ifPresent(pos -> zombie.updatePosition(pos.getX(), pos.getY(), pos.getZ()));
        VillagerData data = zombie.getVillagerData();
        zombie.setVillagerData(new VillagerData(
                        type.orElseGet(data::getType),
                        profession.orElse(VillagerProfession.NONE),
                        level.orElseGet(data::getLevel)
                )
        );
        return zombie;
    }
}
