package com.yijiagou.code;

public enum BackStatus {
    UNKNOWN(0), SUCCESS(1), FAILED(2);

    private final int value;

    private BackStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BackStatus fromValue(int value) {
        for (BackStatus bs : BackStatus.values()) {
            if (bs.getValue() == value)
                return bs;
        }
        throw new IllegalArgumentException(String.valueOf(value));
    }
}
