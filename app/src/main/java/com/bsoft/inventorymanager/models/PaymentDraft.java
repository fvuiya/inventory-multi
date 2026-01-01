package com.bsoft.inventorymanager.models;

import java.io.Serializable;

public class PaymentDraft implements Serializable {
    public double taxPercent;
    public double taxAmount;
    public double discountPercent;
    public double discountAmount;
    public String paymentMethod;
    public double amountPaid;
    public String notes;

    public PaymentDraft() {}

    public PaymentDraft(double taxPercent, double taxAmount, double discountPercent,
                        double discountAmount, String paymentMethod, double amountPaid, String notes) {
        this.taxPercent = taxPercent;
        this.taxAmount = taxAmount;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.paymentMethod = paymentMethod;
        this.amountPaid = amountPaid;
        this.notes = notes;
    }
}
