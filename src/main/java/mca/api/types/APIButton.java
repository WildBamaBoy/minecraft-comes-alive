package mca.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.enums.Constraint;

import java.util.List;
import java.util.Map;

/**
 * APIButton is a button defined in assets/mca/api/gui/*
 * <p>
 * These buttons are dynamically attached to a Screen and include additional instruction/constraints for building
 * and processing interactions.
 */
@AllArgsConstructor
public class APIButton {
    @Getter
    private final int id;             // numeric id
    @Getter
    private final String identifier;  // string identifier for the button in the .lang file
    @Getter
    private final int x;              // x position
    @Getter
    private final int y;              // y position
    @Getter
    private final int width;          // button width
    @Getter
    private final int height;         // button height
    @Getter
    private final boolean notifyServer;   // whether the button press is sent to the server for processing
    @Getter
    private final boolean targetServer;   // whether the button is processed by the villager or the server itself
    private final String constraints;     // list of EnumConstraints separated by a pipe character |
    @Getter
    private final boolean isInteraction;  // whether the button is an interaction that generates a response and boosts/decreases hearts

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
