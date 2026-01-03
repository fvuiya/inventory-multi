package com.bsoft.inventorymanager.activities;

import android.os.Bundle;
import android.widget.Toast;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.model.Supplier;

public class AddEditSupplierSheet extends BaseAddEditSheet<Supplier> {

    public static AddEditSupplierSheet newInstance(Supplier supplier) {
        AddEditSupplierSheet fragment = new AddEditSupplierSheet();
        Bundle args = new Bundle();
        if (supplier != null) {
            String json = com.bsoft.inventorymanager.utils.SupplierSerializationHelper.serialize(supplier);
            args.putString("ARG_ITEM_JSON", json);
        }
        fragment.setArguments(args);
        return fragment;
    }

    private com.bsoft.inventorymanager.viewmodels.SupplierViewModel viewModel;

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("ARG_ITEM_JSON")) {
            String json = getArguments().getString("ARG_ITEM_JSON");
            if (json != null) {
                this.currentItem = com.bsoft.inventorymanager.utils.SupplierSerializationHelper.deserialize(json);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_add_supplier;
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull android.view.View view,
            @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity())
                .get(com.bsoft.inventorymanager.viewmodels.SupplierViewModel.class);

        viewModel.getLastSavedSupplierId().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                // Determine if this is the supplier we just saved.
                // Since we reset value to null on save start, any non-null value here means
                // success.
                if (currentItem.getDocumentId().isEmpty()) {
                    currentItem.setDocumentId(id);
                }
                Toast.makeText(getContext(), "Supplier saved successfully!", Toast.LENGTH_SHORT).show();
                handleSuccess(currentItem);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected Supplier createNewItem() {
        return new Supplier();
    }

    @Override
    protected void onSave(Supplier supplier) {
        viewModel.saveSupplier(supplier);
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
