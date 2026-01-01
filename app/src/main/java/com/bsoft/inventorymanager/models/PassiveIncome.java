package com.bsoft.inventorymanager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class PassiveIncome {

    @DocumentId
    private String documentId;
    private String source;
    private String note;
    private double amount;
    private Timestamp date;

    public PassiveIncome() {
        // Default constructor for Firestore
    }

    public PassiveIncome(String source, String note, double amount, Timestamp date) {
        this.source = source;
        this.note = note;
        this.amount = amount;
        this.date = date;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}
