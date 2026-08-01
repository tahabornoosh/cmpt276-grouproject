package com.cmpt276.group3.grouproject.enums;

// What a group is mainly for. This is the heaviest-weighted question in the group
// matching algorithm - people who want different things out of a group rarely gel.
public enum GroupVibe {
    CHILL_HANGOUT("Chill hangouts"),
    STUDY_GRIND("Study grind sessions"),
    PARTY_SOCIAL("Parties and nights out"),
    OUTDOORS_SPORTS("Outdoors and sports"),
    CREATIVE_PROJECTS("Creative projects"),
    FOOD_EXPLORING("Food and exploring"),
    GAMING_NIGHTS("Gaming nights");

    private final String displayName;

    GroupVibe(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
