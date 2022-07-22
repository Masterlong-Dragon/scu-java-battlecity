package com.masterlong.framework.essentials;

import java.util.HashMap;

/**
 * 全局事件注册器
 */
public class EventRegister {
    private final HashMap<String, IGameEvent> events;
    private static long eventsCounter = 0;

    public static long getEventID() {
        return eventsCounter++;
    }

    protected EventRegister() {
        events = new HashMap<>();
    }

    public void register(String eventType, IGameEvent event) {
        events.put(eventType, event);
    }

    public void unregister(String eventType) {
        events.remove(eventType);
    }

    public void unregisterAll() {
        events.clear();
    }

    public IGameEvent getEvent(String eventType) {
        return events.get(eventType);
    }
}
