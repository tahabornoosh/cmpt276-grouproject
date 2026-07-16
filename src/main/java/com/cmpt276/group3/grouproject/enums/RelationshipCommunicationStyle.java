package com.cmpt276.group3.grouproject.enums;

public enum RelationshipCommunicationStyle {
    DIRECT_HONEST("Direct and honest"),
    PLAYFUL_JOKING("Playful and joking"),
    QUIET_AT_FIRST("Quiet at first"),
    DEEP_THOUGHTFUL("Deep and thoughtful");

    private final String displayName;

    RelationshipCommunicationStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
