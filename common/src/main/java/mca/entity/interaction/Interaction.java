package mca.entity.interaction;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mca.entity.ai.Memories;
import mca.entity.ai.brain.VillagerBrain;

public enum Interaction {
    CHAT(70, 4),
    JOKE(60, 5),
    SHAKE_HAND(80, 3),
    TELL_STORY(50, 7),
    FLIRT(50, 8),
    HUG(30, 9),
    KISS(15, 10);

    private static final Map<String, Interaction> REGISTRY = Stream.of(values()).collect(Collectors.toMap(Interaction::name, Function.identity()));

    private final int baseChance;
    private final int baseHearts;

    Interaction(int baseChance, int baseHearts) {
        this.baseChance = baseChance;
        this.baseHearts = baseHearts;
    }

    public static Optional<Interaction> byCommand(String action) {
        return Optional.ofNullable(REGISTRY.get(action.toUpperCase()));
    }

    public int getBonusChanceForCurrentPoints(int hearts) {
        switch (this) {
            case FLIRT:
                return hearts >= 100 ? 25
                     : hearts >= 90 ? 20
                     : hearts >= 80 ? 15
                     : hearts >= 70 ? 10
                     : hearts >= 60 ? 5
                     : 0;
            case HUG:
                return hearts >= 100 ? 50
                     : hearts >= 90 ? 35
                     : hearts >= 80 ? 20
                     : hearts >= 70 ? 15
                     : hearts >= 60 ? 10
                     : 0;
            case KISS:
                return hearts >= 100 ? 80
                     : hearts >= 90 ? 55
                     : hearts >= 80 ? 40
                     : hearts >= 70 ? 30
                     : hearts >= 60 ? 20
                     : 0;
            default:
                return 0;
        }
    }

    public int getSuccessChance(VillagerBrain<?> brain, Memories memory) {
        return baseChance
                - memory.getInteractionFatigue() * 5
                + brain.getPersonality().getSuccessModifierForInteraction(this)
                + brain.getMood().getSuccessModifierForInteraction(this)
                + brain.getMood().getMoodGroup().getSuccessModifierForInteraction(this)
                + getBonusChanceForCurrentPoints(memory.getHearts());
    }

    public int getHearts(VillagerBrain<?> brain) {
        return baseHearts
                + brain.getPersonality().getHeartsModifierForInteraction(this)
                + brain.getMood().getHeartsModifierForInteraction(this)
                + brain.getMood().getMoodGroup().getHeartsModifierForInteraction(this);
    }
}
