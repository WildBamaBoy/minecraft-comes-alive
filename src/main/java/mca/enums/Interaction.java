package mca.enums;

import mca.entity.VillagerEntityMCA;
import mca.entity.data.Memories;

public enum Interaction {
    CHAT("chat", 70, 4),
    JOKE("joke", 60, 5),
    SHAKE_HAND("handshake", 80, 3),
    TELL_STORY("tellstory", 50, 7),
    FLIRT("flirt", 50, 8),
    HUG("hug", 30, 9),
    KISS("kiss", 15, 10);

    private final String name;
    private final int baseChance;
    private final int baseHearts;

    Interaction(String name, int baseChance, int baseHearts) {
        this.name = name;
        this.baseChance = baseChance;
        this.baseHearts = baseHearts;
    }

    public static Interaction fromName(String name) {
        for (Interaction interaction : Interaction.values()) {
            if (interaction.name.equals(name)) {
                return interaction;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public int getBaseChance() {
        return baseChance;
    }

    public int getBaseHearts() {
        return baseHearts;
    }

    public int getBonusChanceForCurrentPoints(int hearts) {
        int returnAmount = 0;

        switch (this) {
            case FLIRT:
                returnAmount = hearts >= 100 ? 25 : hearts >= 90 ? 20 : hearts >= 80 ? 15 : hearts >= 70 ? 10 : hearts >= 60 ? 5 : 0;
                break;
            case HUG:
                returnAmount = hearts >= 100 ? 50 : hearts >= 90 ? 35 : hearts >= 80 ? 20 : hearts >= 70 ? 15 : hearts >= 60 ? 10 : 0;
                break;
            case KISS:
                returnAmount = hearts >= 100 ? 80 : hearts >= 90 ? 55 : hearts >= 80 ? 40 : hearts >= 70 ? 30 : hearts >= 60 ? 20 : 0;
                break;
        }

        return returnAmount;
    }

    public int getSuccessChance(VillagerEntityMCA villager, Memories memory) {
        return getBaseChance()
                - memory.getInteractionFatigue() * 5
                + memory.getHearts() / 5
                + villager.getPersonality().getSuccessModifierForInteraction(this)
                + villager.getMood().getSuccessModifierForInteraction(this)
                + villager.getMood().getMoodGroup().getSuccessModifierForInteraction(this)
                + getBonusChanceForCurrentPoints(memory.getHearts());
    }

    public int getHearts(VillagerEntityMCA villager) {
        return getBaseHearts()
                + villager.getPersonality().getHeartsModifierForInteraction(this)
                + villager.getMood().getHeartsModifierForInteraction(this)
                + villager.getMood().getMoodGroup().getHeartsModifierForInteraction(this);
    }
}
