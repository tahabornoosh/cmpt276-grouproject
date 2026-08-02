package com.cmpt276.group3.grouproject.enums;

// Where the user actually wants to meet up with their group.
public enum MeetupStyle {
    ON_CAMPUS("On campus"),
    OFF_CAMPUS("Off campus"),
    ONLINE("Online only"),
    MIXED("A mix of everything");

    private final String displayName;

    MeetupStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
