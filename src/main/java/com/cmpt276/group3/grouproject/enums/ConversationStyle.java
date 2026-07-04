package com.cmpt276.group3.grouproject.enums;

public enum ConversationStyle {
    DEEP_CONVERSATIONS("Deep conversations"),
    FUNNY_CONVERSATIONS("Funny conversations"),
    SCHOOL_LIFE_CONVERSATIONS("School-life conversations"),
    RANDOM_CASUAL_TALK("Random casual talk");

    private final String displayName;

    ConversationStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
