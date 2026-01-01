package com.bsoft.inventorymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Customer;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectCustomerAdapter extends RecyclerView.Adapter<MultiSelectCustomerAdapter.MultiSelectCustomerViewHolder> {

    private List<Customer> customerList;
    private final List<Customer> selectedCustomers;

    public MultiSelectCustomerAdapter(List<Customer> customerList) {
        this.customerList = customerList;
        this.selectedCustomers = new ArrayList<>();
    }

    @NonNull
    @Override
    public MultiSelectCustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multiselect_customer, parent, false);
        return new MultiSelectCustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiSelectCustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.textViewCustomerName.setText(customer.getName());

        holder.checkBoxCustomer.setOnCheckedChangeListener(null);
        holder.checkBoxCustomer.setChecked(selectedCustomers.contains(customer));

        holder.checkBoxCustomer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedCustomers.contains(customer)) {
                    selectedCustomers.add(customer);
                }
            } else {
                selectedCustomers.remove(customer);
            }
        });

        holder.itemView.setOnClickListener(v -> holder.checkBoxCustomer.toggle());
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public List<Customer> getSelectedCustomers() {
        return selectedCustomers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customerList = customers;
        notifyDataSetChanged();
    }

    public static class MultiSelectCustomerViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBoxCustomer;
        TextView textViewCustomerName;

        public MultiSelectCustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxCustomer = itemView.findViewById(R.id.checkbox_customer);
            textViewCustomerName = itemView.findViewById(R.id.textview_customer_name);
        }
    }
}
