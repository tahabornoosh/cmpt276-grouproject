package com.cmpt276.group3.grouproject.enums;

public enum RelationshipFreeTime {
    RELAXING_HOME("Relaxing at home"),
    GOING_OUT("Going out socially"),
    NEW_ACTIVITIES("Trying new activities"),
    STUDYING_GOALS("Studying or working on goals");

    private final String displayName;

    RelationshipFreeTime(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
