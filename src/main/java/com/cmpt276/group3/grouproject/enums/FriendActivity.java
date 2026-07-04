package com.cmpt276.group3.grouproject.enums;

public enum FriendActivity {
    COFFEE_OR_FOOD("Coffee or food"),
    OUTDOOR_ACTIVITIES("Outdoor activities"),
    GAMING_OR_MOVIES("Gaming or movies");

    private final String displayName;

    FriendActivity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
