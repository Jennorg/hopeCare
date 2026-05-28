package com.esperanza.hopecare.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {
    private static EventBus instance;
    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public <T> void register(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public <T> void unregister(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> list = listeners.get(eventType);
        if (list != null) list.remove(listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void post(T event) {
        List<EventListener<?>> list = listeners.get(event.getClass());
        if (list != null) {
            for (EventListener<?> listener : list) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }

    public interface EventListener<T> {
        void onEvent(T event);
    }
}
