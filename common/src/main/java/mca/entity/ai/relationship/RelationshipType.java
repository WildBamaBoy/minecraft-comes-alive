package mca.entity.ai.relationship;

public enum RelationshipType {
    STRANGER(1),
    SELF(2),
    SIBLING(2),
    SPOUSE(3),
    PARENT(3),
    CHILD(4);

    private final int proximity;

    RelationshipType(int proximity) {
        this.proximity = proximity;
    }

    /**
     * High proximity creates a smaller effect.
     */
    public int getInverseProximity() {
        return this == STRANGER ? 5 : 1;
    }

    public int getProximityAmplifier() {
        return proximity;
    }
}