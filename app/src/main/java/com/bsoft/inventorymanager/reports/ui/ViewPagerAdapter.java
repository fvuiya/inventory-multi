package com.bsoft.inventorymanager.reports.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bsoft.inventorymanager.reports.ui.fragments.CustomerFragment;
import com.bsoft.inventorymanager.reports.ui.fragments.FinancialFragment;
import com.bsoft.inventorymanager.reports.ui.fragments.ProductsFragment;
import com.bsoft.inventorymanager.reports.ui.fragments.PurchaseFragment;
import com.bsoft.inventorymanager.reports.ui.fragments.SalesFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FinancialFragment();
            case 1:
                return new SalesFragment();
            case 2:
                return new ProductsFragment();
            case 3:
                return new CustomerFragment();
            case 4:
                return new PurchaseFragment();
            default:
                return new FinancialFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}