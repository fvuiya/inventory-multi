package com.bsoft.inventorymanager.roles;

import android.Manifest;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class AddEditEmployeeSheet extends BottomSheetDialogFragment implements PermissionsAdapter.OnPermissionInteractionListener {

    private static final String ARG_EMPLOYEE = "employee";
    private ManageRolesViewModel viewModel;
    private PermissionsAdapter permissionsAdapter;
    private Employee currentEmployee;

    private ImageView employeeImageView;
    private Uri imageUri;
    private String imageBase64;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    public static AddEditEmployeeSheet newInstance(Employee employee) {
        AddEditEmployeeSheet fragment = new AddEditEmployeeSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EMPLOYEE, employee);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentEmployee = (Employee) getArguments().getSerializable(ARG_EMPLOYEE);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(ManageRolesViewModel.class);
        initializeLaunchers();
    }

    private void initializeLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                launchCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && imageUri != null) {
                processImageUri(imageUri);
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                processImageUri(uri);
            }
        });
    }

    private void processImageUri(Uri uri) {
        this.imageUri = uri;
        try {
            Bitmap bitmap = getBitmapFromUri(uri);
            employeeImageView.setImageBitmap(bitmap);
            Bitmap resizedBitmap = resizeBitmap(bitmap, 100, 100);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();
            this.imageBase64 = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_employee, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        employeeImageView = view.findViewById(R.id.iv_employee_image);
        Button cameraButton = view.findViewById(R.id.btn_camera);
        Button galleryButton = view.findViewById(R.id.btn_gallery);
        final EditText nameEditText = view.findViewById(R.id.editTextEmployeeName);
        final EditText designationEditText = view.findViewById(R.id.editTextEmployeeDesignation);
        final EditText emailEditText = view.findViewById(R.id.editTextEmployeeEmail);
        final EditText passwordEditText = view.findViewById(R.id.editTextEmployeePassword);
        final EditText phoneEditText = view.findViewById(R.id.editTextEmployeePhone);
        final EditText addressEditText = view.findViewById(R.id.editTextEmployeeAddress);
        final EditText ageEditText = view.findViewById(R.id.editTextEmployeeAge);
        final EditText salaryEditText = view.findViewById(R.id.editTextEmployeeSalary);
        final RecyclerView permissionsRecyclerView = view.findViewById(R.id.permissionsRecyclerView);
        final Button saveButton = view.findViewById(R.id.buttonSave);
        final Button cancelButton = view.findViewById(R.id.buttonCancel);

        cameraButton.setOnClickListener(v -> requestPermissionLauncher.launch(Manifest.permission.CAMERA));
        galleryButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        view.post(() -> {
            viewModel.setCurrentPermissions(currentEmployee != null ? currentEmployee.getPermissions() : null);
            permissionsAdapter = new PermissionsAdapter(Permissions.PERMISSION_GROUPS, viewModel.getCurrentPermissions(), this);
            permissionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            permissionsRecyclerView.setHasFixedSize(true);
            permissionsRecyclerView.setAdapter(permissionsAdapter);
        });

        if (currentEmployee != null) {
            nameEditText.setText(currentEmployee.getName());
            designationEditText.setText(currentEmployee.getDesignation());
            emailEditText.setText(currentEmployee.getEmail());
            phoneEditText.setText(currentEmployee.getPhone());
            addressEditText.setText(currentEmployee.getAddress());
            ageEditText.setText(String.valueOf(currentEmployee.getAge()));
            salaryEditText.setText(String.valueOf(currentEmployee.getSalary()));
            emailEditText.setEnabled(false);
            passwordEditText.setVisibility(View.GONE);
            saveButton.setText("Save");

            if (currentEmployee.getPhoto() != null && !currentEmployee.getPhoto().isEmpty()) {
                byte[] decodedString = Base64.decode(currentEmployee.getPhoto(), Base64.DEFAULT);
                Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                employeeImageView.setImageBitmap(decodedByte);
                this.imageBase64 = currentEmployee.getPhoto();
            }

        } else {
            saveButton.setText("Add");
        }

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String designation = designationEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            int age = 0;
            double salary = 0;
            try {
                age = Integer.parseInt(ageEditText.getText().toString());
                salary = Double.parseDouble(salaryEditText.getText().toString());
            } catch (NumberFormatException e) { 
                // ignore 
            }
            viewModel.saveEmployee(name, email, password, imageBase64, salary, phone, address, age, designation, currentEmployee);
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onPermissionToggled(int position, String permissionName, boolean isChecked) {
        viewModel.onPermissionToggled(permissionName, isChecked);
        permissionsAdapter.notifyItemChanged(position);
    }

    @Override
    public void onExpandToggled(int position) {
        permissionsAdapter.setExpandedPosition(position);
    }

    @Override
    public void onDurationChipClicked(int position, String permissionName, int chipId) {
        Timestamp expiry = getTimestampForChip(chipId);
        viewModel.setPermissionExpiry(permissionName, expiry);
        permissionsAdapter.notifyItemChanged(position);
    }

    @Override
    public void onTillChipClicked(int position, String permissionName) {
        viewModel.onTillChipClicked(permissionName, permission -> showDatePicker(position, permissionName));
    }

    private void launchCamera() {
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Employee Photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Inventory Manager App");
        imageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        takePictureLauncher.launch(imageUri);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().getContentResolver(), uri));
        } else {
            return MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxWidth;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxHeight;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void showDatePicker(int position, String permissionName) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth, 23, 59, 59);
            Timestamp expiry = new Timestamp(cal.getTime());
            viewModel.setPermissionExpiry(permissionName, expiry);
            permissionsAdapter.notifyItemChanged(position);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private Timestamp getTimestampForChip(int checkedId) {
        Calendar cal = Calendar.getInstance();
        if (checkedId == R.id.chip_1h) cal.add(Calendar.HOUR_OF_DAY, 1);
        else if (checkedId == R.id.chip_4h) cal.add(Calendar.HOUR_OF_DAY, 4);
        else if (checkedId == R.id.chip_8h) cal.add(Calendar.HOUR_OF_DAY, 8);
        else if (checkedId == R.id.chip_12h) cal.add(Calendar.HOUR_OF_DAY, 12);
        else if (checkedId == R.id.chip_1d) cal.add(Calendar.DAY_OF_YEAR, 1);
        else return null;
        return new Timestamp(cal.getTime());
    }
}
