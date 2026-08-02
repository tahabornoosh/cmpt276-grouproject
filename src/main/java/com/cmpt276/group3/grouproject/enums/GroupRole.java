package com.cmpt276.group3.grouproject.enums;

// The two roles a user can hold inside a FriendGroup.
// ADMIN can rename/delete the group and manage members; MEMBER can only take part and leave.
public enum GroupRole {
    MEMBER("Member"),
    ADMIN("Admin");

    private final String displayName;

    GroupRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
