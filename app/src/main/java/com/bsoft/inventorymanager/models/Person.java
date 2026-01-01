package com.bsoft.inventorymanager.models;

public interface Person {
    String getDocumentId();
    void setDocumentId(String documentId);
    String getName();
    void setName(String name);
    String getContactNumber();
    void setContactNumber(String contactNumber);
    String getAddress();
    void setAddress(String address);
    int getAge();
    void setAge(int age);
    String getPhoto();
    void setPhoto(String photo);
    boolean isActive();
    void setActive(boolean active);
}
