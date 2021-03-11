package com.minecraftcomesalive.mca.interaction.base;

import cobalt.minecraft.entity.player.CPlayer;
import com.minecraftcomesalive.mca.api.IInteraction;
import com.minecraftcomesalive.mca.core.Config;
import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
import com.minecraftcomesalive.mca.entity.data.Memories;

public abstract class AbstractBaseInteraction implements IInteraction {
    @Override
    public final void run(EntityVillagerMCA villager, Memories memories, CPlayer player) {
        boolean succeeded = villager.getRNG().nextFloat() < getSuccessChance(memories);
        memories.modHearts(succeeded ? getHeartsModifier() : getHeartsModifier() * -1);
        memories.modInteractionFatigue(1);

        if (succeeded) {
            villager.say(player, getLangPrefix() + ".success");
        } else {
            villager.say(player, getLangPrefix() + ".fail");
        }
    }

    @Override
    public boolean isValidFor(EntityVillagerMCA villager, CPlayer player) {
        return true;
    }

    public float getSuccessChance(Memories memories) {
        float successChance = getBaseSuccessChance();

        // Increase success based on current hearts value
        successChance += (memories.getHearts() / 10.0D) * 0.025F;

        // Diminish success chance by 5% for each interaction fatigue point
        if (Config.enableDiminishingReturns) {
            successChance -= memories.getInteractionFatigue() * 0.05F;
        }

        return successChance;
    }

    public abstract int getHeartsModifier();
    public abstract float getBaseSuccessChance();
    public abstract String getLangPrefix();
}
