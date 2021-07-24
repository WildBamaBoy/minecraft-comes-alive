package mca.client.gui;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Button is a button defined in assets/mca/api/gui/*
 * <p>
 * These buttons are dynamically attached to a Screen and include additional instruction/constraints for building
 * and processing interactions.
 */
public final class Button {
    /**
     * The text and action to perform for this button
     */
    private final String identifier;
    public String identifier() {return identifier;}
    private final int x;
    public int x() {return x;}
    private final int y;
    public int y() {return y;}
    private final int width;
    public int width() {return width;}
    private final int height;
    public int height() {return height;}
    /**
     * whether the button press is sent to the server for processing
     */
    private final boolean notifyServer;
    public boolean notifyServer() {return notifyServer;}
    /**
     * whether the button is processed by the villager or the server itself
     */
    private final boolean targetServer;
    public boolean targetServer() {return targetServer;}
    /**
     * list of EnumConstraints separated by the pipe character |
     */
    private final String constraints;
    public String constraints() {return constraints;}
    /**
     * Whether the button should be hidden completely when its constraints fail. The default is to simply disable it.
     */
    private final boolean hideOnFail;
    public boolean hideOnFail() {return hideOnFail;}
    /**
     * Whether the button is an interaction that generates a response and boosts/decreases hearts
     */
    private final boolean isInteraction;
    public boolean isInteraction() {return isInteraction;}

    public Button(    /**
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
    boolean isInteraction) {
        this.identifier = identifier;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.notifyServer = notifyServer;
        this.targetServer = targetServer;
        this.constraints = constraints;
        this.hideOnFail = hideOnFail;
        this.isInteraction = isInteraction;
    }

    public Stream<Constraint> getConstraints() {
        return Constraint.fromStringList(constraints).stream();
    }

    //checks if a map of given evaluated constraints apply to this button
    public boolean isValidForConstraint(Set<Constraint> constraints) {
        return getConstraints().allMatch(constraints::contains);
    }
}