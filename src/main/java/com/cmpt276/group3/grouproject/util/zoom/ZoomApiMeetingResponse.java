package com.cmpt276.group3.grouproject.util.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoomApiMeetingResponse {
    
    private Long id;

    @JsonProperty("join_url")
    private String joinUrl;

    @JsonProperty("start_url")
    private String startUrl;

    public ZoomApiMeetingResponse() {

    }

    public ZoomApiMeetingResponse(
        Long id,
        String joinUrl,
        String startUrl
    ) {
        this.id = id;
        this.joinUrl = joinUrl;
        this.startUrl = startUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }
    
}
