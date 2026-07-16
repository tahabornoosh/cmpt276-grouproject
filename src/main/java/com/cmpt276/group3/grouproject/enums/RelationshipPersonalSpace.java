package com.cmpt276.group3.grouproject.enums;

public enum RelationshipPersonalSpace {
    A_LOT("A lot"),
    SOME("Some"),
    VERY_LITTLE("Very little"),
    DEPENDS_ON_SCHEDULE("Depends on my schedule");

    private final String displayName;

    RelationshipPersonalSpace(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
