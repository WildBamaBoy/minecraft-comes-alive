package mca.enums;

public enum Rank {
    OUTLAW(0, Integer.MIN_VALUE, 0),
    PEASANT(1, 0, 0),
    MERCHANT(2, 20, 1),
    NOBE(3, 40, 3),
    MAYOR(4, 60, 4),
    MING(5, 80, 4);

    final int id;
    final int reputation;
    final int tasks;

    Rank(int id, int reputation, int tasks) {
        this.id = id;
        this.reputation = reputation;
        this.tasks = tasks;
    }

    public static Rank fromRank(int rank) {
        for (Rank r : Rank.values()) {
            if (r.id == rank) {
                return r;
            }
        }
        return OUTLAW;
    }

    public static Rank fromReputation(int rep) {
        Rank rank = OUTLAW;
        for (Rank r : Rank.values()) {
            if (rep >= r.reputation && r.reputation > rank.reputation) {
                rank = r;
            }
        }
        return rank;
    }

    public int getId() {
        return id;
    }

    public int getReputation() {
        return reputation;
    }

    public int getTasks() {
        return tasks;
    }
}
