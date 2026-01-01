package com.bsoft.inventorymanager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Sale implements Serializable {
    private static final long serialVersionUID = 1L;

    @DocumentId
    private String documentId;

    private String customerId;
    private String customerName;
    private String customerPhoneNumber;
    private String salesPerson; // Sales representative
    private double commissionRate; // Commission rate for salesperson

    private Timestamp saleDate;
    private String invoiceNumber; // Invoice number
    private double totalAmount;
    private double amountPaid;
    private double amountDue;
    private double taxAmount; // Tax on sale
    private double discountAmount; // Discount on sale
    private String paymentMethod; // cash, credit, bank transfer, card
    private String deliveryStatus; // pending, delivered, in-transit, delivered
    private String deliveryAddress; // Delivery address
    private double deliveryCharge; // Delivery charges
    private String loyaltyPointsEarned; // Loyalty points earned by customer
    private String salesChannel; // online, in-store, wholesale
    private String notes; // Additional notes
    private List<SaleItem> items;
    private List<String> productIds; // NEW FIELD
    private String userId;
    private String status; // pending, confirmed, delivered, cancelled
    private double subtotal;
    private Double totalCost; // Pre-calculated total cost of all items
    private Double totalProfit; // totalAmount - totalCost

    public Sale() {
        // Default constructor for Firestore
    }

    public Sale(String documentId, String customerId, String customerName, String customerPhoneNumber,
            String salesPerson,
            Timestamp saleDate, double totalAmount, double amountPaid, List<SaleItem> items,
            String userId, String status) {
        this.documentId = documentId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhoneNumber = customerPhoneNumber;
        this.salesPerson = salesPerson;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.amountDue = totalAmount - amountPaid;
        this.items = items;
        this.productIds = items.stream().map(SaleItem::getProductId).collect(Collectors.toList()); // Populate
                                                                                                   // productIds
        this.userId = userId;
        this.status = status;
    }

    public Sale(String documentId, String customerId, String customerName, String customerPhoneNumber,
            String salesPerson,
            Timestamp saleDate, double totalAmount, double amountPaid, double amountDue, List<SaleItem> items,
            String userId, String status) {
        this.documentId = documentId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhoneNumber = customerPhoneNumber;
        this.salesPerson = salesPerson;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.amountDue = amountDue;
        this.items = items;
        this.productIds = items.stream().map(SaleItem::getProductId).collect(Collectors.toList()); // Populate
                                                                                                   // productIds
        this.userId = userId;
        this.status = status;
    }

    // Getters and Setters
    @DocumentId
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    @PropertyName("customerPhoneNumber")
    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public String getSalesPerson() {
        return salesPerson;
    }

    public void setSalesPerson(String salesPerson) {
        this.salesPerson = salesPerson;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public Timestamp getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Timestamp saleDate) {
        this.saleDate = saleDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @PropertyName("totalBill")
    public double getTotalBill() {
        return totalAmount;
    }

    @PropertyName("totalBill")
    public void setTotalBill(double totalBill) {
        this.totalAmount = totalBill;
        this.amountDue = this.totalAmount - this.amountPaid;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        this.amountDue = this.totalAmount - this.amountPaid;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
        this.amountDue = this.totalAmount - amountPaid;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Double totalProfit) {
        this.totalProfit = totalProfit;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public double getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(double deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public String getLoyaltyPointsEarned() {
        return loyaltyPointsEarned;
    }

    public void setLoyaltyPointsEarned(String loyaltyPointsEarned) {
        this.loyaltyPointsEarned = loyaltyPointsEarned;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("customerId", customerId);
        map.put("customerName", customerName);
        map.put("customerPhoneNumber", customerPhoneNumber);
        map.put("salesPerson", salesPerson);
        map.put("commissionRate", commissionRate);
        map.put("saleDate", saleDate);
        map.put("invoiceNumber", invoiceNumber);
        map.put("totalAmount", totalAmount);
        map.put("amountPaid", amountPaid);
        map.put("amountDue", amountDue);
        map.put("taxAmount", taxAmount);
        map.put("discountAmount", discountAmount);
        map.put("paymentMethod", paymentMethod);
        map.put("deliveryStatus", deliveryStatus);
        map.put("deliveryAddress", deliveryAddress);
        map.put("deliveryCharge", deliveryCharge);
        map.put("loyaltyPointsEarned", loyaltyPointsEarned);
        map.put("salesChannel", salesChannel);
        map.put("notes", notes);
        if (items != null) {
            map.put("items", items.stream().map(SaleItem::toMap).collect(Collectors.toList()));
            // Also update productIds when converting to map
            map.put("productIds", items.stream().map(SaleItem::getProductId).collect(Collectors.toList()));
        } else {
            map.put("items", null);
            map.put("productIds", new ArrayList<>());
        }
        map.put("userId", userId);
        map.put("status", status);
        map.put("totalCost", totalCost);
        map.put("totalProfit", totalProfit);
        return map;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "documentId='" + documentId + '\'' +
                // ... (rest of the fields) ...
                '}';
    }
}
