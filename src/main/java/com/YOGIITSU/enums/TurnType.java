package com.YOGIITSU.enums;

import lombok.Getter;

@Getter
public enum TurnType {

    STRAIGHT(11, "직진"),
    LEFT_TURN(12, "좌회전"),
    RIGHT_TURN(13, "우회전");

    private final int code;
    private final String description;

    TurnType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TurnType fromCode(int code) {
        for (TurnType type : values()) {
            if (type.code == code) {
                return type;
            }
        }

        throw new IllegalArgumentException("알 수 없는 TurnType 코드입니다: " + code);
    }
}
