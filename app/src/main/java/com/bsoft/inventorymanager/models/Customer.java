package com.bsoft.inventorymanager.models;

import com.bsoft.inventorymanager.model.Person;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Customer implements Serializable, Person {

    @DocumentId
    private String documentId;
    private String name;
    private String contactNumber;
    private String address;
    private int age;
    private String photo;
    private boolean isActive = true;
    private long creationDate;
    private double creditLimit;
    private double outstandingBalance;
    private String customerType; // retail, wholesale, VIP
    private String customerTier; // gold, silver, bronze
    private long lastPurchaseDate;
    private double totalPurchaseAmount;
    private int purchaseFrequency; // number of purchases
    private String paymentTerms; // net 30, net 60, etc.
    private double discountRate; // applicable discount rate
    private double rating; // Customer rating
    private int leadTime; // Lead time in days for customer orders
    private double performanceScore; // Customer performance rating
    private boolean preferredCustomer; // Is this a preferred customer
    private String contractDetails; // Contract terms
    private String productsPurchased; // Categories of products purchased
    private String bankAccount; // Customer bank details for payments
    private String taxId; // Tax identification number

    public Customer() {
        // Default constructor for Firestore
    }

    public Customer(String name, String address, int age, String contactNumber) {
        this.name = name;
        this.address = address;
        this.age = age;
        this.contactNumber = contactNumber;
        this.isActive = true; // Default to active
        this.creationDate = Timestamp.now().getSeconds();
    }

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

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getCustomerTier() {
        return customerTier;
    }

    public void setCustomerTier(String customerTier) {
        this.customerTier = customerTier;
    }

    public Timestamp getLastPurchaseDate() {
        return new Timestamp(lastPurchaseDate, 0);
    }

    public void setLastPurchaseDate(Timestamp lastPurchaseDate) {
        if (lastPurchaseDate != null) {
            this.lastPurchaseDate = lastPurchaseDate.getSeconds();
        } else {
            this.lastPurchaseDate = 0;
        }
    }

    public double getTotalPurchaseAmount() {
        return totalPurchaseAmount;
    }

    public void setTotalPurchaseAmount(double totalPurchaseAmount) {
        this.totalPurchaseAmount = totalPurchaseAmount;
    }

    public int getPurchaseFrequency() {
        return purchaseFrequency;
    }

    public void setPurchaseFrequency(int purchaseFrequency) {
        this.purchaseFrequency = purchaseFrequency;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
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

    public boolean isPreferredCustomer() {
        return preferredCustomer;
    }

    public void setPreferredCustomer(boolean preferredCustomer) {
        this.preferredCustomer = preferredCustomer;
    }

    public String getContractDetails() {
        return contractDetails;
    }

    public void setContractDetails(String contractDetails) {
        this.contractDetails = contractDetails;
    }

    public String getProductsPurchased() {
        return productsPurchased;
    }

    public void setProductsPurchased(String productsPurchased) {
        this.productsPurchased = productsPurchased;
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
