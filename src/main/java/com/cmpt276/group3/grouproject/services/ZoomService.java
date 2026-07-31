package com.cmpt276.group3.grouproject.services;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.cmpt276.group3.grouproject.config.ZoomConfig;
import com.cmpt276.group3.grouproject.util.zoom.ZoomApiMeetingResponse;
import com.cmpt276.group3.grouproject.util.zoom.ZoomMeetingResponse;
import com.cmpt276.group3.grouproject.util.zoom.ZoomTokenResponse;

@Service
public class ZoomService {
    
    private static final String ZOOM_TOKEN_URL =
        "https://zoom.us/oauth/token";

    private static final String ZOOM_API_URL =
        "https://api.zoom.us/v2";

    private final ZoomConfig zoomConfig;
    private final RestClient restClient;

    public ZoomService(
        ZoomConfig zoomConfig,
        RestClient.Builder restClientBuilder
    ) {
        this.zoomConfig = zoomConfig;
        this.restClient = restClientBuilder.build();
    }

    public ZoomMeetingResponse createMeeting() {
        validateConfiguation();

        String accessToken = requestAccessToken();

        Map<String, Object> meetingSettings = new LinkedHashMap<>();

        meetingSettings.put("host_video", true);
        meetingSettings.put("participant_video", true);
        meetingSettings.put("join_before_host", false);
        meetingSettings.put("waiting_room", true);
        meetingSettings.put("mute_upon_entry", false);

        Map<String, Object> meetingRequest = new LinkedHashMap<>();

        meetingRequest.put("topic", "Private Video Call");
        meetingRequest.put("type", 1);
        meetingRequest.put("default_password", true);
        meetingRequest.put("settings", meetingSettings);

        ZoomApiMeetingResponse zoomMeeting =
            restClient
                .post()
                .uri(
                    ZOOM_API_URL + "/users/{userId}/meetings",
                    zoomConfig.getUserId()
                )
                .headers(headers ->
                    headers.setBearerAuth(accessToken)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(meetingRequest)
                .retrieve()
                .body(ZoomApiMeetingResponse.class);

        if (zoomMeeting == null || !StringUtils.hasText(zoomMeeting.getJoinUrl()) || !StringUtils.hasText(zoomMeeting.getStartUrl())) {
            throw new IllegalStateException(
                "Zoom returned incomplete meeting information."
            );
        }

        return new ZoomMeetingResponse(
            zoomMeeting.getJoinUrl(),
            zoomMeeting.getStartUrl()
        );
    }

    private String requestAccessToken() {
        MultiValueMap<String, String> tokenRequest =
            new LinkedMultiValueMap<>();

        tokenRequest.add(
            "grant_type",
            "account_credentials"
        );

        tokenRequest.add(
            "account_id",
            zoomConfig.getAccountId()
        );

        ZoomTokenResponse tokenResponse =
            restClient
                .post()
                .uri(ZOOM_TOKEN_URL)
                .headers(headers -> 
                    headers.setBasicAuth(
                        zoomConfig.getClientId(),
                        zoomConfig.getClientSecret()
                    )
                )
                .contentType(
                    MediaType.APPLICATION_FORM_URLENCODED
                )
                .accept(MediaType.APPLICATION_JSON)
                .body(tokenRequest)
                .retrieve()
                .body(ZoomTokenResponse.class);

        if (tokenResponse == null || !StringUtils.hasText(tokenResponse.getAccessToken())) {
            throw new IllegalStateException(
                "Zoom did not return an access token."
            );
        }
        return tokenResponse.getAccessToken();
    }
    private void validateConfiguation() {
        if (!StringUtils.hasText(zoomConfig.getAccountId())) {
            throw new IllegalStateException(
                "The zoom account id is not configured."
            );
        }

        if (!StringUtils.hasText(zoomConfig.getClientId())) {
            throw new IllegalStateException(
                "The zoom client id is not configured"
            );
        }

        if (!StringUtils.hasText(zoomConfig.getClientSecret())) {
            throw new IllegalStateException(
                "The zoom client secret is not configured."
            );
        }

        if (!StringUtils.hasText(zoomConfig.getUserId())) {
            throw new IllegalStateException(
                "The zoom host user ID or email is not configured."
            );
        }
    }
}
