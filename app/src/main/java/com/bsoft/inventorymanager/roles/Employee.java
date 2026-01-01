package com.bsoft.inventorymanager.roles;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Map;

public class Employee implements Serializable {
    @Exclude
    private String documentId;
    private String name;
    private String email;
    private String designation;
    private String fcmToken; // FCM Token for notifications
    private String photo; // Base64 encoded image
    private double salary;
    private String address;
    private int age;
    private String phone;
    private Map<String, Permission> permissions;
    private boolean isActive = true;

    public Employee() {
        // Default constructor required for Firestore
    }

    public Employee(String name, String email, Map<String, Permission> permissions) {
        this.name = name;
        this.email = email;
        this.permissions = permissions;
    }

    @Exclude
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Map<String, Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Permission> permissions) {
        this.permissions = permissions;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }
}
