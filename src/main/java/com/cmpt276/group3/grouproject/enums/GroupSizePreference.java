package com.cmpt276.group3.grouproject.enums;

// How big a group the user wants to end up in.
// Also drives how many people the auto-matcher packs into a newly formed group.
public enum GroupSizePreference {
    SMALL("Small (3-4 people)", 3, 4),
    MEDIUM("Medium (5-6 people)", 5, 6),
    LARGE("Large (7-10 people)", 7, 10);

    private final String displayName;
    private final int minSize;
    private final int maxSize;

    GroupSizePreference(String displayName, int minSize, int maxSize) {
        this.displayName = displayName;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinSize() {
        return minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
