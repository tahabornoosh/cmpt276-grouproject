package com.cmpt276.group3.grouproject.enums;

public enum CommunicationStyle {
    TEXT_OFTEN("Text often"),
    TEXT_SOMETIMES("Text sometimes"),
    MOSTLY_IN_PERSON("Mostly in person"),
    LOW_MAINTENANCE("Low-maintenance");

    private final String displayName;

    CommunicationStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
