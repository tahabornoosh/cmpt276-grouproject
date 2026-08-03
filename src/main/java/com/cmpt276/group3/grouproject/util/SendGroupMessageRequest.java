package com.cmpt276.group3.grouproject.util;

public record SendGroupMessageRequest (
    Long groupId,
    String content
) {
    
}
