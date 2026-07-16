package com.cmpt276.group3.grouproject.enums;

public enum RelationshipSocialLife {
    MOSTLY_QUIET("Mostly quiet"),
    SMALL_GROUP("Small friend group"),
    VERY_SOCIAL("Very social"),
    CHANGES_OFTEN("Changes often");

    private final String displayName;

    RelationshipSocialLife(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
