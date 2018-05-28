package com.yijiagou.code;

public enum Find {
    UNKNOWN(0), EXIST(1), NOT_EXIST(2);

    private final int value;

    private Find(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Find fromValue(int value) {
        for (Find f : Find.values()) {
            if (f.getValue() == value)
                return f;
        }
        throw new IllegalArgumentException(String.valueOf(value));
    }
}
