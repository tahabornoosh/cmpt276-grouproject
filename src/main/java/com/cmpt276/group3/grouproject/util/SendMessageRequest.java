package com.cmpt276.group3.grouproject.util;

public record SendMessageRequest(
    Long recipientId,
    String content
) {

}
