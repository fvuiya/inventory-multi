package com.bsoft.inventorymanager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Supplier implements Serializable, Person {

    @DocumentId
    private String documentId;
    private String name;
    private String contactNumber;
    private String address;
    private int age;
    private String photo;
    private boolean isActive = true;
    private double rating; // Supplier rating
    private String paymentTerms; // Net 30, Net 60, etc.
    private int leadTime; // Lead time in days
    private double performanceScore; // Performance rating
    private boolean preferredSupplier; // Is this a preferred supplier
    private double outstandingPayment; // Amount owed to supplier
    private String contractDetails; // Contract terms
    private String productsSupplied; // Categories of products supplied
    private long lastDeliveryDate;
    private String bankAccount; // Supplier bank details for payments
    private String taxId; // Tax identification number

    private long creationDate;
    private double totalSupplyAmount;
    private int supplyFrequency;
    private String supplierType; // e.g., Distributor, Wholesaler
    private String supplierTier; // e.g., Gold, Silver

    public Supplier() {
        // Default constructor for Firestore
    }

    public Supplier(String name, String address, int age, String contactNumber) {
        this.name = name;
        this.address = address;
        this.age = age;
        this.contactNumber = contactNumber;
        this.isActive = true;
        this.creationDate = Timestamp.now().getSeconds();
    }

    public Timestamp getCreationDate() {
        return new Timestamp(creationDate, 0);
    }

    public void setCreationDate(Timestamp creationDate) {
        if (creationDate != null) {
            this.creationDate = creationDate.getSeconds();
        } else {
            this.creationDate = 0;
        }
    }

    public double getTotalSupplyAmount() {
        return totalSupplyAmount;
    }

    public void setTotalSupplyAmount(double totalSupplyAmount) {
        this.totalSupplyAmount = totalSupplyAmount;
    }

    public int getSupplyFrequency() {
        return supplyFrequency;
    }

    public void setSupplyFrequency(int supplyFrequency) {
        this.supplyFrequency = supplyFrequency;
    }

    public String getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(String supplierType) {
        this.supplierType = supplierType;
    }

    public String getSupplierTier() {
        return supplierTier;
    }

    public void setSupplierTier(String supplierTier) {
        this.supplierTier = supplierTier;
    }

    // Getters and Setters for existing fields
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        this.isActive = active;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public int getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(int leadTime) {
        this.leadTime = leadTime;
    }

    public double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(double performanceScore) {
        this.performanceScore = performanceScore;
    }

    public boolean isPreferredSupplier() {
        return preferredSupplier;
    }

    public void setPreferredSupplier(boolean preferredSupplier) {
        this.preferredSupplier = preferredSupplier;
    }

    public double getOutstandingPayment() {
        return outstandingPayment;
    }

    public void setOutstandingPayment(double outstandingPayment) {
        this.outstandingPayment = outstandingPayment;
    }

    public String getContractDetails() {
        return contractDetails;
    }

    public void setContractDetails(String contractDetails) {
        this.contractDetails = contractDetails;
    }

    public String getProductsSupplied() {
        return productsSupplied;
    }

    public void setProductsSupplied(String productsSupplied) {
        this.productsSupplied = productsSupplied;
    }

    public Timestamp getLastDeliveryDate() {
        return new Timestamp(lastDeliveryDate, 0);
    }

    public void setLastDeliveryDate(Timestamp lastDeliveryDate) {
        if (lastDeliveryDate != null) {
            this.lastDeliveryDate = lastDeliveryDate.getSeconds();
        } else {
            this.lastDeliveryDate = 0;
        }
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }
}
