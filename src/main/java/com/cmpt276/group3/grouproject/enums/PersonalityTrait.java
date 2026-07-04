package com.cmpt276.group3.grouproject.enums;

public enum PersonalityTrait {
    FUNNY("Funny"),
    CALM("Calm"),
    AMBITIOUS("Ambitious"),
    ADVENTUROUS("Adventurous");

    private final String displayName;

    PersonalityTrait(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
