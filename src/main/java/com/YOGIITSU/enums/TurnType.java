package com.YOGIITSU.enums;

import com.fasterxml.jackson.annotation.JsonValue;
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

    @JsonValue
    public int getCode() {
        return code;
    }
}