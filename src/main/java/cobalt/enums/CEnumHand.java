package cobalt.enums;

import lombok.Getter;
import net.minecraft.util.Hand;

import java.util.Arrays;

public enum CEnumHand {
    UNKNOWN(null),
    MAIN_HAND(Hand.MAIN_HAND),
    OFF_HAND(Hand.OFF_HAND);

    CEnumHand(Hand hand) {
        this.mcHand = hand;
    }

    @Getter private Hand mcHand;

    public static CEnumHand fromMC(Hand hand) {
        return Arrays.stream(values()).filter(h -> h.mcHand == hand).findFirst().orElse(UNKNOWN);
    }
}