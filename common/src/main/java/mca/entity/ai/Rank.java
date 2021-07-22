package mca.entity.ai;

public enum Rank {
    OUTLAW  (Integer.MIN_VALUE, 0),
    PEASANT (0, 0),
    MERCHANT(20, 1),
    NOBLE   (40, 3),
    MAYOR   (60, 4),
    KING    (80, 4);

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
     * Returns the highest available rank for a given reputation.
     */
    private static Rank fromReputation(int rep) {
        for (int i = VALUES.length - 1; i >= 0; i--) {
            if (VALUES[i].reputation >= rep) {
                return VALUES[i];
            }
        }
        return OUTLAW;
    }

    public static Rank getClamped(int completedTasks, int reputation) {
        Rank maxRank = fromReputation(reputation);

        // searches all ranks below the maximum
        for (int i = 0; i <= maxRank.ordinal(); i++) {
            Rank r = VALUES[i];

            if (completedTasks < r.getTasks()) {
                return r; // finds the first one with a higher task count that is completed
            }
        }

        return maxRank;
    }

}
