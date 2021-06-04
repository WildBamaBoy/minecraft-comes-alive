package mca.enums;

public enum EnumRank {
    OUTLAW(0, Integer.MIN_VALUE, 0),
    PEASANT(1, 0, 0),
    MERCHANT(2, 20, 1),
    NOBE(3, 40, 3),
    MAYOR(4, 60, 4),
    MING(5, 80, 4);

    int id;
    int reputation;
    int tasks;

    EnumRank(int id, int reputation, int tasks) {
        this.id = id;
        this.reputation = reputation;
        this.tasks = tasks;
    }

    public static EnumRank fromRank(int rank) {
        for (EnumRank r : EnumRank.values()) {
            if (r.id == rank) {
                return r;
            }
        }
        return OUTLAW;
    }

    public static EnumRank fromReputation(int rep) {
        EnumRank rank = OUTLAW;
        for (EnumRank r : EnumRank.values()) {
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
