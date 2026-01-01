package com.bsoft.inventorymanager.activities;

import android.Manifest;
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
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Person;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public abstract class BaseAddEditSheet<T extends Person & Serializable> extends BottomSheetDialogFragment {

    protected FirebaseFirestore db;
    protected T currentItem;

    protected ImageView itemImageView;
    protected Uri imageUri;
    protected String imageBase64;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    protected abstract int getLayoutId();
    protected abstract void onSave(T item);
    protected abstract T createNewItem();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentItem = (T) getArguments().getSerializable("ARG_ITEM");
        }
        db = FirebaseFirestore.getInstance();
        initializeLaunchers();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemImageView = view.findViewById(R.id.iv_person_image);
        Button cameraButton = view.findViewById(R.id.btn_camera);
        Button galleryButton = view.findViewById(R.id.btn_gallery);
        final EditText nameEditText = view.findViewById(R.id.editTextPersonName);
        final EditText phoneEditText = view.findViewById(R.id.editTextPersonContact);
        final EditText addressEditText = view.findViewById(R.id.editTextPersonAddress);
        final EditText ageEditText = view.findViewById(R.id.editTextPersonAge);
        final Button saveButton = view.findViewById(R.id.buttonSave);
        final Button cancelButton = view.findViewById(R.id.buttonCancel);

        cameraButton.setOnClickListener(v -> requestPermissionLauncher.launch(Manifest.permission.CAMERA));
        galleryButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        if (currentItem != null) {
            nameEditText.setText(currentItem.getName());
            phoneEditText.setText(currentItem.getContactNumber());
            addressEditText.setText(currentItem.getAddress());
            ageEditText.setText(String.valueOf(currentItem.getAge()));
            saveButton.setText("Save");

            if (currentItem.getPhoto() != null && !currentItem.getPhoto().isEmpty()) {
                byte[] decodedString = Base64.decode(currentItem.getPhoto(), Base64.DEFAULT);
                Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                itemImageView.setImageBitmap(decodedByte);
                this.imageBase64 = currentItem.getPhoto();
            }
        } else {
            saveButton.setText("Add");
        }

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String ageStr = ageEditText.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            int age = 0;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid age.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentItem == null) {
                currentItem = createNewItem();
            }

            currentItem.setName(name);
            currentItem.setContactNumber(phone);
            currentItem.setAddress(address);
            currentItem.setAge(age);
            currentItem.setPhoto(imageBase64);
            currentItem.setActive(true);

            onSave(currentItem);
        });

        cancelButton.setOnClickListener(v -> dismiss());
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
            itemImageView.setImageBitmap(bitmap);
            Bitmap resizedBitmap = resizeBitmap(bitmap, 100, 100);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();
            this.imageBase64 = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchCamera() {
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Photo");
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
}
