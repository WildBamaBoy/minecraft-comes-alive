package mca.entity.ai;

public enum Rank {
    OUTLAW(Integer.MIN_VALUE, 0),
    PEASANT(0, 0),
    MERCHANT(20, 1),
    NOBLE(40, 2),
    MAYOR(60, 3),
    KING(80, 4);

    private static final Rank[] VALUES = values();

    private final int reputation;
    private final int tasks;

    Rank(int reputation, int tasks) {
        this.reputation = reputation;
        this.tasks = tasks;
    }

    public int getReputation() {
        return reputation;
    }

    public int getTasks() {
        return tasks;
    }

    /**
     * Returns the highest available rank for a given reputation and task progress.
     */
    public static Rank getRank(int completedTasks, int reputation) {
        for (int i = VALUES.length - 1; i >= 0; i--) {
            if (reputation >= VALUES[i].reputation && completedTasks >= VALUES[i].getTasks()) {
                return VALUES[i];
            }
        }
        return OUTLAW;
    }

    public Rank promote() {
        if (ordinal() + 1 < VALUES.length) {
            return VALUES[ordinal() + 1];
        } else {
            return null;
        }
    }
}
