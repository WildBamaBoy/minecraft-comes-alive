package mca.api.types;

import java.util.Set;
import java.util.stream.Stream;

import mca.client.gui.Constraint;

/**
 * Button is a button defined in assets/mca/api/gui/*
 * <p>
 * These buttons are dynamically attached to a Screen and include additional instruction/constraints for building
 * and processing interactions.
 */
public record Button (
    /**
     * Unused.
     */
    int id,
    /**
     * The text and action to perform for this button
     */
    String identifier,
    int x,
    int y,
    int width,
    int height,
    /**
     * whether the button press is sent to the server for processing
     */
    boolean notifyServer,
    /**
     * whether the button is processed by the villager or the server itself
     */
    boolean targetServer,
    /**
     * list of EnumConstraints separated by the pipe character |
     */
    String constraints,
    /**
     * Whether the button should be hidden completely when its constraints fail. The default is to simply disable it.
     */
    boolean hideOnFail,
    /**
     * Whether the button is an interaction that generates a response and boosts/decreases hearts
     */
    boolean isInteraction
) {
    public Stream<Constraint> getConstraints() {
        return Constraint.fromStringList(constraints);
    }

    //checks if a map of given evaluated constraints apply to this button
    public boolean isValidForConstraint(Set<Constraint> constraints) {
        return getConstraints().allMatch(constraints::contains);
    }
}