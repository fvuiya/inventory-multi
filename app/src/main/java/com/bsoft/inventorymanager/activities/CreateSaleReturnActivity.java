package com.bsoft.inventorymanager.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.bsoft.inventorymanager.models.Customer;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.ProductSelection;
import com.bsoft.inventorymanager.models.Sale;
import com.bsoft.inventorymanager.models.SaleItem;
import com.bsoft.inventorymanager.models.SaleReturn;
import com.bsoft.inventorymanager.models.SaleReturnItem;
import com.bsoft.inventorymanager.activities.PaymentSheet;
import com.bsoft.inventorymanager.utils.FinancialCalculator;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CreateSaleReturnActivity extends BaseActivity
        implements SelectedProductsAdapter.OnProductInteractionListener, PaymentSheet.PaymentSheetListener {
    private static final String TAG = "CreateSaleReturn";

    private Spinner customerSpinner;
    private Button finalizeReturnButton;
    private RecyclerView selectedProductsRecyclerView;
    private SelectedProductsAdapter selectedProductsAdapter;
    private ImageButton imageButtonCalendar;
    private ImageButton imageButtonAddNewCustomer;
    private TextView totalAmountTextView;

    private final List<ProductSelection> selectedProductSelections = new ArrayList<>();
    private ArrayAdapter<String> customerSpinnerAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference salesCollection = db.collection("sales");
    private final CollectionReference productsCollection = db.collection("products");
    private final CollectionReference returnsCollection = db.collection("sales_returns");

    private String originalSaleId;
    private Sale originalSale;

    // Track original quantities to validation
    private Sale loadedSaleSnapshot;

    private com.bsoft.inventorymanager.models.PaymentDraft paymentDraft = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sale); // Reuse Sale layout

        // Initialize Views (Same IDs as CreateSaleActivity)
        customerSpinner = findViewById(R.id.spinner_customer);
        finalizeReturnButton = findViewById(R.id.button_finalize_sale);
        selectedProductsRecyclerView = findViewById(R.id.recycler_view_selected_products);
        imageButtonAddNewCustomer = findViewById(R.id.imageButton_add_new_customer);
        imageButtonCalendar = findViewById(R.id.imageButton_calendar);
        totalAmountTextView = findViewById(R.id.tv_total_amount);

        // UI Adjustments for Return Mode
        setTitle("Return Sale");
        finalizeReturnButton.setText("Finalize Return");
        imageButtonAddNewCustomer.setVisibility(View.GONE); // Cannot add new customer in return
        imageButtonCalendar.setVisibility(View.GONE); // Return date is always NOW
        customerSpinner.setEnabled(false); // Read-only customer

        // Setup Adapter with Return Mode = true
        selectedProductsAdapter = new SelectedProductsAdapter(
                selectedProductSelections,
                null, // No scan action
                null, // No add manually action
                this,
                false, // isPurchase (false for sale return)
                true // isReturn (true)
        );
        selectedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedProductsRecyclerView.setAdapter(selectedProductsAdapter);

        // Setup Spinner
        customerSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        customerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customerSpinner.setAdapter(customerSpinnerAdapter);

        finalizeReturnButton.setOnClickListener(v -> openPaymentSheet());

        originalSaleId = getIntent().getStringExtra("SALE_ID");
        if (originalSaleId == null) {
            Toast.makeText(this, "No Sale ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOriginalSale();
    }

    private void loadOriginalSale() {
        showLoadingIndicator("Loading sale details...");

        salesCollection.document(originalSaleId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        originalSale = documentSnapshot.toObject(Sale.class);
                        if (originalSale != null) {
                            originalSale.setDocumentId(documentSnapshot.getId());
                            loadedSaleSnapshot = originalSale; // Keep reference

                            // Setup Customer Display
                            customerSpinnerAdapter.add(originalSale.getCustomerName());
                            customerSpinnerAdapter.notifyDataSetChanged();

                            populateProductsFromSale(originalSale.getItems());
                        } else {
                            hideLoadingIndicator();
                            finish();
                        }
                    } else {
                        hideLoadingIndicator();
                        Toast.makeText(this, "Sale not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingIndicator();
                    Toast.makeText(this, "Error loading sale: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateProductsFromSale(List<SaleItem> items) {
        if (items == null || items.isEmpty()) {
            hideLoadingIndicator();
            return;
        }

        selectedProductSelections.clear();

        // We need to fetch product details to get names/etc if they changed,
        // but primarily we trust the SaleItem snapshot for price at time of sale.
        // However, we need Product objects for the Adapter.

        for (SaleItem item : items) {
            // Calculate how many can be returned
            int originalQty = item.getQuantity();
            int alreadyReturned = item.getReturnedQuantity();
            int returnableQty = originalQty - alreadyReturned;

            if (returnableQty > 0) {
                Product product = new Product();
                product.setDocumentId(item.getProductId());
                product.setName(item.getProductName());
                product.setSellingPrice(item.getPricePerItem());
                product.setQuantity(0); // This isn't used for return limit logic relative to stock

                // Initial selection quantity is 0 or 1? Let's default to 0 so user explicitly
                // selects.
                // Actually adapter logic snaps to 1 if we pass > 0.
                // Let's pass 0 and modify Adapter if needed, OR pass 1 if returnable > 0.
                // If we pass 1, the total immediately reflects 1 item returned. This is usually
                // good UX.
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
            Toast.makeText(this, "This sale has been fully returned previously.", Toast.LENGTH_LONG).show();
            // Optional: finish() or let them see
        }
    }

    private void calculateAndDisplaySubtotal() {
        double subtotal = 0.0;
        for (ProductSelection selection : selectedProductSelections) {
            // Price determines REFUND amount
            subtotal += selection.getProduct().getSellingPrice() * selection.getQuantityInSale();
        }
        totalAmountTextView.setText(String.format("Refund Total: %.2f", subtotal));
    }

    private void openPaymentSheet() {
        if (selectedProductSelections.isEmpty()) {
            Toast.makeText(this, "No items to return.", Toast.LENGTH_SHORT).show();
            return;
        }
        double refundAmount = 0.0;
        for (ProductSelection selection : selectedProductSelections) {
            refundAmount += selection.getProduct().getSellingPrice() * selection.getQuantityInSale();
        }

        if (refundAmount <= 0) {
            Toast.makeText(this, "Refund amount must be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reuse PaymentSheet for Refund
        // We pre-fill "Amount Paid" with the full refund amount since usually returns
        // are immediate full payouts
        PaymentSheet paymentSheet = PaymentSheet.newInstance(
                new ArrayList<>(), // No formatted product list needed for sheet logic
                selectedProductSelections, // Selections
                refundAmount,
                0, 0, 0, 0, // No tax/discount on refund usually? Or should we calculate proportional?
                // For simplicity, we just refund the Item Value. Tax/Discount handling on
                // returns is complex.
                // Given the prompt "simple", we will assume refunding the Item Price is
                // sufficient.
                "Cash",
                refundAmount, // Default to full refund
                "",
                "Status: Completed",
                "Payment: Refunded",
                "Sale Return");
        paymentSheet.show(getSupportFragmentManager(), "PaymentSheet_Return");
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
            // 1. Re-read the Sale to ensure up-to-date returnedQuantities (Concurrency
            // Safety)
            DocumentReference saleRef = salesCollection.document(originalSaleId);
            Sale freshSale = transaction.get(saleRef).toObject(Sale.class);
            if (freshSale == null) {
                throw new ArithmeticException("Sale not found.");
            }
            freshSale.setDocumentId(originalSaleId);

            // 2. Validate quantities again
            List<SaleReturnItem> returnItems = new ArrayList<>();
            List<SaleItem> freshItems = freshSale.getItems();

            double refundTotal = 0;
            double costReduction = 0;

            for (ProductSelection selection : selectedProductSelections) {
                if (selection.getQuantityInSale() <= 0)
                    continue;

                String pid = selection.getProduct().getDocumentId();
                // Find matching item in fresh sale
                SaleItem saleItem = null;
                for (SaleItem si : freshItems) {
                    if (si.getProductId().equals(pid)) {
                        saleItem = si;
                        break;
                    }
                }

                if (saleItem == null) {
                    throw new ArithmeticException(
                            "Item " + selection.getProduct().getName() + " not found in original sale.");
                }

                int maxReturnable = saleItem.getQuantity() - saleItem.getReturnedQuantity();
                if (selection.getQuantityInSale() > maxReturnable) {
                    throw new ArithmeticException("Cannot return " + selection.getQuantityInSale() + " of "
                            + selection.getProduct().getName() + ". Only " + maxReturnable + " remaining.");
                }

                // 3. Update SaleItem returned quantity
                saleItem.setReturnedQuantity(saleItem.getReturnedQuantity() + selection.getQuantityInSale());

                // Add to Return Doc List
                SaleReturnItem returnItem = new SaleReturnItem();
                returnItem.setProductId(pid);
                returnItem.setProductName(saleItem.getProductName());
                returnItem.setQuantity(selection.getQuantityInSale());
                returnItem.setPricePerItem(saleItem.getPricePerItem());
                returnItems.add(returnItem);

                // Use FinancialCalculator for refund and cost math
                refundTotal = FinancialCalculator.add(refundTotal,
                        FinancialCalculator.multiply(returnItem.getPricePerItem(), returnItem.getQuantity()));

                costReduction = FinancialCalculator.add(costReduction,
                        FinancialCalculator.multiply(saleItem.getCostPrice(), selection.getQuantityInSale()));

                // 4. Update Product Inventory (Increase Stock)
                DocumentReference productRef = productsCollection.document(pid);
                transaction.update(productRef, "quantity", FieldValue.increment(selection.getQuantityInSale()));
            }

            if (returnItems.isEmpty()) {
                throw new ArithmeticException("No items selected for return.");
            }

            // 5. Update Sale Document with recalibrated financial fields
            double currentTotalCost = freshSale.getTotalCost() != null ? freshSale.getTotalCost() : 0.0;
            double currentTotalProfit = freshSale.getTotalProfit() != null ? freshSale.getTotalProfit() : 0.0;

            // totalAmount in Sale usually represents the total paid by customer (including
            // tax/discount)
            // If we refund the items, we should ideally reduce the Sale's totalAmount too
            // but the prompt focused on cost/profit desync.
            freshSale.setTotalCost(FinancialCalculator.subtract(currentTotalCost, costReduction));

            // Profit reduction = Refunded Amount - Cost of Refunded Items
            double profitReduction = FinancialCalculator.subtract(refundTotal, costReduction);
            freshSale.setTotalProfit(FinancialCalculator.subtract(currentTotalProfit, profitReduction));

            // Also reduce the totalAmount to reflect the refund
            freshSale.setTotalAmount(FinancialCalculator.subtract(freshSale.getTotalAmount(), refundTotal));

            transaction.set(saleRef, freshSale); // Saves the updated items list and recalibrated fields

            // 6. Create Return Document
            DocumentReference returnRef = returnsCollection.document();
            SaleReturn saleReturn = new SaleReturn();
            saleReturn.setDocumentId(returnRef.getId());
            saleReturn.setOriginalSaleId(originalSaleId);
            saleReturn.setCustomerId(freshSale.getCustomerId());
            saleReturn.setCustomerName(freshSale.getCustomerName());
            saleReturn.setReturnDate(Timestamp.now());
            saleReturn.setUserId(FirebaseAuth.getInstance().getUid());
            saleReturn.setItems(returnItems);
            saleReturn.setTotalRefundAmount(refundTotal);

            transaction.set(returnRef, saleReturn);

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
        // Just set quantity to 0 or remove from list?
        // If we remove from list, we can't add it back because "Add Manually" is
        // hidden.
        // So better to just set quantity to 0.
        // Or actually, remove it, and user checks logic?
        // The adapter supports removing. If removed, it's ignoring that item for
        // return.
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
        // Price should be read-only in return mode ideally, but if changed, it affects
        // refund total
        calculateAndDisplaySubtotal();
    }
}
