package com.bsoft.inventorymanager.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class Message {
    @Exclude
    private String documentId;
    private String offerId;
    private String customerId;
    private String messageContent;
    private Date sentDate;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String offerId, String customerId, String messageContent) {
        this.offerId = offerId;
        this.customerId = customerId;
        this.messageContent = messageContent;
        this.sentDate = new Date();
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }
}
