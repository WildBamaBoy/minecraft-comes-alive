package mca.entity.ai.relationship;

import mca.entity.EntityWrapper;

public interface CompassionateEntity<T extends EntityRelationship> extends EntityWrapper {
    T getRelationships();
}
