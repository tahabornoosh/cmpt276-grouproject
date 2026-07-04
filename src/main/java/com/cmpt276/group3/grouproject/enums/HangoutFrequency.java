package com.cmpt276.group3.grouproject.enums;

public enum HangoutFrequency {
    SEVERAL_TIMES_A_WEEK("Several times a week"),
    ONCE_A_WEEK("Once a week"),
    A_FEW_TIMES_A_MONTH("A few times a month"),
    OCCASIONALLY("Occasionally");

    private final String displayName;

    HangoutFrequency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
