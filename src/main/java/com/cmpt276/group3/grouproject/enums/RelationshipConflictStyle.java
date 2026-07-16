package com.cmpt276.group3.grouproject.enums;

public enum RelationshipConflictStyle {
    TALK_RIGHT_AWAY("Talk right away"),
    TAKE_TIME_THEN_TALK("Take time, then talk"),
    AVOID_CONFLICT("Avoid conflict"),
    DEPENDS_ON_ISSUE("Depends on the issue");

    private final String displayName;

    RelationshipConflictStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
