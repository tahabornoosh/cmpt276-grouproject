package com.cmpt276.group3.grouproject.enums;

public enum FriendshipStyle {
    CLOSE_FRIEND("Close friend"),
    CASUAL_FRIEND("Casual friend"),
    ACTIVITY_BUDDY("Activity buddy"),
    FRIEND_GROUP("Friend group");

    private final String displayName;

    FriendshipStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
