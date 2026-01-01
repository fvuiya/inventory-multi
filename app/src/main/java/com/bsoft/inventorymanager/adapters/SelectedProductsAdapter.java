package com.bsoft.inventorymanager.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.ProductSelection;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SelectedProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecyclerView recyclerView;

    private static final String TAG = "SelectedProductsAdapter";
    private static final int VIEW_TYPE_PRODUCT = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    private final List<ProductSelection> productSelections;
    private final Runnable scanProductAction;
    private final Runnable addManuallyAction;
    private final OnProductInteractionListener mListener;
    private final boolean isPurchase;
    private final boolean isReturn;

    public interface OnProductInteractionListener {
        void onRemoveProductClicked(int position);

        void onQuantityChanged(int position, int newQuantity, double unitPrice);

        void onPriceChanged(int position, double newPrice);
    }

    public SelectedProductsAdapter(List<ProductSelection> productSelections,
            Runnable scanProductAction,
            Runnable addManuallyAction,
            OnProductInteractionListener listener,
            boolean isPurchase) {
        this(productSelections, scanProductAction, addManuallyAction, listener, isPurchase, false);
    }

    public SelectedProductsAdapter(List<ProductSelection> productSelections,
            Runnable scanProductAction,
            Runnable addManuallyAction,
            OnProductInteractionListener listener,
            boolean isPurchase,
            boolean isReturn) {
        this.productSelections = productSelections != null ? productSelections : new ArrayList<>();
        this.scanProductAction = scanProductAction;
        this.addManuallyAction = addManuallyAction;
        this.mListener = listener;
        this.isPurchase = isPurchase;
        this.isReturn = isReturn;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == productSelections.size()) {
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_PRODUCT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_PRODUCT) {
            View view = inflater.inflate(R.layout.item_selected_product, parent, false);
            return new ProductViewHolder(view);
        } else { // VIEW_TYPE_FOOTER
            View view = inflater.inflate(R.layout.layout_sale_actions_footer, parent, false);
            return new FooterViewHolder(view);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_PRODUCT) {
            ProductViewHolder productViewHolder = (ProductViewHolder) holder;
            if (position >= productSelections.size()) {
                return;
            }
            ProductSelection selection = productSelections.get(position);
            Product product = selection.getProduct();

            productViewHolder.textViewProductName.setText(product != null ? product.getName() : "N/A");

            double price = isPurchase ? product.getPurchasePrice() : product.getSellingPrice();
            productViewHolder.textViewProductPrice.setText(String.format("Price: %.2f", price));

            productViewHolder.editTextQuantity.setText(String.valueOf(selection.getQuantityInSale()));

            if (productViewHolder.quantityTextWatcher != null) {
                productViewHolder.editTextQuantity.removeTextChangedListener(productViewHolder.quantityTextWatcher);
            }
            productViewHolder.editTextQuantity.setOnFocusChangeListener(null);

            productViewHolder.quantityTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int currentPosition = productViewHolder.getAdapterPosition();
                    if (currentPosition == RecyclerView.NO_POSITION || currentPosition >= productSelections.size()) {
                        return;
                    }

                    Product currentProduct = productSelections.get(currentPosition).getProduct();
                    if (currentProduct == null)
                        return;

                    String quantityStr = s.toString().trim();
                    if (!quantityStr.isEmpty()) {
                        try {
                            int typedQuantity = Integer.parseInt(quantityStr);
                            int finalQuantity = typedQuantity;

                            // Stock Auto-Correction Logic
                            int limit = Integer.MAX_VALUE;
                            String errorMsg = "";

                            if (isReturn) {
                                limit = productSelections.get(currentPosition).getMaxReturnableQuantity();
                                if (limit == -1)
                                    limit = Integer.MAX_VALUE; // Fallback
                                errorMsg = "Max Return: " + limit;
                            } else if (!isPurchase) {
                                limit = currentProduct.getQuantity();
                                errorMsg = "Exceeds stock (" + limit + ")";
                            }

                            if (typedQuantity > limit) {
                                finalQuantity = limit;

                                // Determine the cursor position - usually strict end or keep current?
                                // Snapping to max usually implies "fixing" the input, so cursor at end is
                                // standard.

                                // Prevent recursive loop
                                productViewHolder.editTextQuantity.removeTextChangedListener(this);

                                productViewHolder.editTextQuantity.setText(String.valueOf(finalQuantity));
                                productViewHolder.editTextQuantity
                                        .setSelection(productViewHolder.editTextQuantity.getText().length());

                                // Re-add listener
                                productViewHolder.editTextQuantity.addTextChangedListener(this);

                                // Show error to indicate value was capped
                                productViewHolder.editTextQuantity.setError(errorMsg);
                            } else {
                                productViewHolder.editTextQuantity.setError(null);
                            }

                            double currentPrice = isPurchase ? currentProduct.getPurchasePrice()
                                    : currentProduct.getSellingPrice();
                            double itemTotal = currentPrice * finalQuantity;
                            productViewHolder.textViewItemTotalAmount
                                    .setText(String.format(Locale.getDefault(), "%.2f", itemTotal));
                            if (mListener != null) {
                                // Provide the corrected quantity to the listener
                                mListener.onQuantityChanged(currentPosition, finalQuantity, currentPrice);
                            }
                        } catch (NumberFormatException e) {
                            productViewHolder.textViewItemTotalAmount.setText("0.00");
                            if (mListener != null) {
                                ProductSelection currentSel = productSelections.get(currentPosition);
                                double currentPrice = isPurchase ? currentProduct.getPurchasePrice()
                                        : currentProduct.getSellingPrice();
                                mListener.onQuantityChanged(currentPosition, currentSel.getQuantityInSale(),
                                        currentPrice);
                            }
                        }
                    } else {
                        // Empty string case
                        productViewHolder.textViewItemTotalAmount.setText("0.00");
                        if (mListener != null) {
                            ProductSelection currentSel = productSelections.get(currentPosition);
                            double currentPrice = isPurchase ? currentProduct.getPurchasePrice()
                                    : currentProduct.getSellingPrice();
                            mListener.onQuantityChanged(currentPosition, currentSel.getQuantityInSale(), currentPrice);
                        }
                    }
                }
            };
            productViewHolder.editTextQuantity.addTextChangedListener(productViewHolder.quantityTextWatcher);

            productViewHolder.editTextQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    int currentPosition = productViewHolder.getAdapterPosition();
                    if (currentPosition == RecyclerView.NO_POSITION || currentPosition >= productSelections.size()) {
                        return;
                    }

                    ProductSelection currentSelection = productSelections.get(currentPosition);
                    Product currentProduct = currentSelection.getProduct();
                    if (currentProduct == null) {
                        return;
                    }

                    String quantityStr = productViewHolder.editTextQuantity.getText().toString().trim();
                    int newQuantity = 1;
                    boolean isValidInput = true;

                    if (!quantityStr.isEmpty()) {
                        try {
                            newQuantity = Integer.parseInt(quantityStr);
                        } catch (NumberFormatException e) {
                            productViewHolder.editTextQuantity.setError("Invalid number");
                            isValidInput = false;
                            productViewHolder.editTextQuantity
                                    .setText(String.valueOf(currentSelection.getQuantityInSale()));
                            productViewHolder.editTextQuantity.selectAll();
                        }
                    }

                    if (isValidInput) {
                        int limit = Integer.MAX_VALUE;
                        String errorMsg = "";

                        if (isReturn) {
                            limit = currentSelection.getMaxReturnableQuantity();
                            if (limit == -1)
                                limit = Integer.MAX_VALUE;
                            errorMsg = "Max Return: " + limit;
                        } else if (!isPurchase) {
                            limit = currentProduct.getQuantity();
                            errorMsg = "Exceeds stock (" + limit + ")";
                        }

                        if (newQuantity > limit) {
                            productViewHolder.editTextQuantity.setError(errorMsg);
                            productViewHolder.editTextQuantity
                                    .setText(String.valueOf(currentSelection.getQuantityInSale()));
                            productViewHolder.editTextQuantity.selectAll();
                        } else if (newQuantity <= 0) {
                            currentSelection.setQuantityInSale(1);
                            productViewHolder.editTextQuantity.setText("1");
                            double currentPrice = isPurchase ? currentProduct.getPurchasePrice()
                                    : currentProduct.getSellingPrice();
                            double resetItemTotal = currentPrice * 1;
                            productViewHolder.textViewItemTotalAmount
                                    .setText(String.format(Locale.getDefault(), "%.2f", resetItemTotal));
                            productViewHolder.editTextQuantity.setError(null);
                            if (mListener != null) {
                                mListener.onQuantityChanged(currentPosition, 1, currentPrice);
                            }
                        } else {
                            currentSelection.setQuantityInSale(newQuantity);
                            double currentPrice = isPurchase ? currentProduct.getPurchasePrice()
                                    : currentProduct.getSellingPrice();
                            double newItemTotal = currentPrice * newQuantity;
                            productViewHolder.textViewItemTotalAmount
                                    .setText(String.format(Locale.getDefault(), "%.2f", newItemTotal));
                            productViewHolder.editTextQuantity.setError(null);
                            if (mListener != null) {
                                mListener.onQuantityChanged(currentPosition, newQuantity, currentPrice);
                            }
                        }
                    }
                }
            });

            productViewHolder.editTextQuantity.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    productViewHolder.editTextQuantity.clearFocus();
                    return true;
                }
                return false;
            });

            double initialItemTotal = price * selection.getQuantityInSale();
            productViewHolder.textViewItemTotalAmount
                    .setText(String.format(Locale.getDefault(), "%.2f", initialItemTotal));

            productViewHolder.buttonRemoveProduct.setOnClickListener(v -> {
                int currentPosition = productViewHolder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < productSelections.size()) {
                    productViewHolder.itemView.post(() -> {
                        if (mListener != null) {
                            mListener.onRemoveProductClicked(currentPosition);
                        }
                    });
                }
            });

            // Hide remove button in return mode if strictly required, but usually user
            // might want to return 0 for an item (i.e. remove it from list)
            // For now, kept enabled.

        } else if (holder.getItemViewType() == VIEW_TYPE_FOOTER) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;

            // Hide footer actions in Return Mode as we only return what was in the original
            // transaction
            if (isReturn) {
                footerViewHolder.itemView.setVisibility(View.GONE);
                footerViewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            } else {
                footerViewHolder.itemView.setVisibility(View.VISIBLE);
                footerViewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            footerViewHolder.buttonScanProductFooter.setOnClickListener(v -> {
                if (scanProductAction != null) {
                    scanProductAction.run();
                }
            });
            footerViewHolder.buttonAddManuallyFooter.setOnClickListener(v -> {
                if (addManuallyAction != null) {
                    addManuallyAction.run();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return productSelections.size() + 1;
    }

    public void addProduct(Product product) {
        if (product == null) {
            return;
        }
        ProductSelection newSelection = new ProductSelection(product, 1);
        int existingPosition = -1;
        for (int i = 0; i < productSelections.size(); i++) {
            if (productSelections.get(i).getProduct() != null &&
                    productSelections.get(i).getProduct().getDocumentId().equals(product.getDocumentId())) {
                existingPosition = i;
                break;
            }
        }

        if (existingPosition != -1) {
            ProductSelection existing = productSelections.get(existingPosition);
            int newQuantity = existing.getQuantityInSale() + 1;
            if (!isPurchase && newQuantity > existing.getProduct().getQuantity()) {
                Log.d(TAG, "Cannot increment quantity for " + product.getName() + ", exceeds stock.");
                notifyItemChanged(existingPosition);
            } else {
                existing.setQuantityInSale(newQuantity);
                notifyItemChanged(existingPosition);
                if (mListener != null) {
                    double price = isPurchase ? existing.getProduct().getPurchasePrice()
                            : existing.getProduct().getSellingPrice();
                    mListener.onQuantityChanged(existingPosition, newQuantity, price);
                }
            }
        } else {
            productSelections.add(newSelection);
            notifyItemInserted(productSelections.size() - 1);
            if (mListener != null) {
                double price = isPurchase ? newSelection.getProduct().getPurchasePrice()
                        : newSelection.getProduct().getSellingPrice();
                mListener.onQuantityChanged(productSelections.size() - 1, 1, price);
            }
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < productSelections.size()) {
            productSelections.remove(position);
            notifyItemRemoved(position);
            if (position < productSelections.size()) {
                notifyItemRangeChanged(position, productSelections.size() - position);
            }
        }
    }

    public List<ProductSelection> getSelectedProductsWithQuantities() {
        return productSelections;
    }

    public void updateList(List<ProductSelection> newSelections) {
        // Use DiffUtil in future for better performance, but for now simple swap
        this.productSelections.clear();
        if (newSelections != null) {
            this.productSelections.addAll(newSelections);
        }
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName, textViewProductPrice;
        EditText editTextQuantity;
        ImageButton buttonRemoveProduct;
        TextView textViewItemTotalAmount;
        TextWatcher quantityTextWatcher;

        ProductViewHolder(View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textView_product_name);
            textViewProductPrice = itemView.findViewById(R.id.textView_product_price);
            editTextQuantity = itemView.findViewById(R.id.editText_quantity);
            buttonRemoveProduct = itemView.findViewById(R.id.button_remove_product);
            textViewItemTotalAmount = itemView.findViewById(R.id.textView_item_total_amount);
            editTextQuantity.setFocusableInTouchMode(true);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        Button buttonScanProductFooter;
        Button buttonAddManuallyFooter;

        FooterViewHolder(View itemView) {
            super(itemView);
            buttonScanProductFooter = itemView.findViewById(R.id.button_scan_product_footer);
            buttonAddManuallyFooter = itemView.findViewById(R.id.button_add_manually_footer);
        }
    }
}
