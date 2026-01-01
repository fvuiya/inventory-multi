package com.bsoft.inventorymanager.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class Notification implements Serializable {
    @DocumentId
    private String documentId;
    private String title;
    private String body;
    private String type; // SALE, PURCHASE, RETURN_SALE, RETURN_PURCHASE, DAMAGE
    private double amount;
    @ServerTimestamp
    private Date timestamp;
    private Map<String, String> metadata;
    private boolean isRead;

    public Notification() {
        // Required for Firestore
    }

    public Notification(String title, String body, String type, double amount, Map<String, String> metadata) {
        this.title = title;
        this.body = body;
        this.type = type;
        this.amount = amount;
        this.metadata = metadata;
        this.isRead = false;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
