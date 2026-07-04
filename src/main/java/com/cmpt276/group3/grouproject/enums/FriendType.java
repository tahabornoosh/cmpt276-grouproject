package com.cmpt276.group3.grouproject.enums;

public enum FriendType {
    SUPPORTIVE("Supportive"),
    FUNNY("Funny"),
    HONEST("Honest"),
    GOOD_LISTENER("Good listener");

    private final String displayName;

    FriendType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
