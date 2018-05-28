package com.yijiagou.code;

public enum Friend {
    UNKNOWN("0"), NEWFRIEND("1"), OLDFRIEND("2");

    private final String value;

    private Friend(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Friend fromValue(int value) {
        for (Friend f : Friend.values()) {
            if (f.getValue().equals(value))
                return f;
        }
        throw new IllegalArgumentException(String.valueOf(value));
    }
}
