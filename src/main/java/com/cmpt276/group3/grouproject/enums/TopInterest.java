package com.cmpt276.group3.grouproject.enums;

public enum TopInterest {
    SPORTS_FITNESS("Sports/fitness"),
    MUSIC_MOVIES("Music/movies"),
    GAMING_TECH("Gaming/tech"),
    FOOD_TRAVEL("Food/travel");

    private final String displayName;

    TopInterest(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
