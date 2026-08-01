package com.cmpt276.group3.grouproject.util.zoom;

public class ZoomMeetingResponse {
    
    private String joinUrl;
    private String hostUrl;

    public ZoomMeetingResponse() {

    }

    public ZoomMeetingResponse(String joinUrl, String hostUrl) {
        this.joinUrl = joinUrl;
        this.hostUrl = hostUrl;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }
}
