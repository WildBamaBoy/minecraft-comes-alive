package mca.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mca.entity.VillagerEntityMCA;
import mca.enums.Constraint;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

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

    /**
     * Determines if the given villager and player match the constraints for this button, allowing the action to be performed
     *
     * @param villager Instance of the EntityVillagerMCA the button would perform the action on
     * @param player   Instance of the PlayerEntity performing the action
     * @return boolean whether the button is valid for a constraint
     */
    public boolean isValidForConstraint(VillagerEntityMCA villager, PlayerEntity player) {
        List<Constraint> constraints = getConstraints();

        for (Constraint constraint : constraints) {
            if (constraint.getCheck().test(villager, player)) {
                return false;
            }
        }
        return true;
    }
}
