package com.cmpt276.group3.grouproject.enums;

public enum PlanningStyle {
    PLANNED_AHEAD("Planned ahead"),
    LAST_MINUTE("Last minute"),
    BOTH("Both"),
    LET_OTHERS_PLAN("I usually let others plan");

    private final String displayName;

    PlanningStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
