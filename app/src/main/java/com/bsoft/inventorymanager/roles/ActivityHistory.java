package com.bsoft.inventorymanager.roles;

import com.google.firebase.Timestamp;

public class ActivityHistory {
    private String eventDescription;
    private Timestamp eventTimestamp;
    private String performedBy;

    public ActivityHistory() {}

    public ActivityHistory(String eventDescription, Timestamp eventTimestamp, String performedBy) {
        this.eventDescription = eventDescription;
        this.eventTimestamp = eventTimestamp;
        this.performedBy = performedBy;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Timestamp getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Timestamp eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
}
