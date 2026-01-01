package com.bsoft.inventorymanager.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.adapters.SelectedProductsAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import com.bsoft.inventorymanager.models.ProductSelection;
import java.util.List;
import java.util.Locale;

public class PaymentSheet extends BottomSheetDialogFragment {

    public interface PaymentSheetListener {
        void onTransactionFinalized(List<ProductSelection> updatedProductSelections,
                double taxAmount, double discountAmount, String paymentMethod, double amountPaid, String notes);

        void onPaymentDraftChanged(com.bsoft.inventorymanager.models.PaymentDraft draft);
    }

    private PaymentSheetListener mListener;

    private static final String ARG_PRODUCTS = "selected_products";
    private static final String ARG_PRODUCT_SELECTIONS = "product_selections";
    private static final String ARG_SUBTOTAL = "subtotal";
    private static final String ARG_TAX_PERCENT = "tax_percent";
    private static final String ARG_TAX_AMOUNT = "tax_amount";
    private static final String ARG_DISCOUNT_PERCENT = "discount_percent";
    private static final String ARG_DISCOUNT_AMOUNT = "discount_amount";
    private static final String ARG_PAYMENT_METHOD = "payment_method";
    private static final String ARG_AMOUNT_PAID = "amount_paid";
    private static final String ARG_NOTES = "notes";
    private static final String ARG_PURCHASE_STATUS = "purchase_status";
    private static final String ARG_PAYMENT_STATUS = "payment_status";
    private static final String ARG_TRANSACTION_TYPE = "transaction_type"; // "Sale" or "Purchase"

    private List<Product> selectedProductsList;
    private List<ProductSelection> selectedProductSelections;
    private double subtotal;
    private double taxPercent;
    private double taxAmount;
    private double discountPercent;
    private double discountAmount;
    private String paymentMethod;
    private double amountPaid;
    private String notes;
    private String purchaseStatus;
    private String paymentStatus;
    private String transactionType = "Transaction"; // Default

    private Spinner paymentMethodSpinner;
    private Button finalizePurchaseButton;
    private ProgressBar progressBarHold;
    private TextView progressText;
    private TextInputLayout tilAmountPaid, tilTaxPercentage, tilTaxAmount, tilDiscountPercentage, tilDiscountAmount,
            tilNotes;
    private TextInputEditText etAmountPaid, etTaxPercentage, etTaxAmount, etDiscountPercentage, etDiscountAmount,
            etNotes;
    private TextView tvAmountDue, tvPurchaseStatus, tvPaymentStatus, tvTotalAmount;
    private ArrayAdapter<String> paymentMethodAdapter;

    private Handler handler;
    private Runnable holdRunnable;
    private boolean isCalculatingTax = false;
    private boolean isCalculatingDiscount = false;

    public static PaymentSheet newInstance(List<Product> selectedProductsList,
            List<ProductSelection> selectedProductSelections,
            double subtotal,
            double taxPercent,
            double taxAmount,
            double discountPercent,
            double discountAmount,
            String paymentMethod,
            double amountPaid,
            String notes,
            String purchaseStatus,
            String paymentStatus,
            String transactionType) {
        PaymentSheet fragment = new PaymentSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCTS, (java.io.Serializable) selectedProductsList);
        args.putSerializable(ARG_PRODUCT_SELECTIONS, (java.io.Serializable) selectedProductSelections);
        args.putDouble(ARG_SUBTOTAL, subtotal);
        args.putDouble(ARG_TAX_PERCENT, taxPercent);
        args.putDouble(ARG_TAX_AMOUNT, taxAmount);
        args.putDouble(ARG_DISCOUNT_PERCENT, discountPercent);
        args.putDouble(ARG_DISCOUNT_AMOUNT, discountAmount);
        args.putString(ARG_PAYMENT_METHOD, paymentMethod);
        args.putDouble(ARG_AMOUNT_PAID, amountPaid);
        args.putString(ARG_NOTES, notes);
        args.putString(ARG_PURCHASE_STATUS, purchaseStatus);
        args.putString(ARG_PAYMENT_STATUS, paymentStatus);
        args.putString(ARG_TRANSACTION_TYPE, transactionType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PaymentSheetListener) {
            mListener = (PaymentSheetListener) context;
        } else {
            throw new RuntimeException(context
                    + " must implement PaymentSheetListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedProductsList = (List<Product>) getArguments().getSerializable(ARG_PRODUCTS);
            selectedProductSelections = (List<ProductSelection>) getArguments()
                    .getSerializable(ARG_PRODUCT_SELECTIONS);
            subtotal = getArguments().getDouble(ARG_SUBTOTAL);
            taxPercent = getArguments().getDouble(ARG_TAX_PERCENT);
            taxAmount = getArguments().getDouble(ARG_TAX_AMOUNT);
            discountPercent = getArguments().getDouble(ARG_DISCOUNT_PERCENT);
            discountAmount = getArguments().getDouble(ARG_DISCOUNT_AMOUNT);
            paymentMethod = getArguments().getString(ARG_PAYMENT_METHOD);
            amountPaid = getArguments().getDouble(ARG_AMOUNT_PAID);
            notes = getArguments().getString(ARG_NOTES);
            purchaseStatus = getArguments().getString(ARG_PURCHASE_STATUS);
            paymentStatus = getArguments().getString(ARG_PAYMENT_STATUS);
            transactionType = getArguments().getString(ARG_TRANSACTION_TYPE, "Transaction");
        }
        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_payment_sheet, container, false);
    }

    private void notifyDraftChanged() {
        if (mListener != null) {
            double taxP = 0, taxA = 0, discP = 0, discA = 0, paid = 0;
            try {
                taxP = Double.parseDouble(etTaxPercentage.getText().toString().trim());
            } catch (Exception ignored) {
            }
            try {
                taxA = Double.parseDouble(etTaxAmount.getText().toString().trim());
            } catch (Exception ignored) {
            }
            try {
                discP = Double.parseDouble(etDiscountPercentage.getText().toString().trim());
            } catch (Exception ignored) {
            }
            try {
                discA = Double.parseDouble(etDiscountAmount.getText().toString().trim());
            } catch (Exception ignored) {
            }
            try {
                paid = Double.parseDouble(etAmountPaid.getText().toString().trim());
            } catch (Exception ignored) {
            }
            String method = paymentMethodSpinner.getSelectedItem() != null
                    ? paymentMethodSpinner.getSelectedItem().toString()
                    : null;
            String n = etNotes.getText() != null ? etNotes.getText().toString() : null;
            com.bsoft.inventorymanager.models.PaymentDraft draft = new com.bsoft.inventorymanager.models.PaymentDraft(
                    taxP, taxA, discP, discA, method, paid, n);
            mListener.onPaymentDraftChanged(draft);
        }
    }

    private void setupDraftListeners() {
        etTaxPercentage.addTextChangedListener(new SimpleAfterTextWatcher(() -> notifyDraftChanged()));
        etTaxAmount.addTextChangedListener(new SimpleAfterTextWatcher(() -> notifyDraftChanged()));
        etDiscountPercentage.addTextChangedListener(new SimpleAfterTextWatcher(() -> notifyDraftChanged()));
        etDiscountAmount.addTextChangedListener(new SimpleAfterTextWatcher(() -> notifyDraftChanged()));
        etAmountPaid.addTextChangedListener(new SimpleAfterTextWatcher(() -> notifyDraftChanged()));
        etNotes.addTextChangedListener(new SimpleAfterTextWatcher(() -> notifyDraftChanged()));
    }

    private static class SimpleAfterTextWatcher implements android.text.TextWatcher {
        private final Runnable r;

        SimpleAfterTextWatcher(Runnable r) {
            this.r = r;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
            r.run();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupPaymentMethodSpinner();
        setupTextWatchers();
        populateFields();
        setupLongPressButton();
        setupDraftListeners();

        // Ensure Notes scrolls into view when focused
        View root = getView();
        if (root instanceof androidx.core.widget.NestedScrollView) {
            androidx.core.widget.NestedScrollView scrollView = (androidx.core.widget.NestedScrollView) root;
            etNotes.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, v.getBottom()));
                }
            });
            etAmountPaid.setOnEditorActionListener((tv, actionId, event) -> {
                // Move to notes and scroll when pressing Next on amountPaid
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                    etNotes.requestFocus();
                    scrollView.post(() -> scrollView.smoothScrollTo(0, etNotes.getBottom()));
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        // Expand the bottom sheet so the keyboard resize keeps content visible
        if (getDialog() instanceof com.google.android.material.bottomsheet.BottomSheetDialog) {
            com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) getDialog();
            com.google.android.material.bottomsheet.BottomSheetBehavior<?> behavior = d.getBehavior();
            behavior.setFitToContents(true);
            behavior.setDraggable(true);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        }
        View root = getView();
        if (root != null) {
            root.setFitsSystemWindows(true);
        }
    }

    private void initializeViews(View view) {
        paymentMethodSpinner = view.findViewById(R.id.spinner_payment_method_sheet);
        finalizePurchaseButton = view.findViewById(R.id.button_finalize_purchase_sheet);
        progressBarHold = view.findViewById(R.id.progress_bar_hold);
        progressText = view.findViewById(R.id.tv_progress_text);

        tilAmountPaid = view.findViewById(R.id.til_amount_paid_sheet);
        tilTaxPercentage = view.findViewById(R.id.til_tax_percentage_sheet);
        tilTaxAmount = view.findViewById(R.id.til_tax_amount_sheet);
        tilDiscountPercentage = view.findViewById(R.id.til_discount_percentage_sheet);
        tilDiscountAmount = view.findViewById(R.id.til_discount_amount_sheet);
        tilNotes = view.findViewById(R.id.til_notes_sheet);

        etAmountPaid = view.findViewById(R.id.et_amount_paid_sheet);
        etTaxPercentage = view.findViewById(R.id.et_tax_percentage_sheet);
        etTaxAmount = view.findViewById(R.id.et_tax_amount_sheet);
        etDiscountPercentage = view.findViewById(R.id.et_discount_percentage_sheet);
        etDiscountAmount = view.findViewById(R.id.et_discount_amount_sheet);
        etNotes = view.findViewById(R.id.et_notes_sheet);

        tvAmountDue = view.findViewById(R.id.tv_amount_due_sheet);
        tvPurchaseStatus = view.findViewById(R.id.tv_purchase_status_sheet);
        tvPaymentStatus = view.findViewById(R.id.tv_payment_status_sheet);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount_purchase);
    }

    private void setupPaymentMethodSpinner() {
        paymentMethodAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                new String[] { "Cash", "Credit", "Bank Transfer", "Mobile Banking", "Check" });
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(paymentMethodAdapter);
    }

    private com.bsoft.inventorymanager.utils.Debouncer debouncer = new com.bsoft.inventorymanager.utils.Debouncer(300);

    private void setupTextWatchers() {
        // Set up tax percentage text watcher
        etTaxPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isCalculatingTax)
                    return; // Prevent infinite loop

                debouncer.debounce(() -> {
                    String taxPercentStr = s.toString().trim();
                    if (!taxPercentStr.isEmpty()) {
                        try {
                            double taxPercent = Double.parseDouble(taxPercentStr);
                            if (taxPercent >= 0) {
                                double taxAmount = (subtotal * taxPercent) / 100;
                                isCalculatingTax = true;
                                etTaxAmount.setText(String.format(Locale.US, "%.2f", taxAmount));
                                isCalculatingTax = false;
                                updateAmountDue();
                            }
                        } catch (NumberFormatException e) {
                            // Handle invalid input
                        }
                    } else {
                        etTaxAmount.setText("");
                        updateAmountDue();
                    }
                });
            }
        });

        // Set up tax amount text watcher
        etTaxAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isCalculatingTax)
                    return; // Prevent infinite loop

                debouncer.debounce(() -> {
                    String taxAmountStr = s.toString().trim();
                    if (!taxAmountStr.isEmpty()) {
                        try {
                            double taxAmount = Double.parseDouble(taxAmountStr);
                            if (taxAmount >= 0) {
                                if (subtotal > 0) {
                                    double taxPercent = (taxAmount / subtotal) * 100;
                                    isCalculatingTax = true;
                                    etTaxPercentage.setText(String.format(Locale.US, "%.2f", taxPercent));
                                    isCalculatingTax = false;
                                }
                                updateAmountDue();
                            }
                        } catch (NumberFormatException e) {
                            // Handle invalid input
                        }
                    } else {
                        etTaxPercentage.setText("");
                        updateAmountDue();
                    }
                });
            }
        });

        // Set up discount percentage text watcher
        etDiscountPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isCalculatingDiscount)
                    return; // Prevent infinite loop

                debouncer.debounce(() -> {
                    String discountPercentStr = s.toString().trim();
                    if (!discountPercentStr.isEmpty()) {
                        try {
                            double discountPercent = Double.parseDouble(discountPercentStr);
                            if (discountPercent >= 0) {
                                double discountAmount = (subtotal * discountPercent) / 100;
                                isCalculatingDiscount = true;
                                etDiscountAmount.setText(String.format(Locale.US, "%.2f", discountAmount));
                                isCalculatingDiscount = false;
                                updateAmountDue();
                            }
                        } catch (NumberFormatException e) {
                            // Handle invalid input
                        }
                    } else {
                        etDiscountAmount.setText("");
                        updateAmountDue();
                    }
                });
            }
        });

        // Set up discount amount text watcher
        etDiscountAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isCalculatingDiscount)
                    return; // Prevent infinite loop

                debouncer.debounce(() -> {
                    String discountAmountStr = s.toString().trim();
                    if (!discountAmountStr.isEmpty()) {
                        try {
                            double discountAmount = Double.parseDouble(discountAmountStr);
                            if (discountAmount >= 0) {
                                if (subtotal > 0) {
                                    double discountPercent = (discountAmount / subtotal) * 100;
                                    isCalculatingDiscount = true;
                                    etDiscountPercentage.setText(String.format(Locale.US, "%.2f", discountPercent));
                                    isCalculatingDiscount = false;
                                }
                                updateAmountDue();
                            }
                        } catch (NumberFormatException e) {
                            // Handle invalid input
                        }
                    } else {
                        etDiscountPercentage.setText("");
                        updateAmountDue();
                    }
                });
            }
        });

        // Set up amount paid text watcher
        etAmountPaid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                debouncer.debounce(() -> updateAmountDue());
            }
        });
    }

    private void populateFields() {
        // Set initial values
        if (taxPercent > 0)
            etTaxPercentage.setText(String.format(Locale.US, "%.2f", taxPercent));
        if (taxAmount > 0)
            etTaxAmount.setText(String.format(Locale.US, "%.2f", taxAmount));
        if (discountPercent > 0)
            etDiscountPercentage.setText(String.format(Locale.US, "%.2f", discountPercent));
        if (discountAmount > 0)
            etDiscountAmount.setText(String.format(Locale.US, "%.2f", discountAmount));
        if (amountPaid > 0)
            etAmountPaid.setText(String.format(Locale.US, "%.2f", amountPaid));
        if (notes != null && !notes.isEmpty())
            etNotes.setText(notes);

        // Set payment method
        if (paymentMethod != null) {
            int spinnerPosition = paymentMethodAdapter.getPosition(paymentMethod);
            if (spinnerPosition >= 0) {
                paymentMethodSpinner.setSelection(spinnerPosition);
            }
        }

        // Update status displays
        tvPurchaseStatus.setText(purchaseStatus);
        tvPaymentStatus.setText(paymentStatus);
        updateAmountDue();
    }

    private void updateAmountDue() {
        // Get tax amount
        double taxAmount = 0.0;
        try {
            String taxAmountStr = etTaxAmount.getText().toString().trim();
            if (!taxAmountStr.isEmpty()) {
                taxAmount = Double.parseDouble(taxAmountStr);
            }
        } catch (NumberFormatException e) {
            // Handle invalid input
        }

        // Get discount amount
        double discountAmount = 0.0;
        try {
            String discountAmountStr = etDiscountAmount.getText().toString().trim();
            if (!discountAmountStr.isEmpty()) {
                discountAmount = Double.parseDouble(discountAmountStr);
            }
        } catch (NumberFormatException e) {
            // Handle invalid input
        }

        // Calculate total: subtotal + tax - discount
        double totalAmount = subtotal + taxAmount - discountAmount;
        tvTotalAmount.setText(String.format("Total: %.2f", totalAmount));

        double amountPaid = 0.0;
        try {
            String amountPaidStr = etAmountPaid.getText().toString().trim();
            if (!amountPaidStr.isEmpty()) {
                amountPaid = Double.parseDouble(amountPaidStr);
                if (tilAmountPaid.getError() != null && (amountPaid >= 0 && amountPaid <= totalAmount)) {
                    tilAmountPaid.setError(null);
                }
            }
        } catch (NumberFormatException e) {
        }

        if (amountPaid < 0) {
            tilAmountPaid.setError("Amount cannot be negative");
        } else if (amountPaid > totalAmount) {
            tilAmountPaid.setError("Cannot exceed total");
        } else {
            tilAmountPaid.setError(null);
        }

        double amountDue = totalAmount - amountPaid;
        if (amountDue < 0)
            amountDue = 0;

        // Update payment status
        updatePaymentStatus(amountPaid, totalAmount);

        Locale bdtLocale = new Locale("bn", "BD");
        NumberFormat bdtFormat = NumberFormat.getCurrencyInstance(bdtLocale);
        String dueText = String.format("Amount Due: %s", bdtFormat.format(amountDue));
        tvAmountDue.setText(dueText);
    }

    private void updatePaymentStatus(double amountPaid, double totalAmount) {
        if (totalAmount == 0) {
            tvPaymentStatus.setText("Payment: N/A");
            DrawableCompat.setTint(tvPaymentStatus.getBackground(),
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            return;
        }

        double paymentPercentage = (amountPaid / totalAmount) * 100;

        if (amountPaid >= totalAmount) {
            tvPaymentStatus.setText("Payment: Paid");
            DrawableCompat.setTint(tvPaymentStatus.getBackground(),
                    ContextCompat.getColor(requireContext(), R.color.colorSuccess));
        } else if (amountPaid > 0 && amountPaid < totalAmount) {
            tvPaymentStatus.setText(String.format("Payment: Partial (%.1f%%)", paymentPercentage));
            DrawableCompat.setTint(tvPaymentStatus.getBackground(),
                    ContextCompat.getColor(requireContext(), R.color.colorWarning));
        } else {
            tvPaymentStatus.setText("Payment: Pending");
            DrawableCompat.setTint(tvPaymentStatus.getBackground(),
                    ContextCompat.getColor(requireContext(), R.color.colorSecondary));
        }
    }

    private void setupLongPressButton() {
        holdRunnable = new Runnable() {
            @Override
            public void run() {
                int progress = progressBarHold.getProgress();
                if (progress < 100) {
                    progress += 5; // Increment progress
                    progressBarHold.setProgress(progress);
                    progressText.setText(String.format("Hold for %d%%", progress));
                    handler.postDelayed(holdRunnable, 150); // Continue holding
                } else {
                    // Hold completed
                    vibrate(500);
                    Toast.makeText(getContext(), transactionType + " Finalized!", Toast.LENGTH_SHORT).show();

                    if (mListener != null) {
                        double taxAmount = 0, discountAmount = 0, amountPaid = 0;
                        try {
                            taxAmount = Double.parseDouble(etTaxAmount.getText().toString());
                        } catch (NumberFormatException e) {
                        }
                        try {
                            discountAmount = Double.parseDouble(etDiscountAmount.getText().toString());
                        } catch (NumberFormatException e) {
                        }
                        try {
                            amountPaid = Double.parseDouble(etAmountPaid.getText().toString());
                        } catch (NumberFormatException e) {
                        }

                        mListener.onTransactionFinalized(
                                selectedProductSelections,
                                taxAmount,
                                discountAmount,
                                paymentMethodSpinner.getSelectedItem().toString(),
                                amountPaid,
                                etNotes.getText().toString());
                    }
                    dismiss(); // Close the sheet
                }
            }
        };

        finalizePurchaseButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Start the progress
                    progressBarHold.setVisibility(View.VISIBLE);
                    progressText.setVisibility(View.VISIBLE);
                    progressBarHold.setProgress(0);
                    progressText.setText("Hold for 0%");
                    handler.postDelayed(holdRunnable, 150);
                    vibrate(50); // Small vibration to confirm touch
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Stop the progress
                    handler.removeCallbacks(holdRunnable);
                    if (progressBarHold.getProgress() > 0 && progressBarHold.getProgress() < 100) {
                        // User released before completion
                        progressBarHold.setProgress(0);
                        progressText.setText("Press and hold the button to finalize " + transactionType.toLowerCase());
                        vibrate(100); // Vibrate to indicate cancellation
                    }
                    progressBarHold.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                    return true;
            }
            return false;
        });
    }

    private void vibrate(long duration) {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(duration);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
