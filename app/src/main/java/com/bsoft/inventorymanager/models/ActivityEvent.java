package com.bsoft.inventorymanager.models;

import com.google.firebase.Timestamp;

public class ActivityEvent implements Comparable<ActivityEvent> {

    public enum EventType {
        SALE,
        PURCHASE,
        DAMAGE,
        RETURN_SALE,
        RETURN_PURCHASE
    }

    private final EventType eventType;
    private final Timestamp eventDate;
    private final Object eventData;

    public ActivityEvent(EventType eventType, Timestamp eventDate, Object eventData) {
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.eventData = eventData;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Timestamp getEventDate() {
        return eventDate;
    }

    public Object getEventData() {
        return eventData;
    }

    @Override
    public int compareTo(ActivityEvent o) {
        return o.getEventDate().compareTo(this.getEventDate()); // Descending order
    }
}
