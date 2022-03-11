package mca.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;

public interface Infectable {
    /**
     * Entity has fully converted into a zombie. Noo!
     */
    float MAX_INFECTION = 255F;
    float INITIAL_INFECTION_AMOUNT = 1F;
    /**
     * No infection has started yet. Yay!
     */
    float MIN_INFECTION = 0F;

    float FEVER_THRESHOLD = 80F;
    float BABBLING_THRESHOLD = 150F;
    float POINT_OF_NO_RETURN = 200F;

    default boolean isInfected() {
        return getInfectionProgress() > MIN_INFECTION;
    }

    default void setInfected(boolean infected) {
        setInfectionProgress(infected ? Math.max(getInfectionProgress(), INITIAL_INFECTION_AMOUNT) : MIN_INFECTION);
    }

    default boolean canBeTargetedBy(Entity mob) {
        return this instanceof HostileEntity
            || !(mob instanceof HostileEntity)
            || getInfectionProgress() < POINT_OF_NO_RETURN;
    }

    /**
     * A value from {@literal MIN_INFECTION} to {@literal MAX_INFECTION} indicating how far along the infection has progressed.
     */
    float getInfectionProgress();

    /**
     * Used for lerping. Nothing else.
     */
    default float getPrevInfectionProgress() {
        return getInfectionProgress();
    }

    void setInfectionProgress(float progress);
}
