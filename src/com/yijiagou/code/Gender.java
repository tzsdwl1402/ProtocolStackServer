package com.yijiagou.code;

public enum Gender {
    UNKNOWN(0), MALE(1), FEMAIL(2);

    private final int value;

    private Gender(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Gender fromValue(int value) {
        for (Gender g : Gender.values()) {
            if (g.getValue() == value)
                return g;
        }
        throw new IllegalArgumentException(String.valueOf(value));
    }
}
