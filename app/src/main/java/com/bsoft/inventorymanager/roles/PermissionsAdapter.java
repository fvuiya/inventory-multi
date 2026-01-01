package com.bsoft.inventorymanager.roles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PermissionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PERMISSION = 1;

    private final Map<String, List<String>> permissionGroups;
    private final Map<String, Permission> employeePermissions;
    private final OnPermissionInteractionListener listener;

    private final List<Object> displayItems = new ArrayList<>();
    private final Set<String> expandedGroups = new HashSet<>();
    private int expandedPosition = -1;

    public interface OnPermissionInteractionListener {
        void onPermissionToggled(int position, String permissionName, boolean isChecked);
        void onExpandToggled(int position);
        void onDurationChipClicked(int position, String permissionName, int chipId);
        void onTillChipClicked(int position, String permissionName);
    }

    public PermissionsAdapter(Map<String, List<String>> permissionGroups, Map<String, Permission> employeePermissions, OnPermissionInteractionListener listener) {
        this.permissionGroups = permissionGroups;
        this.employeePermissions = employeePermissions;
        this.listener = listener;
        buildDisplayList();
    }

    private void buildDisplayList() {
        displayItems.clear();
        for (String groupTitle : permissionGroups.keySet()) {
            displayItems.add(groupTitle);
            if (expandedGroups.contains(groupTitle)) {
                displayItems.addAll(permissionGroups.get(groupTitle));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position) instanceof String && permissionGroups.containsKey(displayItems.get(position)) ? TYPE_HEADER : TYPE_PERMISSION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_permission_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_permission, parent, false);
            return new PermissionViewHolder(view, this::getPermissionName, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String groupTitle = (String) displayItems.get(position);
            headerHolder.headerTitle.setText(groupTitle);
            headerHolder.expandIcon.setImageResource(expandedGroups.contains(groupTitle) ? R.drawable.arrow_up : R.drawable.arrow_down);
            holder.itemView.setOnClickListener(v -> {
                if (expandedGroups.contains(groupTitle)) {
                    expandedGroups.remove(groupTitle);
                } else {
                    expandedGroups.add(groupTitle);
                }
                buildDisplayList();
                notifyDataSetChanged();
            });
        } else {
            PermissionViewHolder pvh = (PermissionViewHolder) holder;
            String permissionName = (String) displayItems.get(position);
            pvh.permissionName.setText(permissionName.replace("_", " "));

            Permission permission = employeePermissions.get(permissionName);
            if (permission == null) {
                permission = new Permission(false, null);
            }

            pvh.permissionSwitch.setChecked(permission.isGranted());

            final boolean isExpanded = position == expandedPosition;
            pvh.durationChipGroup.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            if (isExpanded) {
                pvh.expandIcon.setImageResource(R.drawable.arrow_up);
            } else if (permission.getExpires() != null) {
                pvh.expandIcon.setImageResource(R.drawable.ic_clock);
            } else {
                pvh.expandIcon.setImageResource(R.drawable.arrow_down);
            }

            pvh.chipTill.setText("Select Date");
            updateChipSelection(pvh, permission);
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    private String getPermissionName(int position) {
        if (position >= 0 && position < displayItems.size()) {
            Object item = displayItems.get(position);
            if (item instanceof String && !permissionGroups.containsKey(item)) {
                return (String) item;
            }
        }
        return null;
    }

    public void setExpandedPosition(int position) {
        int previousExpandedPosition = expandedPosition;
        expandedPosition = position == previousExpandedPosition ? -1 : position;
        if (previousExpandedPosition != -1) {
            notifyItemChanged(previousExpandedPosition);
        }
        if (expandedPosition != -1) {
            notifyItemChanged(expandedPosition);
        }
    }

    public void updatePermissions() {
        buildDisplayList();
        notifyDataSetChanged();
    }

    private void updateChipSelection(PermissionViewHolder holder, Permission permission) {
        ChipGroup chipGroup = holder.durationChipGroup;
        Timestamp expires = permission.getExpires();

        chipGroup.clearCheck();

        if (expires != null) {
            // Calculate the difference with a small buffer to handle timing precision issues
            long diff = expires.getSeconds() - (Timestamp.now().getSeconds() + 1); // Add 1 second buffer
            long hours = TimeUnit.SECONDS.toHours(diff);

            if (diff < 0) { // Already expired
                chipGroup.check(R.id.chip_till);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                holder.chipTill.setText("EXPIRED: " + sdf.format(expires.toDate()));
            } else if (hours >= 0 && hours < 2) chipGroup.check(R.id.chip_1h);
            else if (hours >= 2 && hours < 5) chipGroup.check(R.id.chip_4h);
            else if (hours >= 5 && hours < 9) chipGroup.check(R.id.chip_8h);
            else if (hours >= 9 && hours < 13) chipGroup.check(R.id.chip_12h);
            else if (hours >= 13 && hours < 25) chipGroup.check(R.id.chip_1d);
            else {
                chipGroup.check(R.id.chip_till);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                holder.chipTill.setText("Till: " + sdf.format(expires.toDate()));
            }
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        ImageView expandIcon;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.header_title);
            expandIcon = itemView.findViewById(R.id.expand_icon);
        }
    }

    public static class PermissionViewHolder extends RecyclerView.ViewHolder {
        TextView permissionName;
        ImageView expandIcon;
        SwitchMaterial permissionSwitch;
        ChipGroup durationChipGroup;
        Chip chip1h, chip4h, chip8h, chip12h, chip1d, chipTill;

        public PermissionViewHolder(@NonNull View itemView, java.util.function.Function<Integer, String> permissionNameProvider, OnPermissionInteractionListener listener) {
            super(itemView);
            permissionName = itemView.findViewById(R.id.permission_name);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            permissionSwitch = itemView.findViewById(R.id.permission_switch);
            durationChipGroup = itemView.findViewById(R.id.duration_chip_group);
            chip1h = itemView.findViewById(R.id.chip_1h);
            chip4h = itemView.findViewById(R.id.chip_4h);
            chip8h = itemView.findViewById(R.id.chip_8h);
            chip12h = itemView.findViewById(R.id.chip_12h);
            chip1d = itemView.findViewById(R.id.chip_1d);
            chipTill = itemView.findViewById(R.id.chip_till);

            permissionSwitch.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    String permission = permissionNameProvider.apply(position);
                    if (permission != null) {
                        listener.onPermissionToggled(position, permission, permissionSwitch.isChecked());
                    }
                }
            });

            expandIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onExpandToggled(position);
                }
            });

            View.OnClickListener chipClickListener = v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    String permission = permissionNameProvider.apply(position);
                    if (permission != null) {
                        if (v.getId() == R.id.chip_till) {
                            listener.onTillChipClicked(position, permission);
                        } else {
                            listener.onDurationChipClicked(position, permission, v.getId());
                        }
                    }
                }
            };

            chip1h.setOnClickListener(chipClickListener);
            chip4h.setOnClickListener(chipClickListener);
            chip8h.setOnClickListener(chipClickListener);
            chip12h.setOnClickListener(chipClickListener);
            chip1d.setOnClickListener(chipClickListener);
            chipTill.setOnClickListener(chipClickListener);
        }
    }
}
