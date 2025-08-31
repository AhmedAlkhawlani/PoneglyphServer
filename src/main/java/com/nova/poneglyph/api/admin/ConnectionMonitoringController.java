package com.nova.poneglyph.api.admin;

import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.service.SessionService;
import com.nova.poneglyph.service.presence.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/connections")
@RequiredArgsConstructor
public class ConnectionMonitoringController {

    private final PresenceService presenceService;
    private final SessionService sessionService;

    @GetMapping
    public Map<String, Object> getActiveConnections() {
        List<UserSession> activeSessions = sessionService.getAllActiveSessions();

        Map<String, Object> result = new HashMap<>();
        result.put("totalSessions", activeSessions.size());

        List<Map<String, Object>> sessionsData = new ArrayList<>();
        for (UserSession session : activeSessions) {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", session.getUser().getId());
            sessionData.put("sessionId", session.getId());
            sessionData.put("online", session.isOnline());
            sessionData.put("lastActivity", session.getLastActivity());
            sessionData.put("redisActive",
                    presenceService.isUserOnline(session.getUser().getId()));
            sessionData.put("websocketSessionId", session.getWebsocketSessionId());

            sessionsData.add(sessionData);
        }

        result.put("sessions", sessionsData);
        return result;
    }

    @GetMapping("/health")
    public Map<String, Object> getSystemHealth() {
        List<UserSession> activeSessions = sessionService.getAllActiveSessions();

        long activeInRedis = activeSessions.stream()
                .filter(session -> presenceService.isUserOnline(session.getUser().getId()))
                .count();

        long activeInDb = activeSessions.stream()
                .filter(UserSession::isOnline)
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("totalSessions", activeSessions.size());
        result.put("activeInRedis", activeInRedis);
        result.put("activeInDb", activeInDb);
        result.put("syncStatus", activeInRedis == activeInDb ? "SYNCED" : "OUT_OF_SYNC");

        return result;
    }
}
