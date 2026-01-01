package com.bsoft.inventorymanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Customer;

import java.io.Serializable;

public class AddEditCustomerSheet extends BaseAddEditSheet<Customer> {

    public static AddEditCustomerSheet newInstance(Customer customer) {
        AddEditCustomerSheet fragment = new AddEditCustomerSheet();
        Bundle args = new Bundle();
        args.putSerializable("ARG_ITEM", customer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_add_customer;
    }

    @Override
    protected Customer createNewItem() {
        return new Customer();
    }

    @Override
    protected void onSave(Customer customer) {
        if (customer.getDocumentId() != null) {
            db.collection("customers").document(customer.getDocumentId()).set(customer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Customer updated successfully!", Toast.LENGTH_SHORT).show();
                    handleSuccess(customer);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating customer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            db.collection("customers").add(customer)
                .addOnSuccessListener(documentReference -> {
                    customer.setDocumentId(documentReference.getId());
                    Toast.makeText(getContext(), "Customer added successfully!", Toast.LENGTH_SHORT).show();
                    handleSuccess(customer);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error adding customer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void handleSuccess(Customer customer) {
        boolean returnDirectly = getArguments() != null && getArguments().getBoolean("RETURN_DIRECTLY", false);
        if (returnDirectly && getActivity() instanceof CreateSaleActivity) {
            ((CreateSaleActivity) getActivity()).updateCustomerSelectionUI(customer);
            dismiss();
        } else if (getActivity() != null && getActivity().getIntent() != null && getActivity().getIntent().getBooleanExtra("RETURN_ON_ADD", false)) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("NEW_CUSTOMER", customer);
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();
        } else {
            dismiss();
        }
    }
}
