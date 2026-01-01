package com.bsoft.inventorymanager.activities;

import android.os.Bundle;
import android.widget.Toast;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Supplier;

public class AddEditSupplierSheet extends BaseAddEditSheet<Supplier> {

    public static AddEditSupplierSheet newInstance(Supplier supplier) {
        AddEditSupplierSheet fragment = new AddEditSupplierSheet();
        Bundle args = new Bundle();
        args.putSerializable("ARG_ITEM", supplier);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_add_supplier;
    }

    @Override
    protected Supplier createNewItem() {
        // Use parameterized constructor to initialize creationDate
        return new Supplier("", "", 0, "");
    }

    @Override
    protected void onSave(Supplier supplier) {
        if (supplier.getDocumentId() != null) {
            db.collection("suppliers").document(supplier.getDocumentId()).set(supplier)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Supplier updated successfully!", Toast.LENGTH_SHORT).show();
                        handleSuccess(supplier);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error updating supplier: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    });
        } else {
            db.collection("suppliers").add(supplier)
                    .addOnSuccessListener(documentReference -> {
                        supplier.setDocumentId(documentReference.getId());
                        Toast.makeText(getContext(), "Supplier added successfully!", Toast.LENGTH_SHORT).show();
                        handleSuccess(supplier);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error adding supplier: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    });
        }
    }

    private void handleSuccess(Supplier supplier) {
        boolean returnDirectly = getArguments() != null && getArguments().getBoolean("RETURN_DIRECTLY", false);
        if (returnDirectly && getActivity() instanceof CreatePurchaseActivity) {
            ((CreatePurchaseActivity) getActivity()).updateSupplierSelectionUI(supplier);
            dismiss();
        } else {
            dismiss();
        }
    }
}
