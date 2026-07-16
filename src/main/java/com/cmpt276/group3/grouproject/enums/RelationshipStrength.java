package com.cmpt276.group3.grouproject.enums;

public enum RelationshipStrength {
    HONEST_COMMUNICATION("Honest communication"),
    EMOTIONAL_SUPPORT("Emotional support"),
    LOYALTY("Loyalty"),
    FUN_ENERGY("Fun energy");

    private final String displayName;

    RelationshipStrength(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
