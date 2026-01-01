package com.bsoft.inventorymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.activities.CustomerActivity;
import com.bsoft.inventorymanager.activities.ExpensesActivity;
import com.bsoft.inventorymanager.activities.OfferActivity;
import com.bsoft.inventorymanager.activities.OrderActivity;
import com.bsoft.inventorymanager.activities.PassiveIncomeActivity;
import com.bsoft.inventorymanager.activities.ProductActivity;
import com.bsoft.inventorymanager.activities.PurchaseActivity;
import com.bsoft.inventorymanager.activities.SupplierActivity;
import com.bsoft.inventorymanager.activities.ReturnActivity;
import com.bsoft.inventorymanager.activities.SalesActivity;
import com.bsoft.inventorymanager.reports.ui.ReportsActivity;
import com.bsoft.inventorymanager.roles.ManageRolesActivity;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout itemOne = view.findViewById(R.id.itemOne);
        LinearLayout manageCustomersLayout = view.findViewById(R.id.manageCustomersLayout);
        LinearLayout manageOrdersLayout = view.findViewById(R.id.manageOrdersLayout);
        LinearLayout manageSalesLayout = view.findViewById(R.id.manageSalesLayout);
        LinearLayout itemFive = view.findViewById(R.id.itemFive);
        LinearLayout manageSuppliersLayout = view.findViewById(R.id.manageSuppliersLayout);
        LinearLayout manageMarketingLayout = view.findViewById(R.id.manageMarketingLayout);
        LinearLayout manageReportsLayout = view.findViewById(R.id.manageReportsLayout);
        LinearLayout manageRolesLayout = view.findViewById(R.id.manage_roles_layout);
        LinearLayout manageExpensesLayout = view.findViewById(R.id.manageExpensesLayout);
        LinearLayout itemEleven = view.findViewById(R.id.itemEleven);
        LinearLayout itemTwelve = view.findViewById(R.id.itemTwelve);

        itemOne.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ProductActivity.class)));
        manageCustomersLayout
                .setOnClickListener(v -> startActivity(new Intent(v.getContext(), CustomerActivity.class)));
        manageOrdersLayout.setOnClickListener(v -> startActivity(new Intent(v.getContext(), OrderActivity.class)));
        manageSalesLayout.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SalesActivity.class)));
        itemFive.setOnClickListener(v -> startActivity(new Intent(v.getContext(), PurchaseActivity.class)));
        manageSuppliersLayout
                .setOnClickListener(v -> startActivity(new Intent(v.getContext(), SupplierActivity.class)));
        manageMarketingLayout.setOnClickListener(v -> startActivity(new Intent(v.getContext(), OfferActivity.class)));
        manageReportsLayout.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ReportsActivity.class)));
        manageRolesLayout.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ManageRolesActivity.class)));
        manageExpensesLayout.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ExpensesActivity.class)));
        itemEleven.setOnClickListener(v -> startActivity(new Intent(v.getContext(), PassiveIncomeActivity.class)));
        itemTwelve.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ReturnActivity.class)));
    }
}
