package com.minecraftcomesalive.mca.interaction.base;

public class JokeInteraction extends AbstractBaseInteraction {
    @Override
    public int getHeartsModifier() {
        return 8;
    }

    @Override
    public float getBaseSuccessChance() {
        return 70;
    }

    @Override
    public String getLangPrefix() {
        return "joke";
    }
}
