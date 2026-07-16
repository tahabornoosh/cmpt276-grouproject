package com.cmpt276.group3.grouproject.enums;

public enum RelationshipLifestyle {
    CALM_RELAXED("Calm and relaxed"),
    BUSY_GOAL_FOCUSED("Busy and goal-focused"),
    SOCIAL_OUTGOING("Social and outgoing"),
    BALANCED("Balanced");

    private final String displayName;

    RelationshipLifestyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
