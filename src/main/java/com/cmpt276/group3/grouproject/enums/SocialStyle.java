package com.cmpt276.group3.grouproject.enums;

public enum SocialStyle {
    INTROVERTED("Introverted"),
    EXTROVERTED("Extroverted"),
    AMBIVERT("Ambivert"),
    DEPENDS_ON_SITUATION("Depends on the situation");

    private final String displayName;

    SocialStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
