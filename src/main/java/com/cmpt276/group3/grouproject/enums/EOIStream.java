package com.cmpt276.group3.grouproject.enums;

public enum EOIStream {
    FRIENDSHIP("Friendship"),
    RELATIONSHIP("Relationship"),
    STUDY_BUDDY("Study Buddy");

    private final String displayName;

    EOIStream(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
