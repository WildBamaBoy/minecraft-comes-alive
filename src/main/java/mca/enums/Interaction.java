package mca.enums;

import mca.entity.ai.brain.VillagerBrain;
import mca.entity.data.Memories;

public enum Interaction {
    CHAT(70, 4),
    JOKE(60, 5),
    SHAKE_HAND(80, 3),
    TELL_STORY(50, 7),
    FLIRT(50, 8),
    HUG(30, 9),
    KISS(15, 10);

    private final String name;
    private final int baseChance;
    private final int baseHearts;

    Interaction(int baseChance, int baseHearts) {
        this.name = name().toLowerCase().replaceAll("_", "");
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
            default:
        }

        return returnAmount;
    }

    public int getSuccessChance(VillagerBrain brain, Memories memory) {
        return baseChance
                - memory.getInteractionFatigue() * 5
                + memory.getHearts() / 5
                + brain.getPersonality().getSuccessModifierForInteraction(this)
                + brain.getMood().getSuccessModifierForInteraction(this)
                + brain.getMood().getMoodGroup().getSuccessModifierForInteraction(this)
                + getBonusChanceForCurrentPoints(memory.getHearts());
    }

    public int getHearts(VillagerBrain brain) {
        return baseHearts
                + brain.getPersonality().getHeartsModifierForInteraction(this)
                + brain.getMood().getHeartsModifierForInteraction(this)
                + brain.getMood().getMoodGroup().getHeartsModifierForInteraction(this);
    }
}
