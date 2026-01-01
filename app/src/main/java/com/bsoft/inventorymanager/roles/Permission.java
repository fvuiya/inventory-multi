package com.bsoft.inventorymanager.roles;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Permission implements Serializable {
    private boolean granted;
    private Timestamp expires;

    public Permission() {
        // Default constructor for Firestore
    }

    public Permission(boolean granted, Timestamp expires) {
        this.granted = granted;
        this.expires = expires;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public Timestamp getExpires() {
        return expires;
    }

    public void setExpires(Timestamp expires) {
        this.expires = expires;
    }
}
