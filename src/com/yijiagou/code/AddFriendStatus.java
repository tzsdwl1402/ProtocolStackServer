package com.yijiagou.code;

public enum AddFriendStatus {
    UNKNOWN("0"), INVITE("1"), AGREE("2");

    private final String value;

    private AddFriendStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AddFriendStatus fromValue(int value) {
        for (AddFriendStatus afs : AddFriendStatus.values()) {
            if (afs.getValue().equals(value))
                return afs;
        }
        throw new IllegalArgumentException(String.valueOf(value));
    }
}
