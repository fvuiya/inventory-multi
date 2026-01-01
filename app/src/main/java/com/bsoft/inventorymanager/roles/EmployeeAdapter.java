package com.bsoft.inventorymanager.roles;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmployeeAdapter extends ListAdapter<Employee, EmployeeAdapter.EmployeeViewHolder> {

    private final OnEmployeeActionListener actionListener;

    public interface OnEmployeeActionListener {
        void onEdit(Employee employee);
        void onDelete(Employee employee);
    }

    public EmployeeAdapter(OnEmployeeActionListener actionListener) {
        super(DIFF_CALLBACK);
        this.actionListener = actionListener;
    }

    private static final DiffUtil.ItemCallback<Employee> DIFF_CALLBACK = new DiffUtil.ItemCallback<Employee>() {
        @Override
        public boolean areItemsTheSame(@NonNull Employee oldItem, @NonNull Employee newItem) {
            return oldItem.getDocumentId().equals(newItem.getDocumentId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Employee oldItem, @NonNull Employee newItem) {
            return oldItem.equals(newItem) && oldItem.getPermissions().equals(newItem.getPermissions());
        }
    };

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee employee = getItem(position);
        holder.textViewEmployeeName.setText(employee.getName());
        holder.textViewEmployeeDesignation.setText(employee.getDesignation());

        if (employee.getPhoto() != null && !employee.getPhoto().isEmpty()) {
            byte[] decodedString = Base64.decode(employee.getPhoto(), Base64.DEFAULT);
            holder.employeePhoto.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        } else {
            holder.employeePhoto.setImageResource(R.drawable.ic_customer);
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, EmployeeProfileActivity.class);
            intent.putExtra("employee_id", employee.getDocumentId());
            context.startActivity(intent);
        });

        holder.buttonEditEmployee.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(employee);
            }
        });

        holder.buttonDeleteEmployee.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(employee);
            }
        });

        populatePermissions(holder.permissionChipGroup, employee, holder.itemView.getContext());
    }

    private void populatePermissions(ChipGroup chipGroup, Employee employee, Context context) {
        chipGroup.removeAllViews();
        if (employee.getPermissions() == null || employee.getPermissions().isEmpty()) {
            chipGroup.setVisibility(View.GONE);
            return;
        }

        List<String> grantedPermissions = new ArrayList<>();
        for (Map.Entry<String, Permission> entry : employee.getPermissions().entrySet()) {
            if (entry.getValue().isGranted()) {
                grantedPermissions.add(entry.getKey());
            }
        }

        if (grantedPermissions.isEmpty()) {
            chipGroup.setVisibility(View.GONE);
            return;
        }

        chipGroup.setVisibility(View.VISIBLE);
        int limit = Math.min(grantedPermissions.size(), 3);

        for (int i = 0; i < limit; i++) {
            Chip chip = new Chip(context);
            chip.setText(grantedPermissions.get(i).replace("_", " "));
            chipGroup.addView(chip);
        }

        if (grantedPermissions.size() > 3) {
            Chip moreChip = new Chip(context);
            moreChip.setText("+ " + (grantedPermissions.size() - 3) + " more");
            chipGroup.addView(moreChip);
        }
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewEmployeeName, textViewEmployeeDesignation;
        ImageButton buttonEditEmployee, buttonDeleteEmployee;
        ChipGroup permissionChipGroup;
        ImageView employeePhoto;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewEmployeeName = itemView.findViewById(R.id.textViewEmployeeName);
            textViewEmployeeDesignation = itemView.findViewById(R.id.textViewEmployeeDesignation);
            buttonEditEmployee = itemView.findViewById(R.id.buttonEditEmployee);
            buttonDeleteEmployee = itemView.findViewById(R.id.buttonDeleteEmployee);
            permissionChipGroup = itemView.findViewById(R.id.permission_chip_group);
            employeePhoto = itemView.findViewById(R.id.iv_employee_photo);
        }
    }
}
