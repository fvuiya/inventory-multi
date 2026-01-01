package com.bsoft.inventorymanager.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.bsoft.inventorymanager.fragments.PurchaseReturnFragment;
import com.bsoft.inventorymanager.fragments.SalesReturnFragment;

public class ReturnViewPagerAdapter extends FragmentStateAdapter {

    public ReturnViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new SalesReturnFragment();
        } else {
            return new PurchaseReturnFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs
    }
}
