package com.minecraftcomesalive.mca.interaction.base;

public class TellStoryInteraction extends AbstractBaseInteraction {
    @Override
    public int getHeartsModifier() {
        return 9;
    }

    @Override
    public float getBaseSuccessChance() {
        return 65;
    }

    @Override
    public String getLangPrefix() {
        return "tellstory";
    }
}
