package com.minecraftcomesalive.mca.interaction.base;

public class ChatInteraction extends AbstractBaseInteraction {
    @Override
    public int getHeartsModifier() {
        return 5;
    }

    @Override
    public float getBaseSuccessChance() {
        return 80;
    }

    @Override
    public String getLangPrefix() {
        return "chat";
    }
}
