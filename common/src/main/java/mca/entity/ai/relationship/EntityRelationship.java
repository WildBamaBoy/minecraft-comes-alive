package mca.entity.ai.relationship;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import mca.server.world.data.FamilyTree;
import mca.server.world.data.FamilyTreeEntry;
import mca.server.world.data.PlayerSaveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

public interface EntityRelationship {

    default Gender getGender() {
        return Gender.MALE;
    }

    FamilyTree getFamilyTree();

    FamilyTreeEntry getFamilyEntry();

    Stream<Entity> getFamily(int parents, int children);

    Stream<Entity> getParents();

    Stream<Entity> getSiblings();

    Optional<Entity> getSpouse();

    default void onTragedy(DamageSource cause, @Nullable BlockPos burialSite, RelationshipType type) {
        if (type == RelationshipType.STRANGER) {
            return; // effects don't propagate from strangers
        }

        if (type == RelationshipType.SPOUSE) {
            endMarriage(MarriageState.WIDOW);
        }

        getParents().forEach(parent -> {
            EntityRelationship.of(parent).ifPresent(r -> r.onTragedy(cause, burialSite, RelationshipType.CHILD));
        });
        getSiblings().forEach(sibling -> {
            EntityRelationship.of(sibling).ifPresent(r -> r.onTragedy(cause, burialSite, RelationshipType.SIBLING));
        });
        getSpouse().ifPresent(spouse -> {
            EntityRelationship.of(spouse).ifPresent(r -> r.onTragedy(cause, burialSite, RelationshipType.SPOUSE));
        });
    }

    void endMarriage(MarriageState newState);

    MarriageState getMarriageState();

    Optional<UUID> getSpouseUuid();

    Optional<Text> getSpouseName();

    default boolean isMarried() {
        return !getSpouseUuid().orElse(Util.NIL_UUID).equals(Util.NIL_UUID);
    }

    default boolean isMarriedTo(UUID uuid) {
        return getSpouseUuid().orElse(Util.NIL_UUID).equals(uuid);
    }

    static Optional<EntityRelationship> of(Entity entity) {

        if (entity instanceof PlayerEntity && !entity.world.isClient) {
            return Optional.ofNullable(PlayerSaveData.get((ServerWorld)entity.world, entity.getUuid()));
        }

        if (entity instanceof CompassionateEntity) {
            return Optional.of(((CompassionateEntity<?>)entity).getRelationships());
        }

        return Optional.empty();
    }
}