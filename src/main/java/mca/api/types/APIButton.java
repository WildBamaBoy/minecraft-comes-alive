package mca.api.types;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumConstraint;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * APIButton is a button defined in assets/mca/api/gui/*
 * <p>
 * These buttons are dynamically attached to a GuiScreen and include additional instruction/constraints for building
 * and processing interactions.
 */
public class APIButton {
    private int id;             //numeric id
    private String identifier;  //string identifier for the button in the .lang file
    private int x;              //x position
    private int y;              //y position
    private int width;          //button width
    private int height;         //button height
    private boolean notifyServer;   //whether the button press is sent to the server for processing
    private boolean targetServer;   //whether the button is processed by the villager or the server itself
    private String constraints;     //list of EnumConstraints separated by a pipe character |
    private boolean isInteraction;  //whether the button is an interaction that generates a response and boosts/decreases hearts

    public APIButton(int id, String identifier, int x, int y, int width, int height, boolean notifyServer, boolean targetServer, String constraints, boolean isInteraction) {
        this.id = id;
        this.identifier = identifier;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.notifyServer = notifyServer;
        this.targetServer = targetServer;
        this.constraints = constraints;
        this.isInteraction = isInteraction;
    }

    public int getId() {
        return id;
    }

    public String getLangId() {
        return identifier;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean getNotifyServer() {
        return notifyServer;
    }

    public boolean getTargetServer() {
        return targetServer;
    }

    public List<EnumConstraint> getConstraints() {
        return EnumConstraint.fromStringList(constraints);
    }

    /**
     * Determines if the given villager and player match the constraints for this button, allowing the action to be performed
     *
     * @param villager Instance of the EntityVillagerMCA the button would perform the action on
     * @param player   Instance of the EntityPlayer performing the action
     * @return boolean whether the button is valid for a constraint
     */
    public boolean isValidForConstraint(EntityVillagerMCA villager, EntityPlayer player) {
        List<EnumConstraint> constraints = getConstraints();

        if (constraints.contains(EnumConstraint.ADULTS) && !villager.isChild()) {
            return true;
        } else if (constraints.contains(EnumConstraint.ROMANTIC) && !villager.isChild() && (!villager.isMarried() || villager.isMarriedTo(player.getUniqueID()))) {
            return true;
        } else if (constraints.contains(EnumConstraint.SPOUSE) && villager.isMarriedTo(player.getUniqueID())) {
            return true;
        } else if (constraints.contains(EnumConstraint.NOT_SPOUSE) && !villager.isMarriedTo(player.getUniqueID())) {
            return true;
        } else if (constraints.contains(EnumConstraint.FAMILY) && (villager.playerIsParent(player) || villager.isMarriedTo(player.getUniqueID()))){
            return true;
        } else if (constraints.contains(EnumConstraint.NOT_FAMILY) && !(villager.playerIsParent(player) || villager.isMarriedTo(player.getUniqueID()))) {
            return true;
        } else if (constraints.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean getIsInteraction() {
        return this.isInteraction;
    }
}
