package mca.entity.ai.relationship;

import java.util.Optional;
import java.util.stream.Stream;

import mca.server.world.data.FamilyTree;
import mca.server.world.data.FamilyTreeEntry;
import mca.server.world.data.PlayerSaveData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface EntityRelationship {

    default Gender getGender() {
        return Gender.MALE;
    }

    FamilyTree getFamilyTree();

    FamilyTreeEntry getFamily();

    Stream<Entity> getParents();

    Stream<Entity> getSiblings();

    Optional<Entity> getSpouse();

    default void onTragedy(DamageSource cause, RelationshipType type) {
        if (type == RelationshipType.STRANGER) {
            return; // effects don't propagate from strangers
        }

        if (type == RelationshipType.SPOUSE) {
            endMarriage(MarriageState.WIDOW);
        }

        getParents().forEach(parent -> {
            EntityRelationship.of(parent).ifPresent(r -> r.onTragedy(cause, RelationshipType.CHILD));
        });
        getSiblings().forEach(sibling -> {
            EntityRelationship.of(sibling).ifPresent(r -> r.onTragedy(cause, RelationshipType.SIBLING));
        });
        getSpouse().ifPresent(spouse -> {
            EntityRelationship.of(spouse).ifPresent(r -> r.onTragedy(cause, RelationshipType.SPOUSE));
        });
    }

    void endMarriage(MarriageState newState);

    MarriageState getMarriageState();

    boolean isMarried();

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
