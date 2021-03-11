package com.minecraftcomesalive.mca.interaction.base;

public class ShakeHandInteraction extends AbstractBaseInteraction {
    @Override
    public int getHeartsModifier() {
        return 3;
    }

    @Override
    public float getBaseSuccessChance() {
        return 95;
    }

    @Override
    public String getLangPrefix() {
        return "shakehand";
    }
}
