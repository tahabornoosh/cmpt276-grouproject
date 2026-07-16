package com.cmpt276.group3.grouproject.enums;

public enum RelationshipTextingStyle {
    THROUGHOUT_DAY("Throughout the day"),
    ONCE_OR_TWICE_DAY("Once or twice a day"),
    FEW_TIMES_WEEK("A few times a week"),
    PREFER_IN_PERSON("I prefer in-person more");

    private final String displayName;

    RelationshipTextingStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
