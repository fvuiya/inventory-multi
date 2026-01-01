package com.bsoft.inventorymanager.roles;

import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.ActivityEventAdapter;
import com.bsoft.inventorymanager.viewmodels.ActivityFeedViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmployeeProfileActivity extends AppCompatActivity {

    private EmployeeProfileViewModel profileViewModel;
    private ActivityFeedViewModel activityViewModel;
    private ActivityEventAdapter eventAdapter;
    private boolean isPermissionsExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_profile);

        profileViewModel = new ViewModelProvider(this).get(EmployeeProfileViewModel.class);
        activityViewModel = new ViewModelProvider(this).get(ActivityFeedViewModel.class);

        RecyclerView historyRecyclerView = findViewById(R.id.rv_activity_history);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new ActivityEventAdapter();
        historyRecyclerView.setAdapter(eventAdapter);

        LinearLayout permissionsHeader = findViewById(R.id.permissions_header);
        ImageView permissionsExpandIcon = findViewById(R.id.permissions_expand_icon);
        LinearLayout permissionsContainer = findViewById(R.id.permissions_container);

        permissionsHeader.setOnClickListener(v -> {
            isPermissionsExpanded = !isPermissionsExpanded;
            permissionsContainer.setVisibility(isPermissionsExpanded ? View.VISIBLE : View.GONE);
            permissionsExpandIcon.setRotation(isPermissionsExpanded ? 180 : 0);
        });

        String employeeId = getIntent().getStringExtra("employee_id");

        profileViewModel.getEmployee().observe(this, this::updateUi);
        activityViewModel.getActivityEvents().observe(this, eventAdapter::submitList);

        if (employeeId != null) {
            profileViewModel.loadEmployee(employeeId);
            activityViewModel.loadEmployeeActivity(employeeId);
        }
    }

    private void updateUi(Employee employee) {
        if (employee == null) return;

        ImageView photo = findViewById(R.id.iv_employee_photo);
        TextView name = findViewById(R.id.tv_employee_name);
        TextView designation = findViewById(R.id.tv_employee_designation);
        TextView email = findViewById(R.id.tv_employee_email);
        TextView phone = findViewById(R.id.tv_employee_phone);
        TextView address = findViewById(R.id.tv_employee_address);
        TextView age = findViewById(R.id.tv_employee_age);
        TextView salary = findViewById(R.id.tv_employee_salary);

        name.setText(employee.getName());
        designation.setText(employee.getDesignation());
        email.setText(employee.getEmail());
        phone.setText("Phone: " + employee.getPhone());
        address.setText("Address: " + employee.getAddress());
        age.setText("Age: " + employee.getAge());
        salary.setText(String.format("Salary: %.2f", employee.getSalary()));

        if (employee.getPhoto() != null && !employee.getPhoto().isEmpty()) {
            byte[] decodedString = Base64.decode(employee.getPhoto(), Base64.DEFAULT);
            photo.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        } else {
            photo.setImageResource(R.drawable.ic_customer);
        }

        populateCategorizedPermissions(employee);
    }

    private void populateCategorizedPermissions(Employee employee) {
        LinearLayout container = findViewById(R.id.permissions_container);
        container.removeAllViews();
        if (employee.getPermissions() == null || employee.getPermissions().isEmpty()) {
            return;
        }

        for (Map.Entry<String, List<String>> groupEntry : Permissions.PERMISSION_GROUPS.entrySet()) {
            String groupTitle = groupEntry.getKey();
            List<String> permissionsInGroup = groupEntry.getValue();
            
            List<String> grantedPermissionsForGroup = new ArrayList<>();
            for (String permissionName : permissionsInGroup) {
                Permission p = employee.getPermissions().get(permissionName);
                if (p != null && p.isGranted()) {
                    grantedPermissionsForGroup.add(permissionName);
                }
            }

            if (!grantedPermissionsForGroup.isEmpty()) {
                TextView header = new TextView(this);
                header.setText(groupTitle);
                header.setTypeface(header.getTypeface(), Typeface.BOLD);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 24, 0, 8);
                header.setLayoutParams(params);
                container.addView(header);

                ChipGroup chipGroup = new ChipGroup(this);
                for (String permissionName : grantedPermissionsForGroup) {
                    Chip chip = new Chip(this);
                    chip.setText(permissionName.replace("_", " "));
                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.TRANSPARENT));
                    chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#D91E1E")));
                    chip.setChipStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
                    chipGroup.addView(chip);
                }
                container.addView(chipGroup);
            }
        }
    }
}
