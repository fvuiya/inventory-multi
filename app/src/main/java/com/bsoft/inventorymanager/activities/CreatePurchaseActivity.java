package com.bsoft.inventorymanager.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.SelectedProductsAdapter;
import com.bsoft.inventorymanager.models.PaymentDraft;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.ProductSelection;
import com.bsoft.inventorymanager.model.Supplier;
import com.bsoft.inventorymanager.viewmodels.CreatePurchaseViewModel;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreatePurchaseActivity extends BaseActivity
        implements SelectedProductsAdapter.OnProductInteractionListener, PaymentSheet.PaymentSheetListener {

    private static final String TAG = "CreatePurchaseActivity";

    private CreatePurchaseViewModel viewModel;

    private Spinner supplierSpinner;
    private Button savePurchaseButton;
    private RecyclerView selectedProductsRecyclerView;
    private SelectedProductsAdapter selectedProductsAdapter;
    private ImageButton imageButtonCalendar;
    private ImageButton imageButtonAddNewSupplier;
    private TextView totalAmountTextView;

    private ArrayAdapter<String> supplierSpinnerAdapter;
    private List<Supplier> supplierList = new ArrayList<>();
    private Supplier selectedSupplier;

    private GmsBarcodeScanner scanner;
    private Date selectedPurchaseDate = new Date();
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

    private final ActivityResultLauncher<Intent> selectSupplierLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String json = result.getData().getStringExtra(SelectSupplierActivity.EXTRA_SELECTED_SUPPLIER);
                    if (json != null) {
                        Supplier supplier = com.bsoft.inventorymanager.utils.SupplierSerializationHelper
                                .deserialize(json);
                        viewModel.setSelectedSupplier(supplier);
                    }
                }
            });

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_purchase);

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(CreatePurchaseViewModel.class);

        supplierSpinner = findViewById(R.id.spinner_supplier);
        savePurchaseButton = findViewById(R.id.button_finalize_purchase);
        selectedProductsRecyclerView = findViewById(R.id.recycler_view_selected_products_for_purchase);
        totalAmountTextView = findViewById(R.id.tv_total_amount_purchase);
        imageButtonCalendar = findViewById(R.id.imageButton_calendar_purchase);
        imageButtonAddNewSupplier = findViewById(R.id.imageButton_add_new_supplier_purchase);

        // Observers
        viewModel.getProductSelections().observe(this, selections -> {
            selectedProductsAdapter.updateList(selections);
        });

        viewModel.getSubtotal().observe(this, subtotal -> {
            totalAmountTextView.setText(String.format("Total: %.2f", subtotal));
        });

        viewModel.getSuppliers().observe(this, suppliers -> {
            this.supplierList = suppliers;
            // logic to update spinner if needed, or we just keep local list for selection
        });

        viewModel.getSelectedSupplier().observe(this, this::updateSupplierSelectionUI);

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading)
                showLoadingIndicator("Processing...");
            else
                hideLoadingIndicator();
        });

        viewModel.getError().observe(this, this::showErrorToast);

        viewModel.getPurchaseSuccess().observe(this, purchaseId -> {
            showSuccessToast("Purchase saved successfully!");
            finish();
        });

        // Initialize UI Components
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        try {
            scanner = GmsBarcodeScanning.getClient(this, options);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize scanner", e);
        }

        supplierSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        supplierSpinner.setAdapter(supplierSpinnerAdapter);

        setupRecyclerView();

        imageButtonCalendar.setOnClickListener(v -> showDatePickerDialog());
        imageButtonAddNewSupplier.setOnClickListener(v -> {
            AddEditSupplierSheet sheet = AddEditSupplierSheet.newInstance(null);
            Bundle args = sheet.getArguments();
            if (args == null)
                args = new Bundle();
            args.putBoolean("RETURN_DIRECTLY", true);
            sheet.setArguments(args);
            sheet.show(getSupportFragmentManager(), "AddEditSupplierSheet");
        });

        supplierSpinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Intent intent = new Intent(CreatePurchaseActivity.this, SelectSupplierActivity.class);
                selectSupplierLauncher.launch(intent);
                return true;
            }
            return false;
        });

        savePurchaseButton.setOnClickListener(v -> openPaymentSheet());

        updateSupplierSelectionUI(null);

        if (getIntent().hasExtra("EDIT_PURCHASE_ID")) {
            String editingId = getIntent().getStringExtra("EDIT_PURCHASE_ID");
            savePurchaseButton.setText("Update Purchase");
            viewModel.loadPurchaseForEditing(editingId);
        }
    }

    private void setupRecyclerView() {
        selectedProductsAdapter = new SelectedProductsAdapter(
                new ArrayList<>(), // Initial empty list
                this::onScanProductClicked,
                this::onAddManuallyClicked,
                this,
                true // isPurchase
        );
        selectedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedProductsRecyclerView.setAdapter(selectedProductsAdapter);
    }

    public void onScanProductClicked() {
        if (scanner != null) {
            scanner.startScan()
                    .addOnSuccessListener(barcode -> {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null && !rawValue.trim().isEmpty()) {
                            viewModel.loadProductByBarcode(rawValue.trim());
                        } else {
                            Toast.makeText(this, "No barcode data found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Scan failed", Toast.LENGTH_SHORT).show());
        }
    }

    public void onAddManuallyClicked() {
        Intent intent = new Intent(this, SelectProductActivity.class);
        intent.putExtra(SelectProductActivity.EXTRA_IS_PURCHASE, true);
        selectProductLauncher.launch(intent);
    }

    @Override
    public void onRemoveProductClicked(int position) {
        viewModel.removeProduct(position);
    }

    @Override
    public void onQuantityChanged(int position, int newQuantity, double unitPrice) {
        viewModel.updateItemQuantity(position, newQuantity);
    }

    @Override
    public void onPriceChanged(int position, double newPrice) {
        viewModel.updateItemPrice(position, newPrice);
    }

    public void updateSupplierSelectionUI(Supplier supplier) {
        this.selectedSupplier = supplier;
        supplierSpinnerAdapter.clear();
        if (supplier != null) {
            String text = supplier.getName();
            if (supplier.getContactNumber() != null)
                text += " (" + supplier.getContactNumber() + ")";
            supplierSpinnerAdapter.add(text);
        } else {
            supplierSpinnerAdapter.add("Select Supplier");
        }
        supplierSpinnerAdapter.notifyDataSetChanged();
        supplierSpinner.setSelection(0);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedPurchaseDate);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    selectedPurchaseDate = newDate.getTime();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void openPaymentSheet() {
        Double subtotalVal = viewModel.getSubtotal().getValue();
        double subtotal = subtotalVal != null ? subtotalVal : 0.0;

        double taxPercent = paymentDraft != null ? paymentDraft.taxPercent : 0;
        double taxAmount = paymentDraft != null ? paymentDraft.taxAmount : 0;
        double discountPercent = paymentDraft != null ? paymentDraft.discountPercent : 0;
        double discountAmount = paymentDraft != null ? paymentDraft.discountAmount : 0;
        String paymentMethod = paymentDraft != null && paymentDraft.paymentMethod != null ? paymentDraft.paymentMethod
                : "Cash";
        double amountPaid = paymentDraft != null ? paymentDraft.amountPaid : 0;
        String notes = paymentDraft != null && paymentDraft.notes != null ? paymentDraft.notes : "";

        PaymentSheet paymentSheet = PaymentSheet.newInstance(
                new ArrayList<>(),
                viewModel.getProductSelections().getValue(),
                subtotal,
                taxPercent,
                taxAmount,
                discountPercent,
                discountAmount,
                paymentMethod,
                amountPaid,
                notes,
                "Status: COMPLETED",
                "Payment: Pending",
                "Purchase");
        paymentSheet.show(getSupportFragmentManager(), "PaymentSheet");
    }

    @Override
    public void onTransactionFinalized(
            List<ProductSelection> updatedProductSelections,
            double taxAmount,
            double discountAmount,
            String paymentMethod,
            double amountPaid,
            String notes) {

        viewModel.savePurchase(selectedPurchaseDate, taxAmount, discountAmount, paymentMethod, amountPaid, notes);
    }

    @Override
    public void onPaymentDraftChanged(PaymentDraft draft) {
        this.paymentDraft = draft;
    }
}
