package mca.api.types;

import mca.enums.Constraint;

import java.util.List;
import java.util.Map;

/**
 * APIButton is a button defined in assets/mca/api/gui/*
 * <p>
 * These buttons are dynamically attached to a Screen and include additional instruction/constraints for building
 * and processing interactions.
 */
public record APIButton (
    int id,             // numeric id
    String identifier,  // string identifier for the button in the .lang file
    int x,              // x position
    int y,              // y position
    int width,          // button width
    int height,         // button height
    boolean notifyServer,   // whether the button press is sent to the server for processing
    boolean targetServer,   // whether the button is processed by the villager or the server itself
    String constraints,     // list of EnumConstraints separated by a pipe character |
    boolean isInteraction  // whether the button is an interaction that generates a response and boosts/decreases hearts
) {
    public List<Constraint> getConstraints() {
        return Constraint.fromStringList(constraints);
    }

    //checks if a map of given evaluated constraints apply to this button
    public boolean isValidForConstraint(Map<String, Boolean> checkedConstraints) {
        List<Constraint> constraints = getConstraints();
        for (Constraint constraint : constraints) {
            if (checkedConstraints.get(constraint.getId())) {
                return false;
            }
        }
        return true;
    }
}
