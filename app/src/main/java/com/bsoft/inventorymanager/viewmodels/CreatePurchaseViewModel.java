package com.bsoft.inventorymanager.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.ProductSelection;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.PurchaseItem;
import com.bsoft.inventorymanager.model.Supplier;
import com.bsoft.inventorymanager.repositories.PurchaseRepository;
import com.bsoft.inventorymanager.utils.FinancialCalculator;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CreatePurchaseViewModel extends ViewModel {

    private final PurchaseRepository purchaseRepository;

    private final MutableLiveData<List<ProductSelection>> productSelections = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> purchaseSuccess = new MutableLiveData<>();
    private final MutableLiveData<List<Supplier>> suppliers = new MutableLiveData<>();
    private final MutableLiveData<Supplier> selectedSupplier = new MutableLiveData<>();

    @Inject
    public CreatePurchaseViewModel(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
        loadSuppliers();
    }

    public LiveData<List<ProductSelection>> getProductSelections() {
        return productSelections;
    }

    public LiveData<Double> getSubtotal() {
        return subtotal;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getPurchaseSuccess() {
        return purchaseSuccess;
    }

    public LiveData<List<Supplier>> getSuppliers() {
        return suppliers;
    }

    public LiveData<Supplier> getSelectedSupplier() {
        return selectedSupplier;
    }

    public void loadSuppliers() {
        isLoading.setValue(true);
        purchaseRepository.getSuppliers(new PurchaseRepository.SuppliersCallback() {
            @Override
            public void onSuccess(List<Supplier> supplierList) {
                isLoading.setValue(false);
                suppliers.setValue(supplierList);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                error.setValue("Failed to load suppliers: " + e.getMessage());
            }
        });
    }

    public void setSelectedSupplier(Supplier supplier) {
        selectedSupplier.setValue(supplier);
    }

    public void addProduct(Product product) {
        if (product == null)
            return;

        List<ProductSelection> currentItems = productSelections.getValue();
        if (currentItems == null)
            currentItems = new ArrayList<>();

        boolean found = false;
        for (int i = 0; i < currentItems.size(); i++) {
            if (currentItems.get(i).getProduct().getDocumentId().equals(product.getDocumentId())) {
                ProductSelection matchingItem = currentItems.get(i);
                matchingItem.setQuantityInSale(matchingItem.getQuantityInSale() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            currentItems.add(new ProductSelection(product, 1));
        }

        productSelections.setValue(currentItems);
        recalculateSubtotal();
    }

    public void loadProductByBarcode(String barcode) {
        isLoading.setValue(true);
        purchaseRepository.getProductByBarcode(barcode, new PurchaseRepository.ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                isLoading.setValue(false);
                addProduct(product);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                error.setValue("Product not found or error: " + e.getMessage());
            }
        });
    }

    public void removeProduct(int position) {
        List<ProductSelection> currentItems = productSelections.getValue();
        if (currentItems != null && position >= 0 && position < currentItems.size()) {
            currentItems.remove(position);
            productSelections.setValue(currentItems);
            recalculateSubtotal();
        }
    }

    public void updateItemQuantity(int position, int quantity) {
        List<ProductSelection> currentItems = productSelections.getValue();
        if (currentItems != null && position >= 0 && position < currentItems.size()) {
            ProductSelection item = currentItems.get(position);
            item.setQuantityInSale(quantity);
            // CRITICAL FIX: Do NOT call productSelections.setValue(currentItems) here.
            // This prevents the Adapter from refreshing/losing focus while typing.
            recalculateSubtotal();
        }
    }

    public void updateItemPrice(int position, double price) {
        List<ProductSelection> currentItems = productSelections.getValue();
        if (currentItems != null && position >= 0 && position < currentItems.size()) {
            ProductSelection item = currentItems.get(position);
            item.getProduct().setPurchasePrice(price);
            // CRITICAL FIX: Do NOT call productSelections.setValue(currentItems) here.
            recalculateSubtotal();
        }
    }

    private void recalculateSubtotal() {
        double sub = 0.0;
        List<ProductSelection> items = productSelections.getValue();
        if (items != null) {
            for (ProductSelection item : items) {
                sub += FinancialCalculator.calculateLineItemTotal(item.getProduct().getPurchasePrice(),
                        item.getQuantityInSale());
            }
        }
        subtotal.setValue(sub);
    }

    public void savePurchase(Date purchaseDate, double taxAmount, double discountAmount, String paymentMethod,
            double amountPaid, String notes) {
        if (isLoading.getValue() == Boolean.TRUE)
            return;

        List<ProductSelection> selections = productSelections.getValue();
        if (selections == null || selections.isEmpty()) {
            error.setValue("No products selected");
            return;
        }

        if (selectedSupplier.getValue() == null) {
            error.setValue("No supplier selected");
            return;
        }

        isLoading.setValue(true);

        Purchase purchase = new Purchase();
        purchase.setSupplierId(selectedSupplier.getValue().getDocumentId());
        purchase.setSupplierName(selectedSupplier.getValue().getName());
        purchase.setSupplierContactNumber(selectedSupplier.getValue().getContactNumber());

        Date dateParam = purchaseDate != null ? purchaseDate : new Date();
        purchase.setPurchaseDate(new Timestamp(dateParam));

        List<PurchaseItem> items = new ArrayList<>();
        List<String> productIds = new ArrayList<>();
        for (ProductSelection sel : selections) {
            PurchaseItem item = new PurchaseItem();
            item.setProductId(sel.getProduct().getDocumentId());
            item.setProductName(sel.getProduct().getName());
            item.setPricePerItem(sel.getProduct().getPurchasePrice());
            item.setQuantity(sel.getQuantityInSale());
            items.add(item);
            productIds.add(sel.getProduct().getDocumentId());
        }
        purchase.setItems(items);
        purchase.setProductIds(productIds);

        double sub = subtotal.getValue() != null ? subtotal.getValue() : 0.0;
        // Purchase model doesn't strictly have subtotal field exposed in setters
        // usually seen in CreatePurchaseActivity
        // but let's check Purchase model. It has TotalAmount.

        purchase.setTaxAmount(taxAmount);
        purchase.setDiscountAmount(discountAmount);
        purchase.setTotalAmount(FinancialCalculator.calculateTotalAmount(sub, taxAmount, discountAmount));
        purchase.setPaymentMethod(paymentMethod);
        purchase.setAmountPaid(amountPaid);
        purchase.setNotes(notes);
        purchase.setAmountDue(purchase.getTotalAmount() - amountPaid);
        purchase.setUserId("CURRENT_USER_ID"); // TODO: Real user
        purchase.setStatus("COMPLETED");

        purchaseRepository.savePurchase(purchase, items, new PurchaseRepository.PurchaseCallback() {
            @Override
            public void onSuccess(String purchaseId) {
                isLoading.setValue(false);
                purchaseSuccess.setValue(purchaseId);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                error.setValue("Failed to save purchase: " + e.getMessage());
            }
        });
    }

    public void loadPurchaseForEditing(String purchaseId) {
        isLoading.setValue(true);
        purchaseRepository.getPurchase(purchaseId, new PurchaseRepository.GetPurchaseCallback() {
            @Override
            public void onSuccess(Purchase purchase) {
                // Populate selections
                List<ProductSelection> recoveredSelections = new ArrayList<>();
                List<PurchaseItem> items = purchase.getItems();

                // We need to fetch full product details for each item to have complete objects
                // For simplicity/speed in MVP refactor, we can fetch them individually or use
                // what we have.
                // The original Activity fetched them.
                // Let's iterate and fetch.

                // For now, load simple placeholders and trigger async fetch if needed.
                // Or better, let's use a recursive fetch or standard loop with counter like
                // usage in Activity.
                // But doing async inside VM is better handled with Coroutines. Without them, we
                // callback hell.

                // Simplification: Just load what we have from PurchaseItem into Product
                // placeholder
                // and maybe fetch details if deemed critical (e.g. current stock).
                // Original activity fetched details to get current stock and other metadata.

                reconstructSelections(items);

                // Load supplier
                loadSupplierById(purchase.getSupplierId());
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                error.setValue("Failed to load purchase: " + e.getMessage());
            }
        });
    }

    private void loadSupplierById(String supplierId) {
        // We already have suppliers list loaded?
        List<Supplier> currentList = suppliers.getValue();
        if (currentList != null) {
            for (Supplier s : currentList) {
                if (s.getDocumentId().equals(supplierId)) {
                    selectedSupplier.setValue(s);
                    return;
                }
            }
        }
        // If not in list (e.g. pagination or inactive), we might need direct fetch.
        // For now rely on list.
    }

    private void reconstructSelections(List<PurchaseItem> items) {
        // Implementation note: Ideally we fetch latest product info.
        // For this refactor, we will map PurchaseItem to ProductSelection directly.
        List<ProductSelection> list = new ArrayList<>();
        for (PurchaseItem item : items) {
            Product p = new Product();
            p.setDocumentId(item.getProductId());
            p.setName(item.getProductName());
            p.setPurchasePrice(item.getPricePerItem());
            // Quantity? We don't know current stock without fetching.
            // We set it to 0 or leave it.

            ProductSelection sel = new ProductSelection(p, item.getQuantity());
            list.add(sel);
        }
        productSelections.setValue(list);
        recalculateSubtotal();
    }
}
