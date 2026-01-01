package com.bsoft.inventorymanager.roles;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Permissions {

    public static final Map<String, List<String>> PERMISSION_GROUPS = new LinkedHashMap<>();

    static {
        PERMISSION_GROUPS.put("Sales", Arrays.asList(
                "can_create_sales",
                "can_edit_sales",
                "can_delete_sales"
        ));
        PERMISSION_GROUPS.put("Purchases", Arrays.asList(
                "can_create_purchases",
                "can_edit_purchases",
                "can_delete_purchases"
        ));
        PERMISSION_GROUPS.put("Product Details", Arrays.asList(
                "can_create_products",
                "can_edit_products",
                "can_delete_products"
        ));
        PERMISSION_GROUPS.put("Product Stock", Arrays.asList(
                "can_add_stock",
                "can_edit_stock",
                "can_remove_stock"
        ));
        PERMISSION_GROUPS.put("Customer Management", Arrays.asList(
                "can_create_customers",
                "can_edit_customers",
                "can_delete_customers"
        ));
        PERMISSION_GROUPS.put("Employee Management", List.of(
                "can_manage_employees"
        ));
        PERMISSION_GROUPS.put("Reporting", List.of(
                "can_view_reports"
        ));
    }
}
