package com.bsoft.inventorymanager.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.SelectedProductsAdapter;
import com.bsoft.inventorymanager.model.Customer;
import com.bsoft.inventorymanager.models.PaymentDraft;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.ProductSelection;
import com.bsoft.inventorymanager.models.Sale;
import com.bsoft.inventorymanager.models.SaleItem;
import com.bsoft.inventorymanager.utils.SecurityManager;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import com.bsoft.inventorymanager.utils.FinancialCalculator;
import com.bsoft.inventorymanager.viewmodels.CreateSaleViewModel;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateSaleActivity extends BaseActivity
        implements SelectedProductsAdapter.OnProductInteractionListener, PaymentSheet.PaymentSheetListener {

    @Override
    public void onPriceChanged(int position, double newPrice) {
        viewModel.updateItemPrice(position, newPrice);
    }

    @Override
    public void onQuantityChanged(int position, int newQuantity, double unitPrice) {
        viewModel.updateItemQuantity(position, newQuantity);
    }

    @Override
    public void onRemoveProductClicked(int position) {
        viewModel.removeProduct(position);
    }

    private void calculateAndDisplaySubtotal() {
        // No-op, managed by ViewModel observer
    }

    private static final String TAG = "CreateSaleActivity";

    private Spinner customerSpinner;
    private Button saveSaleButton;
    private RecyclerView selectedProductsRecyclerView;
    private SelectedProductsAdapter selectedProductsAdapter;
    private ImageButton imageButtonCalendar;
    private ImageButton imageButtonAddNewCustomer;

    private TextView totalAmountTextView;

    private ArrayAdapter<String> customerSpinnerAdapter;

    private CreateSaleViewModel viewModel;

    // Remove direct DB refs
    // private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // ...

    private String editingSaleId = null;
    private Date selectedSaleDate = new Date();
    private GmsBarcodeScanner scanner;

    // Restored field
    private Customer selectedCustomer;
    private PaymentDraft paymentDraft = null;

    private final ActivityResultLauncher<Intent> selectProductLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Product product = result.getData().getParcelableExtra(SelectProductActivity.EXTRA_SELECTED_PRODUCT);
                    if (product != null) {
                        viewModel.addProduct(product);
                    }
                }
            });

    // ... (rest of onCreate)

    // Restored methods
    private void loadSaleDetailsForEditing(String saleId) {
        viewModel.loadSale(saleId);
    }

    public void updateCustomerSelectionUI(Customer customer) {
        // Implementation from previous file version
        // This is actually used by ViewModel Observer reference
        this.selectedCustomer = customer;
        customerSpinnerAdapter.clear();
        if (customer != null) {
            String customerDisplayText = customer.getName();
            if (customer.getContactNumber() != null && !customer.getContactNumber().isEmpty()) {
                customerDisplayText += " (" + customer.getContactNumber() + ")";
            }
            customerSpinnerAdapter.add(customerDisplayText);
        } else {
            customerSpinnerAdapter.add("Select Customer");
        }
        customerSpinnerAdapter.notifyDataSetChanged();
        customerSpinner.setSelection(0);
        customerSpinner.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        selectedProductsAdapter = new SelectedProductsAdapter(
                new ArrayList<>(),
                this::onScanProductClicked,
                this::onAddManuallyClicked,
                this,
                false // isPurchase false
        );
        selectedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedProductsRecyclerView.setAdapter(selectedProductsAdapter);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedSaleDate);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                    newDate.set(Calendar.MILLISECOND, 0);
                    selectedSaleDate = newDate.getTime();
                    // Just update date, subtotal is separate.
                    // But if UI shows date, we should update that.
                    // For now, minimal restoration.
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private final ActivityResultLauncher<Intent> selectCustomerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Refactored to get ID and load, to match KMP migration and intent payload
                    // change
                    String customerId = result.getData().getStringExtra("SELECTED_CUSTOMER_ID");
                    if (customerId != null && !customerId.isEmpty()) {
                        viewModel.loadCustomerById(customerId);
                    }
                }
            });

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sale);

        // Initialize ViewModel (Hilt injection should happen automatically if field
        // injected, or use Provider)
        // Since we are adding Hilt, we need @AndroidEntryPoint on Activity and use new
        // ViewModelProvider(this).get(...)
        // But first let's add @AndroidEntryPoint annotation to class.

        viewModel = new ViewModelProvider(this).get(CreateSaleViewModel.class);

        customerSpinner = findViewById(R.id.spinner_customer);
        saveSaleButton = findViewById(R.id.button_finalize_sale);
        selectedProductsRecyclerView = findViewById(R.id.recycler_view_selected_products);
        imageButtonAddNewCustomer = findViewById(R.id.imageButton_add_new_customer);
        imageButtonCalendar = findViewById(R.id.imageButton_calendar);
        totalAmountTextView = findViewById(R.id.tv_total_amount);

        // Observers
        viewModel.getProductSelections().observe(this, selections -> {
            selectedProductsAdapter.updateList(selections);
        });

        viewModel.getSubtotal().observe(this, subtotal -> {
            totalAmountTextView.setText(String.format("Total: %.2f", subtotal));
        });

        viewModel.getSelectedCustomer().observe(this, this::updateCustomerSelectionUI);

        viewModel.isLoading().observe(this, isLoading -> {
            if (isLoading)
                showLoadingIndicator("Processing...");
            else
                hideLoadingIndicator();
        });

        viewModel.getError().observe(this, this::showErrorToast);

        viewModel.getSaleSuccess().observe(this, saleId -> {
            showSuccessToast("Sale saved successfully!");
            finish();
        });

        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        try {
            scanner = GmsBarcodeScanning.getClient(this, options);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize barcode scanner", e);
            Toast.makeText(this, "Barcode scanner unavailable", Toast.LENGTH_SHORT).show();
        }

        customerSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        customerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customerSpinner.setAdapter(customerSpinnerAdapter);

        setupRecyclerView();

        imageButtonCalendar.setOnClickListener(v -> showDatePickerDialog());
        imageButtonAddNewCustomer.setOnClickListener(v -> {
            // Updated to reference activity-relative sheet
            com.bsoft.inventorymanager.activities.AddEditCustomerSheet sheet = com.bsoft.inventorymanager.activities.AddEditCustomerSheet
                    .newInstance(null);
            Bundle args = sheet.getArguments();
            if (args == null)
                args = new Bundle();
            args.putBoolean("RETURN_DIRECTLY", true);
            sheet.setArguments(args);
            sheet.show(getSupportFragmentManager(), "AddEditCustomerSheet");
        });

        // Listen for new customer results (e.g. from AddEditCustomerSheet returning
        // result)
        getSupportFragmentManager().setFragmentResultListener("customer_update", this, (requestKey, bundle) -> {
            // If the sheet doesn't return ID directly, we might need a different mechanism
            // or it just updates DB.
            // If it updates DB, we don't know the ID easily unless passed.
            // AddEditCustomerSheet logic was updated to startActivityForResult logic OR
            // fragment result.
            // If it uses startActivityForResult (legacy path via RETURN_ON_ADD intent), we
            // need to handle that.
            // But since it's a sheet in this activity, it might dismiss.

            // To be safe, AddEditCustomerSheet needs to callback with the ID.
            // Previous impl of AddEditCustomerSheet just dismissed or used
            // setFragmentResult.
            // I'll stick to the previous pattern: sheet might just close.
            // If we want auto-selection, we need the ID.
        });

        customerSpinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Intent intent = new Intent(CreateSaleActivity.this, SelectCustomerActivity.class);
                selectCustomerLauncher.launch(intent);
                return true;
            }
            return false;
        });

        saveSaleButton.setOnClickListener(v -> {
            openPaymentSheet();
        });

        updateCustomerSelectionUI(null);

        if (getIntent().hasExtra("EDIT_SALE_ID")) {
            editingSaleId = getIntent().getStringExtra("EDIT_SALE_ID");
            saveSaleButton.setText("Update Sale");
            showLoadingIndicator("Loading sale details...");
            loadSaleDetailsForEditing(editingSaleId);
        } else {
            calculateAndDisplaySubtotal();
        }
    }

    private void openPaymentSheet() {
        List<ProductSelection> currentSelections = viewModel.getProductSelections().getValue();
        if (currentSelections == null || currentSelections.isEmpty()) {
            showErrorToast("Please add at least one product to the sale.");
            return;
        }
        double subtotal = calculateSubtotal();
        double taxPercent = paymentDraft != null ? paymentDraft.taxPercent : 0;
        double taxAmount = paymentDraft != null ? paymentDraft.taxAmount : 0;
        double discountPercent = paymentDraft != null ? paymentDraft.discountPercent : 0;
        double discountAmount = paymentDraft != null ? paymentDraft.discountAmount : 0;
        String paymentMethod = paymentDraft != null && paymentDraft.paymentMethod != null ? paymentDraft.paymentMethod
                : "Cash";
        double amountPaid = paymentDraft != null ? paymentDraft.amountPaid : 0;
        String notes = paymentDraft != null && paymentDraft.notes != null ? paymentDraft.notes : "";
        PaymentSheet paymentSheet = PaymentSheet.newInstance(
                new java.util.ArrayList<>(),
                currentSelections,
                subtotal,
                taxPercent,
                taxAmount,
                discountPercent,
                discountAmount,
                paymentMethod,
                amountPaid,
                notes,
                "Status: Draft",
                "Payment: Pending",
                "Sale");
        paymentSheet.show(getSupportFragmentManager(), "PaymentSheet");
    }

    @Override
    public void onPaymentDraftChanged(com.bsoft.inventorymanager.models.PaymentDraft draft) {
        this.paymentDraft = draft;
    }

    public void onScanProductClicked() {
        scanner.startScan()
                .addOnSuccessListener(
                        barcode -> {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null && !rawValue.trim().isEmpty()) {
                                viewModel.loadProductByBarcode(rawValue.trim());
                            } else {
                                Toast.makeText(this, "No barcode data found.", Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnCanceledListener(() -> {
                    Toast.makeText(this, "Scan canceled.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Scan failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void onAddManuallyClicked() {
        Intent intent = new Intent(this, SelectProductActivity.class);
        selectProductLauncher.launch(intent);
    }

    @Override
    public void onTransactionFinalized(
            List<ProductSelection> updatedProductSelections,
            double taxAmount,
            double discountAmount,
            String paymentMethod,
            double amountPaid,
            String notes) {

        viewModel.saveSale(selectedSaleDate, taxAmount, discountAmount, paymentMethod, amountPaid, notes);
    }

    private double calculateSubtotal() {
        Double val = viewModel.getSubtotal().getValue();
        return val != null ? val : 0.0;
    }

}
