package com.bsoft.inventorymanager.roles;

import com.google.firebase.Timestamp;

public class CurrentUser {
    private static CurrentUser instance;
    private Employee employee;

    private CurrentUser() {}

    public static CurrentUser getInstance() {
        if (instance == null) {
            instance = new CurrentUser();
        }
        return instance;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void clear() {
        employee = null;
    }

    public boolean hasPermission(String permission) {
        if (employee == null || employee.getPermissions() == null) {
            return false;
        }

        Permission p = employee.getPermissions().get(permission);
        if (p == null || !p.isGranted()) {
            return false;
        }

        return p.getExpires() == null || p.getExpires().compareTo(Timestamp.now()) >= 0; // Permission has expired
    }
}
