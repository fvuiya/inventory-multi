package com.bsoft.inventorymanager.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.SelectedProductsAdapter;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.ProductSelection;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.PurchaseItem;
import com.bsoft.inventorymanager.models.PurchaseReturn;
import com.bsoft.inventorymanager.models.PurchaseReturnItem;
import com.bsoft.inventorymanager.activities.PaymentSheet;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.bsoft.inventorymanager.utils.FinancialCalculator;

import java.util.ArrayList;
import java.util.List;

public class CreatePurchaseReturnActivity extends BaseActivity
        implements SelectedProductsAdapter.OnProductInteractionListener, PaymentSheet.PaymentSheetListener {
    private static final String TAG = "CreatePurchaseReturn";

    private Spinner supplierSpinner;
    private Button finalizeReturnButton;
    private RecyclerView selectedProductsRecyclerView;
    private SelectedProductsAdapter selectedProductsAdapter;
    private ImageButton imageButtonCalendar;
    private ImageButton imageButtonAddNewSupplier;
    private TextView totalAmountTextView;

    private final List<ProductSelection> selectedProductSelections = new ArrayList<>();
    private ArrayAdapter<String> supplierSpinnerAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference purchasesCollection = db.collection("purchases");
    private final CollectionReference productsCollection = db.collection("products");
    private final CollectionReference returnsCollection = db.collection("purchase_returns");

    private String originalPurchaseId;
    private Purchase originalPurchase;
    private Purchase loadedPurchaseSnapshot;

    private com.bsoft.inventorymanager.models.PaymentDraft paymentDraft = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_purchase);

        // Initialize Views
        supplierSpinner = findViewById(R.id.spinner_supplier);
        finalizeReturnButton = findViewById(R.id.button_finalize_purchase);
        selectedProductsRecyclerView = findViewById(R.id.recycler_view_selected_products_for_purchase);
        imageButtonAddNewSupplier = findViewById(R.id.imageButton_add_new_supplier_purchase);
        imageButtonCalendar = findViewById(R.id.imageButton_calendar_purchase);
        totalAmountTextView = findViewById(R.id.tv_total_amount_purchase);

        // UI Adjustments for Return Mode
        setTitle("Return Purchase");
        finalizeReturnButton.setText("Finalize Return");
        imageButtonAddNewSupplier.setVisibility(View.GONE);
        imageButtonCalendar.setVisibility(View.GONE);
        supplierSpinner.setEnabled(false);

        // Setup Adapter
        selectedProductsAdapter = new SelectedProductsAdapter(
                selectedProductSelections,
                null,
                null,
                this,
                true, // isPurchase (true)
                true // isReturn (true)
        );
        selectedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedProductsRecyclerView.setAdapter(selectedProductsAdapter);

        // Setup Spinner
        supplierSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        supplierSpinner.setAdapter(supplierSpinnerAdapter);

        finalizeReturnButton.setOnClickListener(v -> openPaymentSheet());

        originalPurchaseId = getIntent().getStringExtra("PURCHASE_ID");
        if (originalPurchaseId == null) {
            Toast.makeText(this, "No Purchase ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOriginalPurchase();
    }

    private void loadOriginalPurchase() {
        showLoadingIndicator("Loading purchase details...");

        purchasesCollection.document(originalPurchaseId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        originalPurchase = documentSnapshot.toObject(Purchase.class);
                        if (originalPurchase != null) {
                            originalPurchase.setDocumentId(documentSnapshot.getId());
                            loadedPurchaseSnapshot = originalPurchase;

                            // Setup Supplier Display
                            supplierSpinnerAdapter.add(originalPurchase.getSupplierName());
                            supplierSpinnerAdapter.notifyDataSetChanged();

                            populateProductsFromPurchase(originalPurchase.getItems());
                        } else {
                            hideLoadingIndicator();
                            finish();
                        }
                    } else {
                        hideLoadingIndicator();
                        Toast.makeText(this, "Purchase not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingIndicator();
                    Toast.makeText(this, "Error loading purchase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateProductsFromPurchase(List<PurchaseItem> items) {
        if (items == null || items.isEmpty()) {
            hideLoadingIndicator();
            return;
        }

        selectedProductSelections.clear();

        for (PurchaseItem item : items) {
            int originalQty = item.getQuantity();
            int alreadyReturned = item.getReturnedQuantity();
            int returnableQty = originalQty - alreadyReturned;

            if (returnableQty > 0) {
                Product product = new Product();
                product.setDocumentId(item.getProductId());
                product.setName(item.getProductName());
                product.setPurchasePrice(item.getPricePerItem());
                product.setSellingPrice(0); // Not used in this context
                product.setQuantity(0); // Not used for limit

                int initQty = 1;
                if (returnableQty < 1)
                    initQty = 0;

                ProductSelection selection = new ProductSelection(
                        product, initQty);
                selection.setMaxReturnableQuantity(returnableQty); // STRICT LIMIT
                selectedProductSelections.add(selection);
            }
        }

        selectedProductsAdapter.notifyDataSetChanged();
        calculateAndDisplaySubtotal();
        hideLoadingIndicator();

        if (selectedProductSelections.isEmpty()) {
            Toast.makeText(this, "This purchase has been fully returned previously.", Toast.LENGTH_LONG).show();
        }
    }

    private void calculateAndDisplaySubtotal() {
        double subtotal = 0.0;
        for (ProductSelection selection : selectedProductSelections) {
            subtotal += selection.getProduct().getPurchasePrice() * selection.getQuantityInSale();
        }
        totalAmountTextView.setText(String.format("Credit Total: %.2f", subtotal));
    }

    private void openPaymentSheet() {
        if (selectedProductSelections.isEmpty()) {
            Toast.makeText(this, "No items to return.", Toast.LENGTH_SHORT).show();
            return;
        }
        double creditAmount = 0.0;
        for (ProductSelection selection : selectedProductSelections) {
            creditAmount += selection.getProduct().getPurchasePrice() * selection.getQuantityInSale();
        }

        if (creditAmount <= 0) {
            Toast.makeText(this, "Amount match be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        PaymentSheet paymentSheet = PaymentSheet.newInstance(
                new ArrayList<>(),
                selectedProductSelections,
                creditAmount,
                0, 0, 0, 0,
                "Cash",
                creditAmount,
                "",
                "Status: Completed",
                "Payment: Credit/Refunded",
                "Purchase Return");
        paymentSheet.show(getSupportFragmentManager(), "PaymentSheet_Return_Purchase");
    }

    @Override
    public void onTransactionFinalized(List<ProductSelection> updatedProductSelections,
            double taxAmount, double discountAmount, String paymentMethod, double amountPaid, String notes) {
        processReturnTransaction(paymentMethod, amountPaid, notes);
    }

    @Override
    public void onPaymentDraftChanged(com.bsoft.inventorymanager.models.PaymentDraft draft) {
        this.paymentDraft = draft;
    }

    private void processReturnTransaction(String paymentMethod, double refundAmount, String notes) {
        showLoadingIndicator("Processing Return...");

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // 1. Re-read Purchase
            DocumentReference purchaseRef = purchasesCollection.document(originalPurchaseId);
            Purchase freshPurchase = transaction.get(purchaseRef).toObject(Purchase.class);
            if (freshPurchase == null) {
                throw new ArithmeticException("Purchase not found.");
            }

            // 2. Validate quantities
            List<PurchaseReturnItem> returnItems = new ArrayList<>();
            List<PurchaseItem> freshItems = freshPurchase.getItems();

            double creditTotal = 0;

            for (ProductSelection selection : selectedProductSelections) {
                if (selection.getQuantityInSale() <= 0)
                    continue;

                String pid = selection.getProduct().getDocumentId();
                PurchaseItem purchaseItem = null;
                for (PurchaseItem pi : freshItems) {
                    if (pi.getProductId().equals(pid)) {
                        purchaseItem = pi;
                        break;
                    }
                }

                if (purchaseItem == null) {
                    throw new ArithmeticException(
                            "Item " + selection.getProduct().getName() + " not found in original purchase.");
                }

                int maxReturnable = purchaseItem.getQuantity() - purchaseItem.getReturnedQuantity();
                if (selection.getQuantityInSale() > maxReturnable) {
                    throw new ArithmeticException("Cannot return " + selection.getQuantityInSale() + " of "
                            + selection.getProduct().getName() + ". Only " + maxReturnable + " remaining.");
                }

                // 3. Update PurchaseItem returned qty
                purchaseItem.setReturnedQuantity(purchaseItem.getReturnedQuantity() + selection.getQuantityInSale());

                // Add to Return Doc List
                PurchaseReturnItem returnItem = new PurchaseReturnItem();
                returnItem.setProductId(pid);
                returnItem.setProductName(purchaseItem.getProductName());
                returnItem.setQuantity(selection.getQuantityInSale());
                returnItem.setPricePerItem(purchaseItem.getPricePerItem());
                returnItems.add(returnItem);

                // Use FinancialCalculator for credit math
                creditTotal = FinancialCalculator.add(creditTotal,
                        FinancialCalculator.multiply(returnItem.getPricePerItem(), returnItem.getQuantity()));

                // 4. Update Product Inventory (DECREASE Stock)
                DocumentReference productRef = productsCollection.document(pid);
                Product productInDb = transaction.get(productRef).toObject(Product.class);
                if (productInDb == null) {
                    throw new ArithmeticException(
                            "Product " + selection.getProduct().getName() + " not found in inventory.");
                }

                int currentStock = productInDb.getQuantity();
                if (currentStock < selection.getQuantityInSale()) {
                    throw new ArithmeticException("Insufficient stock for " + selection.getProduct().getName() +
                            ". Current: " + currentStock + ", Trying to Return: " + selection.getQuantityInSale());
                }

                transaction.update(productRef, "quantity", FieldValue.increment(-selection.getQuantityInSale()));
            }

            if (returnItems.isEmpty()) {
                throw new ArithmeticException("No items selected for return.");
            }

            // 5. Update Purchase Document
            // Recalibrate the totalAmount of the original purchase
            freshPurchase.setTotalAmount(FinancialCalculator.subtract(freshPurchase.getTotalAmount(), creditTotal));
            transaction.set(purchaseRef, freshPurchase);

            // 6. Create Return Document
            DocumentReference returnRef = returnsCollection.document();
            PurchaseReturn purchaseReturn = new PurchaseReturn();
            purchaseReturn.setDocumentId(returnRef.getId());
            purchaseReturn.setOriginalPurchaseId(originalPurchaseId);
            purchaseReturn.setSupplierId(freshPurchase.getSupplierId());
            purchaseReturn.setSupplierName(freshPurchase.getSupplierName());
            purchaseReturn.setReturnDate(Timestamp.now());
            purchaseReturn.setUserId(FirebaseAuth.getInstance().getUid());
            purchaseReturn.setItems(returnItems);
            purchaseReturn.setTotalCreditAmount(creditTotal);

            transaction.set(returnRef, purchaseReturn);

            return null;
        }).addOnSuccessListener(aVoid -> {
            hideLoadingIndicator();
            showSuccessToast("Return Processed Successfully");
            setResult(Activity.RESULT_OK);
            finish();
        }).addOnFailureListener(e -> {
            hideLoadingIndicator();
            if (e instanceof ArithmeticException) {
                showErrorToast(e.getMessage());
            } else {
                showErrorToast("Error processing return: " + e.getMessage());
            }
        });
    }

    @Override
    public void onRemoveProductClicked(int position) {
        if (position >= 0 && position < selectedProductSelections.size()) {
            selectedProductSelections.remove(position);
            selectedProductsAdapter.notifyItemRemoved(position);
            calculateAndDisplaySubtotal();
        }
    }

    @Override
    public void onQuantityChanged(int position, int newQuantity, double unitPrice) {
        calculateAndDisplaySubtotal();
    }

    @Override
    public void onPriceChanged(int position, double newPrice) {
        calculateAndDisplaySubtotal();
    }
}
