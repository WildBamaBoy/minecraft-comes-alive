package com.minecraftcomesalive.mca.api.objects;

import com.minecraftcomesalive.mca.api.IInteraction;
import com.minecraftcomesalive.mca.core.MCA;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * APIButton is a button defined in assets/mca/api/gui/*
 * <p>
 * These buttons are dynamically attached to a GuiScreen and include additional instruction/constraints for building
 * and processing interactions.
 */
@AllArgsConstructor
public class APIButton {
    @Getter private int id;             // numeric id
    @Getter private String identifier;  // string identifier for the button in the .lang file
    @Getter private int x;              // x position
    @Getter private int y;              // y position
    @Getter private int width;          // button width
    @Getter private int height;         // button height
    private String interactionClass;    // fully qualified path of the class used to handle this interaction, should implelment IInteraction.

    public IInteraction getInteraction() {
        try {
            return (IInteraction) Class.forName(interactionClass).newInstance();
        } catch (Exception e) {
            MCA.logAndThrow("Unable to find class for interaction.", e);
        }
        return null;
    }
}
