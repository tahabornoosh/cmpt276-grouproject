package com.cmpt276.group3.grouproject.enums;

public enum FriendshipValue {
    LOYALTY("Loyalty"),
    HONESTY("Honesty"),
    HAVING_FUN("Having fun");

    private final String displayName;

    FriendshipValue(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
