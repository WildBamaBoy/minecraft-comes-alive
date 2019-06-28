package mca.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum EnumReaperAttackState {
    IDLE(0),
    PRE(1),
    POST(2),
    REST(3),
    BLOCK(4);

    int id;

    public static EnumReaperAttackState fromId(int id) {
        return Arrays.stream(values()).filter(s -> s.id == id).findFirst().orElse(IDLE);
    }
}
