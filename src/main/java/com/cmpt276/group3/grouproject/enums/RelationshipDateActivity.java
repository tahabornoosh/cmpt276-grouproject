package com.cmpt276.group3.grouproject.enums;

public enum RelationshipDateActivity {
    COFFEE_FOOD("Coffee or food"),
    WALKS_OUTDOORS("Walks or outdoor activities"),
    MOVIES_GAMES("Movies or games"),
    EVENTS_PARTIES("Events or parties");

    private final String displayName;

    RelationshipDateActivity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
