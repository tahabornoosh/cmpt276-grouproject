package com.cmpt276.group3.grouproject.util.zoom;

public class CreateZoomMeetingRequest {
    private Long recipientId;

    public CreateZoomMeetingRequest() {

    }

    public CreateZoomMeetingRequest(Long recipientId) {
        this.recipientId = recipientId;
    }
    
    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
}
