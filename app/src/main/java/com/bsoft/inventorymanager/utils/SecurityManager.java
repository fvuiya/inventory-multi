package com.bsoft.inventorymanager.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.bsoft.inventorymanager.roles.CurrentUser;
import com.bsoft.inventorymanager.roles.Employee;
import com.google.firebase.Timestamp;

public class SecurityManager {
    
    private static final String TAG = "SecurityManager";
    
    // Canonical permission constants (aligned with Permissions.PERMISSION_GROUPS)
    public static final String PERMISSION_CAN_CREATE_SALES = "can_create_sales";
    public static final String PERMISSION_CAN_EDIT_SALES = "can_edit_sales";
    public static final String PERMISSION_CAN_DELETE_SALES = "can_delete_sales";

    public static final String PERMISSION_CAN_CREATE_PURCHASES = "can_create_purchases";
    public static final String PERMISSION_CAN_EDIT_PURCHASES = "can_edit_purchases";
    public static final String PERMISSION_CAN_DELETE_PURCHASES = "can_delete_purchases";

    public static final String PERMISSION_CAN_CREATE_PRODUCTS = "can_create_products";
    public static final String PERMISSION_CAN_EDIT_PRODUCTS = "can_edit_products";
    public static final String PERMISSION_CAN_DELETE_PRODUCTS = "can_delete_products";

    public static final String PERMISSION_CAN_CREATE_CUSTOMERS = "can_create_customers";
    public static final String PERMISSION_CAN_EDIT_CUSTOMERS = "can_edit_customers";
    public static final String PERMISSION_CAN_DELETE_CUSTOMERS = "can_delete_customers";

    public static final String PERMISSION_CAN_MANAGE_EMPLOYEES = "can_manage_employees";
    public static final String PERMISSION_CAN_VIEW_REPORTS = "can_view_reports";

    /**
     * Checks if the current user has a specific permission
     * @param permission The permission to check
     * @return true if user has permission, false otherwise
     */
    public static boolean hasPermission(String permission) {
        CurrentUser currentUser = CurrentUser.getInstance();
        Employee employee = currentUser.getEmployee();
        
        if (employee == null || employee.getPermissions() == null) {
            Log.w(TAG, "No current user or permissions available");
            return false;
        }
        
        // Check if the specific permission exists and is granted
        com.bsoft.inventorymanager.roles.Permission userPermission = 
            employee.getPermissions().get(permission);
        
        if (userPermission == null || !userPermission.isGranted()) {
            return false;
        }
        
        // Check if permission has expired
        Timestamp expires = userPermission.getExpires();
        if (expires != null) {
            Timestamp now = Timestamp.now();
            // Add a small buffer to handle potential timing precision issues
            // A permission that expires within 1 second is considered expired
            Timestamp adjustedNow = new Timestamp(now.getSeconds() + 1, now.getNanoseconds());
            if (expires.compareTo(adjustedNow) < 0) { // expired if expires is before adjusted now
                Log.w(TAG, "Permission " + permission + " has expired");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates access to a specific operation and shows toast if denied
     * @param context The context for showing toast
     * @param permission The required permission
     * @param operationName The name of the operation for error message
     * @return true if access is granted, false otherwise
     */
    public static boolean validateAccess(Context context, String permission, String operationName) {
        if (!hasPermission(permission)) {
            String errorMsg = "Access denied: You don't have permission to " + operationName;
            Log.w(TAG, errorMsg);
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    
    /**
     * Checks if the current user can perform financial operations
     * @return true if user can perform financial operations, false otherwise
     */
    public static boolean canPerformFinancialOperations() {
        return hasAnyPermission(PERMISSION_CAN_VIEW_REPORTS, PERMISSION_CAN_CREATE_SALES, PERMISSION_CAN_CREATE_PURCHASES);
    }
    
    /**
     * Checks if the current user can create purchases
     * @return true if user can create purchases, false otherwise
     */
    public static boolean canCreatePurchases() {
        return hasPermission(PERMISSION_CAN_CREATE_PURCHASES);
    }
    
    /**
     * Checks if the current user can edit purchases
     * @return true if user can edit purchases, false otherwise
     */
    public static boolean canEditPurchases() {
        return hasPermission(PERMISSION_CAN_EDIT_PURCHASES);
    }
    
    /**
     * Checks if the current user can delete purchases
     * @return true if user can delete purchases, false otherwise
     */
    public static boolean canDeletePurchases() {
        return hasPermission(PERMISSION_CAN_DELETE_PURCHASES);
    }

    /**
     * Checks if the current user can create sales
     * @return true if user can create sales, false otherwise
     */
    public static boolean canCreateSales() {
        return hasPermission(PERMISSION_CAN_CREATE_SALES);
    }

    /**
     * Checks if the current user can edit sales
     * @return true if user can edit sales, false otherwise
     */
    public static boolean canEditSales() {
        return hasPermission(PERMISSION_CAN_EDIT_SALES);
    }

    /**
     * Checks if the current user can delete sales
     * @return true if user can delete sales, false otherwise
     */
    public static boolean canDeleteSales() {
        return hasPermission(PERMISSION_CAN_DELETE_SALES);
    }

    /**
     * Checks if the current user is an admin (has manage roles permission)
     * @return true if user is admin, false otherwise
     */
    public static boolean isAdmin() {
        // Align admin to the single management permission in the system
        return hasPermission(PERMISSION_CAN_MANAGE_EMPLOYEES);
    }

    /**
     * Checks if the current user is a manager (has employee management permission)
     * @return true if user is manager, false otherwise
     */
    public static boolean isManager() {
        return hasPermission(PERMISSION_CAN_MANAGE_EMPLOYEES);
    }

    /**
     * Checks if the current user has any of the specified permissions
     * @param permissions The permissions to check
     * @return true if user has any of the permissions, false otherwise
     */
    public static boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }
}