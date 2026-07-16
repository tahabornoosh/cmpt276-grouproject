package com.cmpt276.group3.grouproject.enums;

public enum RelationshipValue {
    TRUST("Trust"),
    LOYALTY("Loyalty"),
    COMMUNICATION("Communication"),
    INDEPENDENCE("Independence");

    private final String displayName;

    RelationshipValue(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
