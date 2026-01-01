package com.bsoft.inventorymanager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class Purchase implements Serializable {
    private static final long serialVersionUID = 2L;

    @DocumentId
    private String documentId;

    private String SupplierId;
    private String SupplierName;
    private String SupplierContactNumber;

    private Timestamp purchaseDate;
    private String purchaseOrderNumber; // Purchase order reference
    private Timestamp expectedDeliveryDate; // Expected delivery date
    private Timestamp actualDeliveryDate; // Actual delivery date
    private String deliveryStatus; // pending, delivered, partial, delayed
    private String purchaseStatus; // pending, approved, received, cancelled
    private double totalAmount;
    private double amountPaid;
    private double amountDue;
    private double taxAmount; // Tax on purchase
    private double discountAmount; // Discount on purchase
    private String paymentStatus; // pending, partial, paid, overdue
    private String paymentMethod; // cash, credit, bank transfer
    private String notes; // Additional notes
    private List<PurchaseItem> items;
    private List<String> productIds; // NEW
    private String userId;
    private String status;
    private String deliveryAddress; // Delivery address
    private String invoiceNumber; // Supplier invoice number

    public Purchase() {
        // Default constructor for Firestore
    }

    public Purchase(String documentId, String SupplierId, String SupplierName, String SupplierContactNumber,
                    Timestamp purchaseDate, double totalAmount, double amountPaid, double amountDue, List<PurchaseItem> items,
                    String userId, String status) {
        this.documentId = documentId;
        this.SupplierId = SupplierId;
        this.SupplierName = SupplierName;
        this.SupplierContactNumber = SupplierContactNumber;
        this.purchaseDate = purchaseDate;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.amountDue = amountDue;
        this.items = items;
        this.productIds = items.stream().map(PurchaseItem::getProductId).collect(Collectors.toList()); // NEW
        this.userId = userId;
        this.status = status;
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSupplierId() {
        return SupplierId;
    }

    public void setSupplierId(String SupplierId) {
        this.SupplierId = SupplierId;
    }

    public String getSupplierName() {
        return SupplierName;
    }

    public void setSupplierName(String SupplierName) {
        this.SupplierName = SupplierName;
    }

    public String getSupplierContactNumber() {
        return SupplierContactNumber;
    }

    public void setSupplierContactNumber(String SupplierContactNumber) {
        this.SupplierContactNumber = SupplierContactNumber;
    }

    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public Timestamp getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(Timestamp expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public Timestamp getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(Timestamp actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getPurchaseStatus() {
        return purchaseStatus;
    }

    public void setPurchaseStatus(String purchaseStatus) {
        this.purchaseStatus = purchaseStatus;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItem> items) {
        this.items = items;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
}
