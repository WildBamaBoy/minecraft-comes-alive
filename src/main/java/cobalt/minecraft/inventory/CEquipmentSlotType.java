package cobalt.minecraft.inventory;

import net.minecraft.inventory.EquipmentSlotType;

import java.util.Arrays;

public enum CEquipmentSlotType {
    UNKNOWN(null),
    MAINHAND(EquipmentSlotType.MAINHAND),
    OFFHAND(EquipmentSlotType.OFFHAND),
    FEET(EquipmentSlotType.FEET),
    LEGS(EquipmentSlotType.LEGS),
    CHEST(EquipmentSlotType.CHEST),
    HEAD(EquipmentSlotType.HEAD);

    private final EquipmentSlotType mcType;
    CEquipmentSlotType(EquipmentSlotType type) {
        this.mcType = type;
    }

    public static CEquipmentSlotType fromMC(EquipmentSlotType type) {
        return Arrays.stream(values()).filter(t -> t.mcType == type).findFirst().orElse(UNKNOWN);
    }

    public EquipmentSlotType getMcType() {
        return mcType;
    }
}
