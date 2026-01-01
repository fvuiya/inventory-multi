package com.bsoft.inventorymanager.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bsoft.inventorymanager.models.Customer;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.Sale;
import com.bsoft.inventorymanager.models.SaleItem;
import com.bsoft.inventorymanager.repositories.SaleRepository;
import com.bsoft.inventorymanager.utils.FinancialCalculator;
import com.bsoft.inventorymanager.models.ProductSelection;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CreateSaleViewModel extends ViewModel {

    private final SaleRepository saleRepository;

    // State
    private final MutableLiveData<List<ProductSelection>> productSelections = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Customer> selectedCustomer = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> saleSuccess = new MutableLiveData<>();
    private final MutableLiveData<List<Customer>> customers = new MutableLiveData<>();

    @Inject
    public CreateSaleViewModel(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
        loadCustomers();
    }

    // Getters
    public LiveData<List<ProductSelection>> getProductSelections() {
        return productSelections;
    }

    public LiveData<List<Customer>> getCustomers() {
        return customers;
    }

    public LiveData<Double> getSubtotal() {
        return subtotal;
    }

    public LiveData<Customer> getSelectedCustomer() {
        return selectedCustomer;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSaleSuccess() {
        return saleSuccess;
    }

    // Actions
    public void setSelectedCustomer(Customer customer) {
        selectedCustomer.setValue(customer);
    }

    public void addProduct(Product product) {
        List<ProductSelection> currentItems = productSelections.getValue();
        if (currentItems == null)
            currentItems = new ArrayList<>();

        // Check if already exists
        boolean found = false;
        for (ProductSelection item : currentItems) {
            if (item.getProduct().getDocumentId().equals(product.getDocumentId())) {
                // Already added - increment
                item.setQuantityInSale(item.getQuantityInSale() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            ProductSelection newItem = new ProductSelection(product, 1);
            currentItems.add(newItem);
        }

        productSelections.setValue(currentItems);
        recalculateSubtotal();
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
            // productSelections.setValue(currentItems); // Prevent loop/focus loss
            recalculateSubtotal();
        }
    }

    public void updateItemPrice(int position, double price) {
        List<ProductSelection> currentItems = productSelections.getValue();
        if (currentItems != null && position >= 0 && position < currentItems.size()) {
            ProductSelection item = currentItems.get(position);
            item.getProduct().setSellingPrice(price);
            // productSelections.setValue(currentItems); // Prevent loop/focus loss
            recalculateSubtotal();
        }
    }

    private void recalculateSubtotal() {
        double sub = 0.0;
        List<ProductSelection> items = productSelections.getValue();
        if (items != null) {
            for (ProductSelection item : items) {
                sub += FinancialCalculator.calculateLineItemTotal(item.getProduct().getSellingPrice(),
                        item.getQuantityInSale());
            }
        }
        subtotal.setValue(sub);
    }

    public void saveSale(Date saleDate, double taxAmount, double discountAmount, String paymentMethod,
            double amountPaid,
            String notes) {
        if (isLoading.getValue() == Boolean.TRUE)
            return;

        List<ProductSelection> selections = productSelections.getValue();
        if (selections == null || selections.isEmpty()) {
            error.setValue("No products selected");
            return;
        }

        if (selectedCustomer.getValue() == null) {
            error.setValue("No customer selected");
            return;
        }

        isLoading.setValue(true);

        Sale sale = new Sale();
        sale.setCustomerId(selectedCustomer.getValue().getDocumentId());
        sale.setCustomerName(selectedCustomer.getValue().getName());

        Date dateParam = saleDate != null ? saleDate : new Date();
        sale.setSaleDate(new Timestamp(dateParam));

        List<SaleItem> items = new ArrayList<>();
        double totalCost = 0.0;
        for (ProductSelection sel : selections) {
            SaleItem item = new SaleItem();
            item.setProductId(sel.getProduct().getDocumentId());
            item.setProductName(sel.getProduct().getName());
            item.setPricePerItem(sel.getProduct().getSellingPrice());
            item.setQuantity(sel.getQuantityInSale());
            item.setCategory(sel.getProduct().getCategory());
            item.setBrand(sel.getProduct().getBrand());

            // Capture historical cost price
            double itemCost = sel.getProduct().getCostPrice();
            item.setCostPrice(itemCost);
            totalCost += (itemCost * sel.getQuantityInSale());

            items.add(item);
        }
        sale.setItems(items);

        double sub = subtotal.getValue() != null ? subtotal.getValue() : 0.0;
        sale.setSubtotal(sub);
        sale.setTaxAmount(taxAmount);
        sale.setDiscountAmount(discountAmount);

        double totalAmount = FinancialCalculator.calculateTotalAmount(sub, taxAmount, discountAmount);
        sale.setTotalAmount(totalAmount);

        // Populate pre-calculated financial data
        sale.setTotalCost(totalCost);
        sale.setTotalProfit((sub - discountAmount) - totalCost);

        sale.setPaymentMethod(paymentMethod);
        sale.setAmountPaid(amountPaid);
        sale.setNotes(notes);
        // Calculate amountDue
        sale.setAmountDue(totalAmount - amountPaid);
        // TODO: Get real user ID
        sale.setUserId("CURRENT_USER_ID");
        sale.setStatus("confirmed"); // Default to confirmed for now

        saleRepository.saveSale(sale, items, new SaleRepository.SaleCallback() {
            @Override
            public void onSuccess(String saleId) {
                isLoading.setValue(false);
                saleSuccess.setValue(saleId);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                error.setValue(e.getMessage());
            }
        });
    }

    public void loadProductByBarcode(String barcode) {
        isLoading.setValue(true);
        saleRepository.getProductByBarcode(barcode, new SaleRepository.ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                isLoading.setValue(false);
                addProduct(product);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                error.setValue("Product not found");
            }
        });
    }

    public void loadSale(String saleId) {
        isLoading.setValue(true);
        saleRepository.getSale(saleId, new SaleRepository.GetSaleCallback() {
            @Override
            public void onSuccess(Sale sale) {
                List<SaleItem> items = sale.getItems();
                if (items == null || items.isEmpty()) {
                    isLoading.setValue(false);
                    return;
                }

                // Reset state
                productSelections.setValue(new ArrayList<>());

                // Customer
                Customer c = new Customer();
                c.setDocumentId(sale.getCustomerId());
                c.setName(sale.getCustomerName());
                // Ideally we should fetch the full customer object here
                selectedCustomer.setValue(c);

                fetchProductsForItems(items, 0, new ArrayList<>());
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                error.setValue("Failed to load sale: " + e.getMessage());
            }
        });
    }

    private void loadCustomers() {
        saleRepository.getCustomers(new SaleRepository.CustomersCallback() {
            @Override
            public void onSuccess(List<Customer> customerList) {
                customers.setValue(customerList);
            }

            @Override
            public void onFailure(Exception e) {
                error.setValue("Failed to load customers: " + e.getMessage());
            }
        });
    }

    private void fetchProductsForItems(List<SaleItem> items, int index, List<ProductSelection> accumulated) {
        if (index >= items.size()) {
            productSelections.setValue(accumulated);
            recalculateSubtotal();
            isLoading.setValue(false);
            return;
        }

        SaleItem item = items.get(index);
        saleRepository.getProduct(item.getProductId(), new SaleRepository.ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                // Use fresh product data but maintain sale quantity
                ProductSelection sel = new ProductSelection(product, item.getQuantity());
                // Override selling price with the price from the sale item to reflect
                // historical price
                product.setSellingPrice(item.getPricePerItem());

                accumulated.add(sel);
                fetchProductsForItems(items, index + 1, accumulated);
            }

            @Override
            public void onFailure(Exception e) {
                // If product deleted or not found, create placeholder
                Product placeholder = new Product();
                placeholder.setDocumentId(item.getProductId());
                placeholder.setName(item.getProductName() + " (Unavailable)");
                placeholder.setSellingPrice(item.getPricePerItem());
                placeholder.setQuantity(0);

                ProductSelection sel = new ProductSelection(placeholder, item.getQuantity());
                accumulated.add(sel);
                fetchProductsForItems(items, index + 1, accumulated);
            }
        });
    }
}
