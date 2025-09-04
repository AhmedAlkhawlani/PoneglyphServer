//package com.nova.poneglyph.config.websocket;
//
//import org.springframework.stereotype.Service;
//import org.w3c.dom.css.Counter;
//
//import java.util.Map;
//import java.util.Timer;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class WebSocketMetricsService {
//    private final MeterRegistry meterRegistry;
//    private final Map<String, Counter> messageCounters = new ConcurrentHashMap<>();
//    private final Map<String, Timer> messageTimers = new ConcurrentHashMap<>();
//
//    public WebSocketMetricsService(MeterRegistry meterRegistry) {
//        this.meterRegistry = meterRegistry;
//    }
//
//    public void recordMessageSent(String destination, long duration) {
//        String counterName = "websocket.messages.sent." + destination.replace("/", ".");
//        String timerName = "websocket.messages.duration." + destination.replace("/", ".");
//
//        messageCounters.computeIfAbsent(counterName,
//                k -> meterRegistry.counter(k)).increment();
//
//        messageTimers.computeIfAbsent(timerName,
//                k -> meterRegistry.timer(k)).record(duration, TimeUnit.MILLISECONDS);
//    }
//
//    public void recordConnectionEvent(String type, boolean success) {
//        meterRegistry.counter("websocket.connections." + type,
//                "success", String.valueOf(success)).increment();
//    }
//}
