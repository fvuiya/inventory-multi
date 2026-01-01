package com.bsoft.inventorymanager.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.ProductAdapter;
import com.bsoft.inventorymanager.models.ApiProduct;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.ProductResponse;
import com.bsoft.inventorymanager.network.ProductApiService;
import com.bsoft.inventorymanager.network.RetrofitClient;
import com.bsoft.inventorymanager.utils.ErrorHandler;
import com.bsoft.inventorymanager.utils.FinancialCalculator;
import com.bsoft.inventorymanager.utils.InputValidator;
import com.bsoft.inventorymanager.utils.SecurityManager;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProductActivity extends AppCompatActivity {

    private static final String TAG = "ProductActivity";
    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private final List<String> brandList = new ArrayList<>();
    private final List<String> categoryList = new ArrayList<>();
    private FirebaseFirestore db;
    private CollectionReference productsCollection;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri imageUri;
    private String apiImageUrl = null;
    private ImageView dialogProductImage;
    private GmsBarcodeScanner scanner;
    private AlertDialog loadingDialog;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private com.bsoft.inventorymanager.viewmodels.MainViewModel mainViewModel;

    private TextInputEditText etProductName;
    private AutoCompleteTextView actvBrand;
    private AutoCompleteTextView actvCategory;
    private TextInputEditText etProductCode;

    private Product productFromScan = null; // Holds product if found via barcode scan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(ProductActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_product);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_product);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();
        productsCollection = db.collection("products");

        RecyclerView productsRecyclerView = findViewById(R.id.productsRecyclerView);
        FloatingActionButton addProductFab = findViewById(R.id.addProductFab);

        adapter = new ProductAdapter(productList, new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(int position, Product product) {
                showEditProductDialog(product);
            }

            @Override
            public void onDelete(int position, Product product) {
                showDeleteConfirmationDialog(product);
            }
        });
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productsRecyclerView.setAdapter(adapter);

        initializeLaunchers();
        scanner = GmsBarcodeScanning.getClient(this);
        initLoadingDialog();

        addProductFab.setOnClickListener(v -> showAddProductDialog());
        // loadProductsFromFirestore(); // Removed in favor of ViewModel observation
        mainViewModel = new ViewModelProvider(this).get(com.bsoft.inventorymanager.viewmodels.MainViewModel.class);
        loadBrandsAndCategories();

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_products);
        swipeRefreshLayout.setOnRefreshListener(() -> mainViewModel.refreshData());

        observeViewModel();
        setupScrollListener(productsRecyclerView);
    }

    private void observeViewModel() {
        mainViewModel.getProducts().observe(this, products -> {
            if (products != null) {
                productList.clear();
                productList.addAll(products);
                adapter.notifyDataSetChanged();
            }
        });

        mainViewModel.getIsLoading().observe(this, isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
            if (loadingDialog != null) {
                if (isLoading && !loadingDialog.isShowing()) {
                    // For pagination, we usually don't show a full dialog
                } else if (!isLoading && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
        });
    }

    private void setupScrollListener(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        mainViewModel.loadNextPageProducts();
                    }
                }
            }
        });
    }

    private void initializeLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                    }
                });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && imageUri != null) {
                if (dialogProductImage != null) {
                    Glide.with(this).load(imageUri).placeholder(R.drawable.ic_product)
                            .error(R.drawable.ic_product_error).into(dialogProductImage);
                    this.apiImageUrl = null;
                }
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageUri = uri;
                if (dialogProductImage != null) {
                    Glide.with(this).load(imageUri).placeholder(R.drawable.ic_product)
                            .error(R.drawable.ic_product_error).into(dialogProductImage);
                    this.apiImageUrl = null;
                }
            }
        });
    }

    private void initLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        loadingDialog = builder.create();
    }

    // loadProductsFromFirestore removed or deprecated
    /*
     * private void loadProductsFromFirestore() {
     * productsCollection.addSnapshotListener((value, error) -> {
     * // ...
     * });
     * }
     */

    private void loadBrandsAndCategories() {
        mainViewModel.fetchUniqueBrandsAndCategories(
                new com.bsoft.inventorymanager.repositories.ProductRepository.UniqueFieldsCallback() {
                    @Override
                    public void onSuccess(List<String> brands, List<String> categories) {
                        brandList.clear();
                        brandList.addAll(brands != null ? brands : new ArrayList<>());
                        categoryList.clear();
                        categoryList.addAll(categories != null ? categories : new ArrayList<>());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error loading brands and categories", e);
                    }
                });
    }

    private void showAddProductDialog() {
        imageUri = null;
        this.apiImageUrl = null;
        this.productFromScan = null; // Reset for new dialog session
        showProductDialog(null);
    }

    private void showEditProductDialog(Product product) {
        imageUri = null;
        this.apiImageUrl = null;
        this.productFromScan = null; // Reset for new dialog session
        showProductDialog(product);
    }

    private void showProductDialog(final Product initialProductData) {
        this.productFromScan = null; // Ensure it's reset each time dialog opens
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        dialogProductImage = dialogView.findViewById(R.id.iv_product_image);
        Button btnCamera = dialogView.findViewById(R.id.btn_camera);
        Button btnGallery = dialogView.findViewById(R.id.btn_gallery);
        etProductName = dialogView.findViewById(R.id.et_product_name);
        actvBrand = dialogView.findViewById(R.id.actv_brand);
        actvCategory = dialogView.findViewById(R.id.actv_category);
        etProductCode = dialogView.findViewById(R.id.et_product_code);
        TextInputEditText etStocks = dialogView.findViewById(R.id.et_stocks);
        AutoCompleteTextView actvUnit = dialogView.findViewById(R.id.actv_unit);
        TextInputEditText etPurchasePrice = dialogView.findViewById(R.id.et_purchase_price);
        TextInputEditText etCostPrice = dialogView.findViewById(R.id.et_cost_price);
        TextInputEditText etMrp = dialogView.findViewById(R.id.et_mrp);
        TextInputEditText etWholesalePrice = dialogView.findViewById(R.id.et_wholesale_price);
        TextInputEditText etDealerPrice = dialogView.findViewById(R.id.et_dealer_price);
        ImageButton btnBarcodeScanner = dialogView.findViewById(R.id.btn_barcode_scanner);

        String[] units = { "pcs", "kg", "g", "ltr", "ml", "box", "pack", "dozen" };
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, units);
        actvUnit.setAdapter(unitAdapter);

        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                brandList);
        actvBrand.setAdapter(brandAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                categoryList);
        actvCategory.setAdapter(categoryAdapter);

        btnCamera.setOnClickListener(v -> requestPermissionLauncher.launch(Manifest.permission.CAMERA));
        btnGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnBarcodeScanner.setOnClickListener(v -> startBarcodeScan());

        String dialogTitle = (initialProductData == null) ? "Add Product" : "Edit Product";
        String positiveButtonText = (initialProductData == null) ? "Add" : "Save";

        if (initialProductData != null) {
            etProductName.setText(initialProductData.getName());
            actvBrand.setText(initialProductData.getBrand());
            actvCategory.setText(initialProductData.getCategory());
            etProductCode.setText(initialProductData.getProductCode()); // Which is the document ID
            etStocks.setText(String.valueOf(initialProductData.getQuantity()));
            actvUnit.setText(initialProductData.getUnit());
            etPurchasePrice.setText(String.valueOf(initialProductData.getPurchasePrice()));
            etCostPrice.setText(String.valueOf(initialProductData.getCostPrice()));
            etMrp.setText(String.valueOf(initialProductData.getMrp()));
            etWholesalePrice.setText(String.valueOf(initialProductData.getWholesalePrice()));
            etDealerPrice.setText(String.valueOf(initialProductData.getDealerPrice()));

            if (initialProductData.getImageUrl() != null && !initialProductData.getImageUrl().isEmpty()) {
                if (initialProductData.getImageUrl().startsWith("http")) {
                    this.apiImageUrl = initialProductData.getImageUrl();
                    if (dialogProductImage != null) {
                        Glide.with(this)
                                .load(this.apiImageUrl)
                                .placeholder(R.drawable.ic_product)
                                .error(R.drawable.ic_product_error)
                                .into(dialogProductImage);
                    }
                } else {
                    try {
                        byte[] decodedString = Base64.decode(initialProductData.getImageUrl(), Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (dialogProductImage != null)
                            dialogProductImage.setImageBitmap(decodedByte);
                    } catch (IllegalArgumentException e) {
                        if (dialogProductImage != null)
                            Glide.with(this).load(R.drawable.ic_product_error).into(dialogProductImage);
                    }
                }
            } else {
                if (dialogProductImage != null)
                    Glide.with(this).load(R.drawable.ic_product).into(dialogProductImage);
            }
        } else {
            if (dialogProductImage != null)
                Glide.with(this).load(R.drawable.ic_product).into(dialogProductImage);
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(dialogTitle)
                .setIcon(R.drawable.ic_product)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    processProductData(initialProductData, etProductName, actvBrand, actvCategory, etProductCode,
                            etStocks, actvUnit, etPurchasePrice, etCostPrice, etMrp, etWholesalePrice, etDealerPrice);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startBarcodeScan() {
        if (etProductName == null || actvBrand == null || actvCategory == null || etProductCode == null
                || dialogProductImage == null) {
            Log.w(TAG, "Dialog views not fully initialized for barcode scan.");
        }
        this.apiImageUrl = null;
        this.imageUri = null;
        // this.productFromScan = null; // Reset before a new scan operation (already
        // done in showProductDialog)

        scanner.startScan()
                .addOnSuccessListener(
                        barcode -> {
                            String rawValue = barcode.getRawValue();
                            // TODO: Implement barcode normalization here (e.g. UPC-A to EAN-13)
                            // String normalizedBarcode = normalizeBarcode(rawValue);
                            // checkFirebaseThenApi(normalizedBarcode);
                            if (rawValue != null && !rawValue.trim().isEmpty()) {
                                checkFirebaseThenApi(rawValue.trim());
                            } else {
                                Toast.makeText(this, "Empty barcode scanned.", Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnCanceledListener(
                        () -> Toast.makeText(this, "Scan canceled.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(
                        e -> Toast.makeText(this, "Scan failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void checkFirebaseThenApi(String barcodeValue) {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
        this.productFromScan = null; // Reset before check

        productsCollection.document(barcodeValue).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product productFromFirebase = documentSnapshot.toObject(Product.class);
                        if (productFromFirebase != null) {
                            productFromFirebase.setDocumentId(documentSnapshot.getId()); // ID is barcodeValue
                            this.productFromScan = productFromFirebase; // Store for save operation

                            if (loadingDialog != null && loadingDialog.isShowing()) {
                                loadingDialog.dismiss();
                            }
                            Toast.makeText(ProductActivity.this, "Product found in your inventory.", Toast.LENGTH_SHORT)
                                    .show();

                            if (etProductCode != null)
                                etProductCode.setText(productFromFirebase.getProductCode());
                            if (etProductName != null)
                                etProductName.setText(productFromFirebase.getName());
                            if (actvBrand != null)
                                actvBrand.setText(productFromFirebase.getBrand());
                            if (actvCategory != null)
                                actvCategory.setText(productFromFirebase.getCategory());

                            // Populate other fields from productFromFirebase as needed, e.g., stocks,
                            // prices
                            // TextInputEditText etStocks = dialogView.findViewById(R.id.et_stocks);
                            // if (etStocks != null)
                            // etStocks.setText(String.valueOf(productFromFirebase.getQuantity()));
                            // ... and so on for other fields ...

                            this.imageUri = null;
                            this.apiImageUrl = null;

                            if (productFromFirebase.getImageUrl() != null
                                    && !productFromFirebase.getImageUrl().isEmpty()) {
                                if (dialogProductImage != null) {
                                    if (productFromFirebase.getImageUrl().startsWith("http")) {
                                        this.apiImageUrl = productFromFirebase.getImageUrl();
                                        Glide.with(this)
                                                .load(this.apiImageUrl)
                                                .placeholder(R.drawable.ic_product)
                                                .error(R.drawable.ic_product_error)
                                                .into(dialogProductImage);
                                    } else {
                                        try {
                                            byte[] decodedString = Base64.decode(productFromFirebase.getImageUrl(),
                                                    Base64.DEFAULT);
                                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0,
                                                    decodedString.length);
                                            dialogProductImage.setImageBitmap(decodedByte);
                                        } catch (IllegalArgumentException e) {
                                            Glide.with(this).load(R.drawable.ic_product_error).into(dialogProductImage);
                                            Log.e(TAG, "Error decoding Base64 image from Firebase: " + e.getMessage());
                                        }
                                    }
                                }
                            } else {
                                if (dialogProductImage != null) {
                                    Glide.with(this).load(R.drawable.ic_product).into(dialogProductImage);
                                }
                            }
                            // The dialog title and button text might need to be updated here
                            // to reflect "Edit Product" if it was "Add Product".
                            // This requires access to the AlertDialog instance or recreating parts of it.
                            // For now, this logic is deferred.
                        } else {
                            if (loadingDialog != null && loadingDialog.isShowing()) {
                                loadingDialog.dismiss();
                            }
                            Log.w(TAG, "Product exists in Firebase but failed to parse. Barcode: " + barcodeValue
                                    + ". Querying API.");
                            fetchProductDetails(barcodeValue);
                        }
                    } else {
                        Log.i(TAG, "Product with barcode " + barcodeValue + " not found in Firebase. Querying API.");
                        fetchProductDetails(barcodeValue); // loadingDialog kept visible
                    }
                })
                .addOnFailureListener(e -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                    Log.e(TAG, "Error querying Firebase for barcode " + barcodeValue, e);
                    Toast.makeText(ProductActivity.this, "Error checking inventory. Trying online...",
                            Toast.LENGTH_SHORT).show();
                    fetchProductDetails(barcodeValue);
                });
    }

    private void fetchProductDetails(String barcode) {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        } else if (loadingDialog == null) {
            initLoadingDialog();
            loadingDialog.show();
        }
        this.productFromScan = null; // API result is not from existing scan

        ProductApiService apiService = RetrofitClient.getClient().create(ProductApiService.class);
        Call<ProductResponse> call = apiService.getProductByBarcode(barcode);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 1) {
                    ApiProduct apiProduct = response.body().getProduct();
                    if (apiProduct != null) {
                        if (etProductCode != null)
                            etProductCode.setText(barcode); // Use the scanned barcode

                        String productName = apiProduct.getProductName();
                        String brand = apiProduct.getBrands();
                        String categories = apiProduct.getCategories();
                        ProductActivity.this.apiImageUrl = apiProduct.getImageUrl();

                        if (etProductName != null) {
                            if (productName != null && !productName.isEmpty()) {
                                if (brand != null && !brand.isEmpty()) {
                                    String pattern = "(?i)" + Pattern.quote(brand);
                                    String cleanProductName = productName.replaceAll(pattern, "").trim();
                                    etProductName.setText(cleanProductName);
                                    if (actvBrand != null)
                                        actvBrand.setText(brand);
                                } else {
                                    etProductName.setText(productName);
                                }
                            } else if (brand != null && !brand.isEmpty() && actvBrand != null) {
                                actvBrand.setText(brand);
                            }

                            if (categories != null && !categories.isEmpty() && actvCategory != null) {
                                actvCategory.setText(categories.split(",")[0].trim());
                            }

                            if (ProductActivity.this.apiImageUrl != null && !ProductActivity.this.apiImageUrl.isEmpty()
                                    && dialogProductImage != null) {
                                Glide.with(ProductActivity.this)
                                        .load(ProductActivity.this.apiImageUrl)
                                        .placeholder(R.drawable.ic_product)
                                        .error(R.drawable.ic_product_error)
                                        .into(dialogProductImage);
                                ProductActivity.this.imageUri = null;
                            }
                            Toast.makeText(ProductActivity.this, "Product details found online.", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(ProductActivity.this, "Product details found, but dialog not ready.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (etProductCode != null)
                            etProductCode.setText(barcode);
                        Toast.makeText(ProductActivity.this, "Product not found online. Please enter details manually.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (etProductCode != null)
                        etProductCode.setText(barcode);
                    String message = "Product not found online (or API error).";
                    if (response.body() != null && response.body().getStatus() == 0) {
                        message = "Product not found in the online database.";
                    } else if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "API Error: " + response.code() + " " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(ProductActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                if (loadingDialog != null && loadingDialog.isShowing())
                    loadingDialog.dismiss();
                if (etProductCode != null)
                    etProductCode.setText(barcode);
                Toast.makeText(ProductActivity.this, "API call failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    private void launchCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Product Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Inventory Manager App");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        takePictureLauncher.launch(imageUri);
    }

    private void processProductData(Product originalDialogProduct, TextInputEditText etProductName,
            AutoCompleteTextView actvBrand,
            AutoCompleteTextView actvCategory, TextInputEditText etProductCodeView,
            TextInputEditText etStocks, AutoCompleteTextView actvUnit,
            TextInputEditText etPurchasePrice, TextInputEditText etCostPrice, TextInputEditText etMrp,
            TextInputEditText etWholesalePrice, TextInputEditText etDealerPrice) {

        // Security check: Verify user has permission to write products
        if (!SecurityManager.validateAccess(this, SecurityManager.PERMISSION_CAN_CREATE_PRODUCTS,
                "add or edit products")) {
            return;
        }

        String currentProductCode = etProductCodeView.getText() != null ? etProductCodeView.getText().toString().trim()
                : "";

        // Validate product code (barcode)
        String validatedProductCode = InputValidator.validateAndSanitizeBarcode(currentProductCode);
        if (validatedProductCode == null) {
            ErrorHandler.handleValidationError(this, "Product Code",
                    "Invalid barcode format. Use alphanumeric characters only (1-20 characters).");
            return;
        }

        // Validate product name
        String productName = etProductName.getText() != null ? etProductName.getText().toString().trim() : "";
        String validatedProductName = InputValidator.validateAndSanitizeName(productName);
        if (validatedProductName == null || validatedProductName.isEmpty()) {
            ErrorHandler.handleValidationError(this, "Product Name",
                    "Product name is required and must be valid (2-50 characters, letters, spaces, hyphens, apostrophes).");
            return;
        }

        Product productToProcess;
        // If a scan found an item, and its barcode matches what's in the text field,
        // prioritize it.
        if (this.productFromScan != null && this.productFromScan.getProductCode().equals(currentProductCode)) {
            productToProcess = this.productFromScan;
        }
        // Else, if the dialog was opened for editing an existing product, and its code
        // matches, use it.
        else if (originalDialogProduct != null && originalDialogProduct.getProductCode().equals(currentProductCode)) {
            productToProcess = originalDialogProduct;
        }
        // Otherwise, it's a new product or an existing product whose code has been
        // changed in the UI.
        else {
            productToProcess = null; // Indicates new product or product with changed code
        }

        String imageIdentifierToSave = null;
        if (imageUri != null) {
            try {
                Bitmap bitmap = getBitmapFromUri(imageUri);
                Bitmap resizedBitmap = resizeBitmap(bitmap, 100, 100);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] data = baos.toByteArray();
                imageIdentifierToSave = Base64.encodeToString(data, Base64.DEFAULT);
                this.apiImageUrl = null;
            } catch (IOException e) {
                ErrorHandler.handleGeneralError(this, e, "processing image");
                return; // Stop if image processing fails
            }
        } else if (this.apiImageUrl != null && this.apiImageUrl.startsWith("http")) {
            imageIdentifierToSave = this.apiImageUrl;
        } else if (productToProcess != null && productToProcess.getImageUrl() != null) { // Use existing image if no new
                                                                                         // one
            imageIdentifierToSave = productToProcess.getImageUrl();
        } else if (originalDialogProduct != null && originalDialogProduct.getImageUrl() != null
                && (this.productFromScan == null)) {
            // Fallback if productFromScan cleared things but no new image was picked.
            imageIdentifierToSave = originalDialogProduct.getImageUrl();
        }

        saveProductData(productToProcess, imageIdentifierToSave, etProductName, actvBrand, actvCategory,
                etProductCodeView, etStocks,
                actvUnit, etPurchasePrice, etCostPrice, etMrp, etWholesalePrice, etDealerPrice);
    }

    private void saveProductData(Product productBeingSaved, String imageIdentifier, TextInputEditText etProductNameView,
            AutoCompleteTextView actvBrandView,
            AutoCompleteTextView actvCategoryView, TextInputEditText etProductCodeView,
            TextInputEditText etStocksView, AutoCompleteTextView actvUnitView,
            TextInputEditText etPurchasePriceView, TextInputEditText etCostPriceView, TextInputEditText etMrpView,
            TextInputEditText etWholesalePriceView, TextInputEditText etDealerPriceView) {

        String name = etProductNameView.getText() != null ? etProductNameView.getText().toString().trim() : "";
        String brand = actvBrandView.getText() != null ? actvBrandView.getText().toString().trim() : "";
        String category = actvCategoryView.getText() != null ? actvCategoryView.getText().toString().trim() : "";
        String currentProductCodeFromUi = etProductCodeView.getText() != null
                ? etProductCodeView.getText().toString().trim()
                : "";
        String unit = actvUnitView.getText() != null ? actvUnitView.getText().toString().trim() : "";

        // Validate and sanitize inputs
        String validatedName = InputValidator.validateAndSanitizeName(name);
        String validatedBrand = brand.isEmpty() ? "" : InputValidator.validateAndSanitizeName(brand);
        String validatedCategory = category.isEmpty() ? "" : InputValidator.validateAndSanitizeName(category); // Categories
                                                                                                               // should
                                                                                                               // follow
                                                                                                               // same
                                                                                                               // rules
                                                                                                               // as
                                                                                                               // names
        String validatedProductCode = InputValidator.validateAndSanitizeBarcode(currentProductCodeFromUi);
        String validatedUnit = unit.isEmpty() ? "" : InputValidator.sanitizeInput(unit);

        if (validatedName == null) {
            ErrorHandler.handleValidationError(this, "Product Name", "Invalid product name format.");
            return;
        }

        if (validatedProductCode == null) {
            ErrorHandler.handleValidationError(this, "Product Code", "Invalid product code format.");
            return;
        }

        if (validatedBrand == null) {
            ErrorHandler.handleValidationError(this, "Brand", "Invalid brand name format.");
            return;
        }

        if (validatedCategory == null) {
            ErrorHandler.handleValidationError(this, "Category", "Invalid category name format.");
            return;
        }

        // Validate and sanitize numeric fields
        String stocksStr = etStocksView.getText() != null ? etStocksView.getText().toString().trim() : "0";
        int stocks = InputValidator.validateAndSanitizeQuantity(stocksStr);
        if (stocks < 0) {
            ErrorHandler.handleValidationError(this, "Stock Quantity", "Quantity must be a non-negative integer.");
            return;
        }

        String purchasePriceStr = etPurchasePriceView.getText() != null
                ? etPurchasePriceView.getText().toString().trim()
                : "0";
        double purchasePrice = InputValidator.validateAndSanitizePrice(purchasePriceStr);
        if (purchasePrice < 0) {
            ErrorHandler.handleValidationError(this, "Purchase Price",
                    "Price must be a positive number with up to 2 decimal places.");
            return;
        }

        String costPriceStr = etCostPriceView.getText() != null ? etCostPriceView.getText().toString().trim() : "0";
        double costPrice = InputValidator.validateAndSanitizePrice(costPriceStr);
        if (costPrice < 0) {
            ErrorHandler.handleValidationError(this, "Cost Price",
                    "Price must be a positive number with up to 2 decimal places.");
            return;
        }

        String mrpStr = etMrpView.getText() != null ? etMrpView.getText().toString().trim() : "0";
        double mrp = InputValidator.validateAndSanitizePrice(mrpStr);
        if (mrp < 0) {
            ErrorHandler.handleValidationError(this, "MRP",
                    "Price must be a positive number with up to 2 decimal places.");
            return;
        }

        String wholesalePriceStr = etWholesalePriceView.getText() != null
                ? etWholesalePriceView.getText().toString().trim()
                : "0";
        double wholesalePrice = InputValidator.validateAndSanitizePrice(wholesalePriceStr);
        if (wholesalePrice < 0) {
            ErrorHandler.handleValidationError(this, "Wholesale Price",
                    "Price must be a positive number with up to 2 decimal places.");
            return;
        }

        String dealerPriceStr = etDealerPriceView.getText() != null ? etDealerPriceView.getText().toString().trim()
                : "0";
        double dealerPrice = InputValidator.validateAndSanitizePrice(dealerPriceStr);
        if (dealerPrice < 0) {
            ErrorHandler.handleValidationError(this, "Dealer Price",
                    "Price must be a positive number with up to 2 decimal places.");
            return;
        }

        // Validate price relationships (optional but recommended)
        if (mrp > 0 && (purchasePrice > mrp || wholesalePrice > mrp || dealerPrice > mrp)) {
            Toast.makeText(this, "Warning: Some prices are higher than MRP. Please verify.", Toast.LENGTH_LONG).show();
        }

        // [CRITICAL_FIX] Validate Selling Price >= Cost Price to prevent loss
        if (!FinancialCalculator.isValidSellingPrice(mrp, costPrice)) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Potential Loss Warning")
                    .setMessage("MRP (" + mrp + ") is lower than Cost Price (" + costPrice
                            + "). This will result in a loss.")
                    .setPositiveButton("Fix", null)
                    .setNegativeButton("Ignore & Save", (dialog, which) -> {
                        proceedToSave(validatedName, imageIdentifier, validatedBrand, validatedCategory,
                                validatedProductCode,
                                stocks, validatedUnit, costPrice, purchasePrice, mrp, wholesalePrice, dealerPrice);
                    })
                    .show();
            return;
        }

        // Check other selling prices too if they are used regularly
        if (!FinancialCalculator.isValidSellingPrice(dealerPrice, costPrice) ||
                !FinancialCalculator.isValidSellingPrice(wholesalePrice, costPrice)) {
            Toast.makeText(this, "Warning: Dealer or Wholesale price is below Cost Price.", Toast.LENGTH_LONG).show();
        }

        proceedToSave(validatedName, imageIdentifier, validatedBrand, validatedCategory, validatedProductCode,
                stocks, validatedUnit, costPrice, purchasePrice, mrp, wholesalePrice, dealerPrice);
    }

    // Extracted save logic to a separate method to allow "Proceed Anyway"
    // confirmation
    private void proceedToSave(String name, String imageIdentifier, String brand, String category, String productCode,
            int stocks, String unit, double costPrice, double purchasePrice, double mrp,
            double wholesalePrice, double dealerPrice) {
        Product productToSave = new Product(name, imageIdentifier, brand, category, productCode, stocks, unit,
                costPrice, purchasePrice, mrp, wholesalePrice, dealerPrice);

        productToSave.setDocumentId(productCode); // Barcode is the document ID

        if (loadingDialog != null && !loadingDialog.isShowing())
            loadingDialog.show();

        mainViewModel.saveProduct(productToSave,
                new com.bsoft.inventorymanager.repositories.ProductRepository.ProductCallback() {
                    @Override
                    public void onSuccess() {
                        if (loadingDialog != null && loadingDialog.isShowing())
                            loadingDialog.dismiss();
                        Toast.makeText(ProductActivity.this, "Product saved successfully.", Toast.LENGTH_SHORT).show();
                        loadBrandsAndCategories(); // Refresh dropdowns
                        productFromScan = null; // Clear after successful save
                    }

                    @Override
                    public void onError(Exception e) {
                        if (loadingDialog != null && loadingDialog.isShowing())
                            loadingDialog.dismiss();
                        ErrorHandler.handleFirestoreError(ProductActivity.this,
                                (com.google.firebase.firestore.FirebaseFirestoreException) e, "saving product");
                        Log.e(TAG, "Error saving product to ID " + productCode, e);
                    }
                });

    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), uri));
        } else {
            // SuppressLint("deprecation") for MediaStore.Images.Media.getBitmap
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
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

    private void showDeleteConfirmationDialog(Product product) {
        // Security check: Verify user has permission to delete products
        if (!SecurityManager.validateAccess(this, SecurityManager.PERMISSION_CAN_DELETE_PRODUCTS, "delete products")) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setIcon(R.drawable.ic_product)
                .setMessage("Are you sure you want to delete \"" + product.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteFirestoreDocument(product);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFirestoreDocument(Product product) {
        // Additional security check: Verify user has permission to delete products
        if (!SecurityManager.validateAccess(this, SecurityManager.PERMISSION_CAN_DELETE_PRODUCTS, "delete products")) {
            return;
        }

        if (product.getDocumentId() == null || product.getDocumentId().isEmpty()) {
            ErrorHandler.handleValidationError(this, "Product ID", "Product ID is missing, cannot delete.");
            return;
        }

        if (loadingDialog != null && !loadingDialog.isShowing())
            loadingDialog.show();

        mainViewModel.deleteProduct(product,
                new com.bsoft.inventorymanager.repositories.ProductRepository.ProductCallback() {
                    @Override
                    public void onSuccess() {
                        if (loadingDialog != null && loadingDialog.isShowing())
                            loadingDialog.dismiss();
                        Toast.makeText(ProductActivity.this, "Product deleted successfully.", Toast.LENGTH_SHORT)
                                .show();
                        productFromScan = null; // Clear if the deleted item was from a scan
                    }

                    @Override
                    public void onError(Exception e) {
                        if (loadingDialog != null && loadingDialog.isShowing())
                            loadingDialog.dismiss();
                        ErrorHandler.handleFirestoreError(ProductActivity.this,
                                (com.google.firebase.firestore.FirebaseFirestoreException) e, "deleting product");
                        Log.e(TAG, "Error deleting product", e);
                    }
                });
    }
}
