package com.bsoft.inventorymanager.reports.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.bsoft.inventorymanager.models.Customer;
import com.bsoft.inventorymanager.models.Product;
import com.bsoft.inventorymanager.models.Purchase;
import com.bsoft.inventorymanager.models.PurchaseItem;
import com.bsoft.inventorymanager.models.Sale;
import com.bsoft.inventorymanager.models.SaleItem;
import com.bsoft.inventorymanager.models.Expense;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.firebase.functions.FirebaseFunctions;
import java.time.ZoneId;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ReportsViewModel extends ViewModel {

    public enum UiState {
        LOADING, HAS_DATA, NO_DATA
    }

    private final FirebaseFirestore db;
    private final FirebaseFunctions functions;

    @Inject
    public ReportsViewModel(FirebaseFirestore db, FirebaseFunctions functions) {
        this.db = db;
        this.functions = functions;
    }

    // --- LiveData Objects ---
    private final MutableLiveData<List<Entry>> salesOverTimeData = new MutableLiveData<>();
    private final MutableLiveData<UiState> salesOverTimeState = new MutableLiveData<>();
    private final MutableLiveData<List<Entry>> profitOverTimeData = new MutableLiveData<>();
    private final MutableLiveData<UiState> profitOverTimeState = new MutableLiveData<>();
    private final MutableLiveData<Double> averageOrderValueData = new MutableLiveData<>();
    private final MutableLiveData<UiState> averageOrderValueState = new MutableLiveData<>();
    private final MutableLiveData<Double> totalRevenueData = new MutableLiveData<>();
    private final MutableLiveData<UiState> totalRevenueState = new MutableLiveData<>();
    private final MutableLiveData<Double> totalProfitData = new MutableLiveData<>();
    private final MutableLiveData<UiState> totalProfitState = new MutableLiveData<>();
    private final MutableLiveData<Double> totalExpensesData = new MutableLiveData<>();
    private final MutableLiveData<UiState> totalExpensesState = new MutableLiveData<>();
    private final MutableLiveData<Double> netProfitData = new MutableLiveData<>();
    private final MutableLiveData<UiState> netProfitState = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalTransactionsData = new MutableLiveData<>();
    private final MutableLiveData<UiState> totalTransactionsState = new MutableLiveData<>();
    private final MutableLiveData<Double> totalInventoryValueData = new MutableLiveData<>();
    private final MutableLiveData<UiState> totalInventoryValueState = new MutableLiveData<>();
    private final MutableLiveData<List<BarEntry>> topSellingProductsData = new MutableLiveData<>();
    private final MutableLiveData<UiState> topSellingProductsState = new MutableLiveData<>();
    private final MutableLiveData<List<String>> topSellingProductsLabels = new MutableLiveData<>();
    private final MutableLiveData<List<BarEntry>> mostProfitableProductsData = new MutableLiveData<>();
    private final MutableLiveData<UiState> mostProfitableProductsState = new MutableLiveData<>();
    private final MutableLiveData<List<String>> mostProfitableProductsLabels = new MutableLiveData<>();
    private final MutableLiveData<List<Product>> lowStockProductsData = new MutableLiveData<>();
    private final MutableLiveData<UiState> lowStockProductsState = new MutableLiveData<>();
    private final MutableLiveData<List<Product>> slowMovingProductsData = new MutableLiveData<>();
    private final MutableLiveData<UiState> slowMovingProductsState = new MutableLiveData<>();
    private final MutableLiveData<List<Entry>> customerAcquisitionData = new MutableLiveData<>();
    private final MutableLiveData<UiState> customerAcquisitionState = new MutableLiveData<>();
    private final MutableLiveData<List<BarEntry>> topCustomersData = new MutableLiveData<>();
    private final MutableLiveData<UiState> topCustomersState = new MutableLiveData<>();
    private final MutableLiveData<List<String>> topCustomersLabels = new MutableLiveData<>();
    private final MutableLiveData<List<Customer>> lapsedCustomersData = new MutableLiveData<>();
    private final MutableLiveData<UiState> lapsedCustomersState = new MutableLiveData<>();
    private final MutableLiveData<List<PieEntry>> salesByCategoryData = new MutableLiveData<>();
    private final MutableLiveData<UiState> salesByCategoryState = new MutableLiveData<>();
    private final MutableLiveData<List<BarEntry>> totalSpendBySupplierData = new MutableLiveData<>();
    private final MutableLiveData<UiState> totalSpendBySupplierState = new MutableLiveData<>();
    private final MutableLiveData<List<String>> totalSpendBySupplierLabels = new MutableLiveData<>();
    private final MutableLiveData<List<Entry>> purchaseOrdersOverTimeData = new MutableLiveData<>();
    private final MutableLiveData<UiState> purchaseOrdersOverTimeState = new MutableLiveData<>();
    private final MutableLiveData<List<Entry>> purchaseOrdersOverTimeVolumeData = new MutableLiveData<>();
    private final MutableLiveData<UiState> purchaseOrdersOverTimeVolumeState = new MutableLiveData<>();
    private final MutableLiveData<List<BarEntry>> topPurchasedProductsData = new MutableLiveData<>();
    private final MutableLiveData<UiState> topPurchasedProductsState = new MutableLiveData<>();
    private final MutableLiveData<List<String>> topPurchasedProductsLabels = new MutableLiveData<>();
    private final MutableLiveData<List<PieEntry>> newVsReturningData = new MutableLiveData<>();
    private final MutableLiveData<UiState> newVsReturningState = new MutableLiveData<>();

    // --- Granularity Support ---
    public enum Granularity {
        DAY, HOUR, MINUTE
    }

    private final MutableLiveData<Granularity> currentGranularity = new MutableLiveData<>(Granularity.DAY);

    public LiveData<Granularity> getCurrentGranularity() {
        return currentGranularity;
    }

    private void updateGranularity(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        // Use setValue for synchronous update since this is called from UI thread
        if (diff > TimeUnit.DAYS.toMillis(32)) {
            // For > 1 month (e.g. Year), show Daily stats
            currentGranularity.setValue(Granularity.DAY);
        } else {
            // For Month (up to 32 days), Week, or Today, show Minute precision
            currentGranularity.setValue(Granularity.MINUTE);
        }
    }

    private Date currentStartDate;
    private Date currentEndDate;
    private final MutableLiveData<Boolean> refreshTrigger = new MutableLiveData<>();

    public void triggerRefresh() {
        refreshTrigger.setValue(true);
    }

    public LiveData<Boolean> getRefreshTrigger() {
        return refreshTrigger;
    }

    public void setDateRange(Date startDate, Date endDate) {
        this.currentStartDate = startDate;
        this.currentEndDate = endDate;
        updateGranularity(startDate, endDate);
        fetchDashboardStats(startDate, endDate);
    }

    public Date getCurrentStartDate() {
        return currentStartDate;
    }

    public Date getCurrentEndDate() {
        return currentEndDate;
    }

    // --- Getters ---
    public LiveData<List<Entry>> getSalesOverTimeData() {
        return salesOverTimeData;
    }

    public LiveData<UiState> getSalesOverTimeState() {
        return salesOverTimeState;
    }

    public LiveData<List<Entry>> getProfitOverTimeData() {
        return profitOverTimeData;
    }

    public LiveData<UiState> getProfitOverTimeState() {
        return profitOverTimeState;
    }

    // ... [Other getters remain unchanged, assumed correctly placed] ...

    // --- Process Methods ---
    private long getGranularityDivisor() {
        Granularity g = currentGranularity.getValue();
        if (g == Granularity.HOUR)
            return TimeUnit.HOURS.toMillis(1);
        if (g == Granularity.MINUTE)
            return TimeUnit.MINUTES.toMillis(1);
        return TimeUnit.DAYS.toMillis(1);
    }

    private void processSalesData(QuerySnapshot snapshots, Date startDate) {
        if (snapshots == null || snapshots.isEmpty()) {
            salesOverTimeState.postValue(UiState.NO_DATA);
            salesOverTimeData.postValue(new ArrayList<>());
            return;
        }
        Map<Long, Double> salesByTime = new HashMap<>();
        long startMillis = startDate.getTime();
        long divisor = getGranularityDivisor();

        for (DocumentSnapshot document : snapshots.getDocuments()) {
            try {
                Sale sale = document.toObject(Sale.class);
                if (sale != null && sale.getSaleDate() != null) {
                    long saleMillis = sale.getSaleDate().toDate().getTime();
                    long timeIndex = (saleMillis - startMillis) / divisor;
                    salesByTime.put(timeIndex, salesByTime.getOrDefault(timeIndex, 0.0) + sale.getTotalAmount());
                }
            } catch (Exception e) {
                // Skip corrupted documents
            }
        }
        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<Long, Double> mapEntry : salesByTime.entrySet()) {
            entries.add(new Entry(mapEntry.getKey().floatValue(), mapEntry.getValue().floatValue()));
        }
        // Sort entries by X index
        Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        salesOverTimeData.postValue(entries);
        salesOverTimeState.postValue(UiState.HAS_DATA);
    }

    private void processProfitOverTime(QuerySnapshot salesSnapshots, Date startDate) {
        if (salesSnapshots == null || salesSnapshots.isEmpty()) {
            profitOverTimeState.postValue(UiState.NO_DATA);
            profitOverTimeData.postValue(new ArrayList<>());
            return;
        }

        Map<Long, Double> profitByTime = new HashMap<>();
        long startMillis = startDate.getTime();
        long divisor = getGranularityDivisor();

        for (DocumentSnapshot doc : salesSnapshots.getDocuments()) {
            try {
                Sale sale = doc.toObject(Sale.class);
                if (sale != null && sale.getSaleDate() != null) {
                    long saleMillis = sale.getSaleDate().toDate().getTime();
                    long timeIndex = (saleMillis - startMillis) / divisor;
                    Double currentProfit = profitByTime.getOrDefault(timeIndex, 0.0);
                    Double saleProfit = sale.getTotalProfit();
                    if (saleProfit == null)
                        saleProfit = 0.0;
                    profitByTime.put(timeIndex, (currentProfit != null ? currentProfit : 0.0) + saleProfit);
                }
            } catch (Exception e) {
                // Skip corrupted documents
            }
        }

        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<Long, Double> mapEntry : profitByTime.entrySet()) {
            entries.add(new Entry(mapEntry.getKey().floatValue(), mapEntry.getValue().floatValue()));
        }
        Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        profitOverTimeData.postValue(entries);
        profitOverTimeState.postValue(UiState.HAS_DATA);
    }

    public LiveData<Double> getAverageOrderValueData() {
        return averageOrderValueData;
    }

    public LiveData<UiState> getAverageOrderValueState() {
        return averageOrderValueState;
    }

    public LiveData<Double> getTotalRevenueData() {
        return totalRevenueData;
    }

    public LiveData<UiState> getTotalRevenueState() {
        return totalRevenueState;
    }

    public LiveData<Double> getTotalProfitData() {
        return totalProfitData;
    }

    public LiveData<UiState> getTotalProfitState() {
        return totalProfitState;
    }

    public LiveData<Double> getTotalExpensesData() {
        return totalExpensesData;
    }

    public LiveData<UiState> getTotalExpensesState() {
        return totalExpensesState;
    }

    public LiveData<Double> getNetProfitData() {
        return netProfitData;
    }

    public LiveData<UiState> getNetProfitState() {
        return netProfitState;
    }

    public LiveData<Integer> getTotalTransactionsData() {
        return totalTransactionsData;
    }

    public LiveData<UiState> getTotalTransactionsState() {
        return totalTransactionsState;
    }

    public LiveData<Double> getTotalInventoryValueData() {
        return totalInventoryValueData;
    }

    public LiveData<UiState> getTotalInventoryValueState() {
        return totalInventoryValueState;
    }

    public LiveData<List<BarEntry>> getTopSellingProductsData() {
        return topSellingProductsData;
    }

    public LiveData<UiState> getTopSellingProductsState() {
        return topSellingProductsState;
    }

    public LiveData<List<String>> getTopSellingProductsLabels() {
        return topSellingProductsLabels;
    }

    public LiveData<List<BarEntry>> getMostProfitableProductsData() {
        return mostProfitableProductsData;
    }

    public LiveData<UiState> getMostProfitableProductsState() {
        return mostProfitableProductsState;
    }

    public LiveData<List<String>> getMostProfitableProductsLabels() {
        return mostProfitableProductsLabels;
    }

    public LiveData<List<Product>> getLowStockProductsData() {
        return lowStockProductsData;
    }

    public LiveData<UiState> getLowStockProductsState() {
        return lowStockProductsState;
    }

    public LiveData<List<Product>> getSlowMovingProductsData() {
        return slowMovingProductsData;
    }

    public LiveData<UiState> getSlowMovingProductsState() {
        return slowMovingProductsState;
    }

    public LiveData<List<Entry>> getCustomerAcquisitionData() {
        return customerAcquisitionData;
    }

    public LiveData<UiState> getCustomerAcquisitionState() {
        return customerAcquisitionState;
    }

    public LiveData<List<BarEntry>> getTopCustomersData() {
        return topCustomersData;
    }

    public LiveData<UiState> getTopCustomersState() {
        return topCustomersState;
    }

    public LiveData<List<String>> getTopCustomersLabels() {
        return topCustomersLabels;
    }

    public LiveData<List<Customer>> getLapsedCustomersData() {
        return lapsedCustomersData;
    }

    public LiveData<UiState> getLapsedCustomersState() {
        return lapsedCustomersState;
    }

    public LiveData<List<PieEntry>> getSalesByCategoryData() {
        return salesByCategoryData;
    }

    public LiveData<UiState> getSalesByCategoryState() {
        return salesByCategoryState;
    }

    public LiveData<List<BarEntry>> getTotalSpendBySupplierData() {
        return totalSpendBySupplierData;
    }

    public LiveData<UiState> getTotalSpendBySupplierState() {
        return totalSpendBySupplierState;
    }

    public LiveData<List<String>> getTotalSpendBySupplierLabels() {
        return totalSpendBySupplierLabels;
    }

    public LiveData<List<Entry>> getPurchaseOrdersOverTimeData() {
        return purchaseOrdersOverTimeData;
    }

    public LiveData<UiState> getPurchaseOrdersOverTimeState() {
        return purchaseOrdersOverTimeState;
    }

    public LiveData<List<Entry>> getPurchaseOrdersOverTimeVolumeData() {
        return purchaseOrdersOverTimeVolumeData;
    }

    public LiveData<UiState> getPurchaseOrdersOverTimeVolumeState() {
        return purchaseOrdersOverTimeVolumeState;
    }

    public LiveData<List<BarEntry>> getTopPurchasedProductsData() {
        return topPurchasedProductsData;
    }

    public LiveData<UiState> getTopPurchasedProductsState() {
        return topPurchasedProductsState;
    }

    public LiveData<List<String>> getTopPurchasedProductsLabels() {
        return topPurchasedProductsLabels;
    }

    public LiveData<List<PieEntry>> getNewVsReturningData() {
        return newVsReturningData;
    }

    public LiveData<UiState> getNewVsReturningState() {
        return newVsReturningState;
    }

    // --- Load Methods ---
    // --- Cloud Function Migration ---

    public void fetchDashboardStats(Date startDate, Date endDate) {
        if (startDate == null || endDate == null)
            return;

        // Set Loading States
        totalRevenueState.postValue(UiState.LOADING);
        totalProfitState.postValue(UiState.LOADING);
        totalExpensesState.postValue(UiState.LOADING);
        netProfitState.postValue(UiState.LOADING);
        totalTransactionsState.postValue(UiState.LOADING);
        averageOrderValueState.postValue(UiState.LOADING);
        salesOverTimeState.postValue(UiState.LOADING);
        profitOverTimeState.postValue(UiState.LOADING);
        topSellingProductsState.postValue(UiState.LOADING);
        salesByCategoryState.postValue(UiState.LOADING);

        Map<String, Object> data = new HashMap<>();
        data.put("startDate", startDate.getTime());
        data.put("endDate", endDate.getTime());
        try {
            data.put("timeZone", ZoneId.systemDefault().getId());
        } catch (Exception e) {
            data.put("timeZone", "UTC");
        }

        functions.getHttpsCallable("getDashboardStats")
                .call(data)
                .addOnSuccessListener(result -> {
                    try {
                        Map<String, Object> response = (Map<String, Object>) result.getData();
                        processDashboardStats(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setAllStates(UiState.NO_DATA);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    setAllStates(UiState.NO_DATA);
                });
    }

    private void setAllStates(UiState state) {
        totalRevenueState.postValue(state);
        totalProfitState.postValue(state);
        totalExpensesState.postValue(state);
        netProfitState.postValue(state);
        totalTransactionsState.postValue(state);
        averageOrderValueState.postValue(state);
        salesOverTimeState.postValue(state);
        profitOverTimeState.postValue(state);
        topSellingProductsState.postValue(state);
        salesByCategoryState.postValue(state);
    }

    private void processDashboardStats(Map<String, Object> data) {
        if (data == null) {
            setAllStates(UiState.NO_DATA);
            return;
        }

        // 1. Scalars
        Number revenue = (Number) data.getOrDefault("totalRevenue", 0.0);
        totalRevenueData.postValue(revenue.doubleValue());
        totalRevenueState.postValue(UiState.HAS_DATA);

        Number profit = (Number) data.getOrDefault("totalProfit", 0.0);
        totalProfitData.postValue(profit.doubleValue());
        totalProfitState.postValue(UiState.HAS_DATA);

        Number expenses = (Number) data.getOrDefault("totalExpenses", 0.0);
        totalExpensesData.postValue(expenses.doubleValue());
        totalExpensesState.postValue(UiState.HAS_DATA);

        Number net = (Number) data.getOrDefault("netProfit", 0.0);
        netProfitData.postValue(net.doubleValue());
        netProfitState.postValue(UiState.HAS_DATA);

        Number tx = (Number) data.getOrDefault("totalTransactions", 0);
        totalTransactionsData.postValue(tx.intValue());
        totalTransactionsState.postValue(UiState.HAS_DATA);

        Number aov = (Number) data.getOrDefault("averageOrderValue", 0.0);
        averageOrderValueData.postValue(aov.doubleValue());
        averageOrderValueState.postValue(UiState.HAS_DATA);

        // 2. Charts
        processChartData((List<Map<String, Object>>) data.get("salesOverTime"), salesOverTimeData, salesOverTimeState);
        processChartData((List<Map<String, Object>>) data.get("profitOverTime"), profitOverTimeData,
                profitOverTimeState);

        // 3. Top Lists
        List<Map<String, Object>> topProducts = (List<Map<String, Object>>) data.get("topSellingProducts");
        List<BarEntry> productEntries = new ArrayList<>();
        List<String> productLabels = new ArrayList<>();
        if (topProducts != null) {
            for (int i = 0; i < topProducts.size(); i++) {
                Map<String, Object> item = topProducts.get(i);
                String label = (String) item.get("label");
                Number val = (Number) item.get("value");
                // Chart expects x index (0, 1, 2...)
                productEntries.add(new BarEntry(topProducts.size() - 1 - i, val.floatValue())); // Reverse for chart
                productLabels.add(label);
            }
            Collections.reverse(productLabels); // Match UI expectation
        }
        topSellingProductsData.postValue(productEntries);
        topSellingProductsLabels.postValue(productLabels);
        topSellingProductsState.postValue(!productEntries.isEmpty() ? UiState.HAS_DATA : UiState.NO_DATA);

        // 4. Categories
        List<Map<String, Object>> categories = (List<Map<String, Object>>) data.get("salesByCategory");
        List<PieEntry> catEntries = new ArrayList<>();
        if (categories != null) {
            for (Map<String, Object> item : categories) {
                String label = (String) item.get("label");
                Number val = (Number) item.get("value");
                catEntries.add(new PieEntry(val.floatValue(), label));
            }
        }
        salesByCategoryData.postValue(catEntries);
        salesByCategoryState.postValue(!catEntries.isEmpty() ? UiState.HAS_DATA : UiState.NO_DATA);
    }

    private void processChartData(List<Map<String, Object>> list, MutableLiveData<List<Entry>> liveData,
            MutableLiveData<UiState> stateData) {
        List<Entry> entries = new ArrayList<>();
        if (list != null) {
            for (Map<String, Object> point : list) {
                Number x = (Number) point.get("x");
                Number y = (Number) point.get("y");
                entries.add(new Entry(x.floatValue(), y.floatValue()));
            }
        }
        Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));
        liveData.postValue(entries);
        stateData.postValue(!entries.isEmpty() ? UiState.HAS_DATA : UiState.NO_DATA);
    }

    // --- Legacy Load Methods (Deprecated - Handled by Cloud Function) ---

    public void loadSalesOverTime(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    public void loadProfitOverTime(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    public void loadAverageOrderValue(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    public void loadTotalRevenue(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    public void loadTotalProfit(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    public void loadTotalExpenses(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    private void loadNetProfit(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    public void loadTotalTransactions(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    public void loadTotalInventoryValue() {
        totalInventoryValueState.postValue(UiState.LOADING);
        db.collection("products").get()
                .addOnSuccessListener(this::processTotalInventoryValue)
                .addOnFailureListener(e -> totalInventoryValueState.postValue(UiState.NO_DATA));
    }

    public void loadTopSellingProducts(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    public void loadTopPurchasedProducts(Date startDate, Date endDate) {
        topPurchasedProductsState.postValue(UiState.LOADING);
        db.collection("purchases").whereGreaterThanOrEqualTo("purchaseDate", new Timestamp(startDate))
                .whereLessThanOrEqualTo("purchaseDate", new Timestamp(endDate)).get()
                .addOnSuccessListener(this::processTopPurchasedProducts)
                .addOnFailureListener(e -> topPurchasedProductsState.postValue(UiState.NO_DATA));
    }

    public void loadMostProfitableProducts(Date startDate, Date endDate) {
        mostProfitableProductsState.postValue(UiState.LOADING);
        db.collection("sales").whereGreaterThanOrEqualTo("saleDate", new Timestamp(startDate))
                .whereLessThanOrEqualTo("saleDate", new Timestamp(endDate)).get()
                .addOnSuccessListener(this::processProductProfitability)
                .addOnFailureListener(e -> mostProfitableProductsState.postValue(UiState.NO_DATA));
    }

    public void loadLowStockProducts() {
        lowStockProductsState.postValue(UiState.LOADING);
        db.collection("products").whereLessThanOrEqualTo("quantity", 10).orderBy("quantity", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(this::processLowStockProducts)
                .addOnFailureListener(e -> lowStockProductsState.postValue(UiState.NO_DATA));
    }

    public void loadSlowMovingProducts(Date startDate, Date endDate) {
        slowMovingProductsState.postValue(UiState.LOADING);
        Task<QuerySnapshot> salesTask = db.collection("sales")
                .whereGreaterThanOrEqualTo("saleDate", new Timestamp(startDate))
                .whereLessThanOrEqualTo("saleDate", new Timestamp(endDate)).get();
        Task<QuerySnapshot> productsTask = db.collection("products").get();

        Tasks.whenAllSuccess(salesTask, productsTask).addOnSuccessListener(results -> {
            QuerySnapshot salesSnapshot = (QuerySnapshot) results.get(0);
            QuerySnapshot productsSnapshot = (QuerySnapshot) results.get(1);

            Set<String> soldProductIds = new HashSet<>();
            for (DocumentSnapshot doc : salesSnapshot.getDocuments()) {
                Sale sale = doc.toObject(Sale.class);
                if (sale != null && sale.getItems() != null) {
                    for (SaleItem item : sale.getItems()) {
                        soldProductIds.add(item.getProductId());
                    }
                }
            }

            List<Product> allProducts = new ArrayList<>();
            for (DocumentSnapshot doc : productsSnapshot.getDocuments()) {
                try {
                    Product p = doc.toObject(Product.class);
                    if (p != null) {
                        p.setDocumentId(doc.getId());
                        allProducts.add(p);
                    }
                } catch (Exception e) {
                    /* Skip */}
            }
            List<Product> slowMovingProducts = allProducts.stream()
                    .filter(p -> !soldProductIds.contains(p.getDocumentId()))
                    .collect(Collectors.toList());

            if (slowMovingProducts.isEmpty()) {
                slowMovingProductsState.postValue(UiState.NO_DATA);
            } else {
                slowMovingProductsData.postValue(slowMovingProducts);
                slowMovingProductsState.postValue(UiState.HAS_DATA);
            }
        }).addOnFailureListener(e -> slowMovingProductsState.postValue(UiState.NO_DATA));
    }

    public void loadLapsedCustomers() {
        lapsedCustomersState.postValue(UiState.LOADING);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -90);
        Date ninetyDaysAgo = cal.getTime();

        Task<QuerySnapshot> allCustomersTask = db.collection("customers").whereEqualTo("isActive", true).get();
        Task<QuerySnapshot> recentSalesTask = db.collection("sales")
                .whereGreaterThanOrEqualTo("saleDate", new Timestamp(ninetyDaysAgo)).get();

        Tasks.whenAllSuccess(allCustomersTask, recentSalesTask).addOnSuccessListener(results -> {
            QuerySnapshot customersSnapshot = (QuerySnapshot) results.get(0);
            QuerySnapshot salesSnapshot = (QuerySnapshot) results.get(1);

            Set<String> recentCustomerIds = new HashSet<>();
            for (DocumentSnapshot doc : salesSnapshot.getDocuments()) {
                Sale sale = doc.toObject(Sale.class);
                if (sale != null && sale.getCustomerId() != null) {
                    recentCustomerIds.add(sale.getCustomerId());
                }
            }

            List<Customer> allCustomers = new ArrayList<>();
            for (DocumentSnapshot doc : customersSnapshot.getDocuments()) {
                try {
                    Customer c = doc.toObject(Customer.class);
                    if (c != null) {
                        c.setDocumentId(doc.getId());
                        allCustomers.add(c);
                    }
                } catch (Exception e) {
                    /* Skip */}
            }
            List<Customer> lapsedCustomers = allCustomers.stream()
                    .filter(c -> !recentCustomerIds.contains(c.getDocumentId()))
                    .collect(Collectors.toList());

            if (lapsedCustomers.isEmpty()) {
                lapsedCustomersState.postValue(UiState.NO_DATA);
            } else {
                lapsedCustomersData.postValue(lapsedCustomers);
                lapsedCustomersState.postValue(UiState.HAS_DATA);
            }
        }).addOnFailureListener(e -> lapsedCustomersState.postValue(UiState.NO_DATA));
    }

    public void loadCustomerAcquisitionOverTime(Date startDate, Date endDate) {
        customerAcquisitionState.postValue(UiState.LOADING);
        db.collection("customers")
                .whereGreaterThanOrEqualTo("creationDate", new Timestamp(startDate))
                .whereLessThanOrEqualTo("creationDate", new Timestamp(endDate))
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots == null || snapshots.isEmpty()) {
                        customerAcquisitionState.postValue(UiState.NO_DATA);
                        customerAcquisitionData.postValue(new ArrayList<>());
                        return;
                    }
                    Map<Long, Integer> acquisitionsByTime = new HashMap<>();
                    long startMillis = startDate.getTime();
                    long divisor = getGranularityDivisor();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        Customer customer = document.toObject(Customer.class);
                        if (customer != null && customer.getCreationDate() != null) {
                            long creationMillis = customer.getCreationDate().toDate().getTime();
                            long timeIndex = (creationMillis - startMillis) / divisor;
                            acquisitionsByTime.put(timeIndex, acquisitionsByTime.getOrDefault(timeIndex, 0) + 1);
                        }
                    }
                    List<Entry> entries = new ArrayList<>();
                    for (Map.Entry<Long, Integer> mapEntry : acquisitionsByTime.entrySet()) {
                        entries.add(new Entry(mapEntry.getKey().floatValue(), mapEntry.getValue().floatValue()));
                    }
                    Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));
                    customerAcquisitionData.postValue(entries);
                    customerAcquisitionState.postValue(UiState.HAS_DATA);
                })
                .addOnFailureListener(e -> {
                    customerAcquisitionState.postValue(UiState.NO_DATA);
                    customerAcquisitionData.postValue(new ArrayList<>());
                });
    }

    public void loadTopCustomersBySpend(Date startDate, Date endDate) {
        topCustomersState.postValue(UiState.LOADING);
        db.collection("sales").whereGreaterThanOrEqualTo("saleDate", new Timestamp(startDate))
                .whereLessThanOrEqualTo("saleDate", new Timestamp(endDate)).get()
                .addOnSuccessListener(this::processTopCustomers)
                .addOnFailureListener(e -> topCustomersState.postValue(UiState.NO_DATA));
    }

    public void loadSalesByCategory(Date startDate, Date endDate) {
        // Handled by fetchDashboardStats
    }

    public void loadTotalSpendBySupplier(Date startDate, Date endDate) {
        totalSpendBySupplierState.postValue(UiState.LOADING);
        db.collection("purchases").whereGreaterThanOrEqualTo("purchaseDate", new Timestamp(startDate))
                .whereLessThanOrEqualTo("purchaseDate", new Timestamp(endDate)).get()
                .addOnSuccessListener(this::processTotalSpendBySupplier)
                .addOnFailureListener(e -> totalSpendBySupplierState.postValue(UiState.NO_DATA));
    }

    public void loadPurchaseOrdersOverTime(Date startDate, Date endDate) {
        purchaseOrdersOverTimeState.postValue(UiState.LOADING);
        db.collection("purchases").whereGreaterThanOrEqualTo("purchaseDate", new Timestamp(startDate))
                .whereLessThanOrEqualTo("purchaseDate", new Timestamp(endDate)).get()
                .addOnSuccessListener(snapshots -> processPurchaseOrdersOverTime(snapshots, startDate))
                .addOnFailureListener(e -> purchaseOrdersOverTimeState.postValue(UiState.NO_DATA));
    }

    public void loadPurchaseOrdersOverTimeVolume(Date startDate, Date endDate) {
        purchaseOrdersOverTimeVolumeState.postValue(UiState.LOADING);
        db.collection("purchases").whereGreaterThanOrEqualTo("purchaseDate", new Timestamp(startDate))
                .whereLessThanOrEqualTo("purchaseDate", new Timestamp(endDate)).get()
                .addOnSuccessListener(snapshots -> processPurchaseOrdersOverTimeVolume(snapshots, startDate))
                .addOnFailureListener(e -> purchaseOrdersOverTimeVolumeState.postValue(UiState.NO_DATA));
    }

    public void loadNewVsReturningCustomers(Date startDate, Date endDate) {
        newVsReturningState.postValue(UiState.LOADING);

        Task<QuerySnapshot> oldCustomersTask = db.collection("sales")
                .whereLessThan("saleDate", new Timestamp(startDate)).get();
        Task<QuerySnapshot> currentCustomersTask = db.collection("sales")
                .whereGreaterThanOrEqualTo("saleDate", new Timestamp(startDate))
                .whereLessThanOrEqualTo("saleDate", new Timestamp(endDate)).get();

        Tasks.whenAllSuccess(oldCustomersTask, currentCustomersTask).addOnSuccessListener(results -> {
            Set<String> oldCustomerIds = new HashSet<>();
            QuerySnapshot oldCustomersSnapshot = (QuerySnapshot) results.get(0);
            for (DocumentSnapshot doc : oldCustomersSnapshot.getDocuments()) {
                Sale sale = doc.toObject(Sale.class);
                if (sale != null && sale.getCustomerId() != null) {
                    oldCustomerIds.add(sale.getCustomerId());
                }
            }

            Set<String> currentCustomerIds = new HashSet<>();
            QuerySnapshot currentCustomersSnapshot = (QuerySnapshot) results.get(1);
            if (currentCustomersSnapshot.isEmpty()) {
                newVsReturningState.postValue(UiState.NO_DATA);
                newVsReturningData.postValue(new ArrayList<>());
                return;
            }
            for (DocumentSnapshot doc : currentCustomersSnapshot.getDocuments()) {
                Sale sale = doc.toObject(Sale.class);
                if (sale != null && sale.getCustomerId() != null) {
                    currentCustomerIds.add(sale.getCustomerId());
                }
            }

            int newCount = 0;
            int returningCount = 0;
            for (String customerId : currentCustomerIds) {
                if (oldCustomerIds.contains(customerId)) {
                    returningCount++;
                } else {
                    newCount++;
                }
            }

            List<PieEntry> entries = new ArrayList<>();
            if (newCount > 0)
                entries.add(new PieEntry(newCount, "New"));
            if (returningCount > 0)
                entries.add(new PieEntry(returningCount, "Returning"));

            newVsReturningData.postValue(entries);
            newVsReturningState.postValue(UiState.HAS_DATA);
        }).addOnFailureListener(e -> {
            newVsReturningState.postValue(UiState.NO_DATA);
            newVsReturningData.postValue(new ArrayList<>());
        });
    }

    // --- Process Methods ---

    private void processAverageOrderValue(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            averageOrderValueState.postValue(UiState.NO_DATA);
            averageOrderValueData.postValue(0.0);
            return;
        }
        double totalRevenue = 0;
        int saleCount = snapshots.size();
        for (DocumentSnapshot document : snapshots.getDocuments()) {
            Sale sale = document.toObject(Sale.class);
            if (sale != null) {
                totalRevenue += sale.getTotalAmount();
            }
        }
        averageOrderValueData.postValue(totalRevenue / saleCount);
        averageOrderValueState.postValue(UiState.HAS_DATA);
    }

    private void processTotalRevenue(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            totalRevenueState.postValue(UiState.NO_DATA);
            totalRevenueData.postValue(0.0);
            return;
        }
        double totalRevenue = 0;
        for (DocumentSnapshot document : snapshots.getDocuments()) {
            Sale sale = document.toObject(Sale.class);
            if (sale != null) {
                totalRevenue += sale.getTotalAmount();
            }
        }
        totalRevenueData.postValue(totalRevenue);
        totalRevenueState.postValue(UiState.HAS_DATA);
    }

    private void processTotalProfit(QuerySnapshot salesSnapshots) {
        if (salesSnapshots == null || salesSnapshots.isEmpty()) {
            totalProfitState.postValue(UiState.NO_DATA);
            totalProfitData.postValue(0.0);
            return;
        }

        double totalProfitValue = 0;
        for (DocumentSnapshot doc : salesSnapshots.getDocuments()) {
            try {
                Sale sale = doc.toObject(Sale.class);
                if (sale != null) {
                    Double profit = sale.getTotalProfit();
                    if (profit != null) {
                        totalProfitValue += profit;
                    }
                }
            } catch (Exception e) {
                // Skip corrupted documents
            }
        }
        totalProfitData.postValue(totalProfitValue);
        totalProfitState.postValue(UiState.HAS_DATA);
    }

    private void processTotalExpenses(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            totalExpensesState.postValue(UiState.NO_DATA);
            totalExpensesData.postValue(0.0);
            return;
        }
        double total = 0;
        for (DocumentSnapshot document : snapshots.getDocuments()) {
            try {
                Expense expense = document.toObject(Expense.class);
                if (expense != null) {
                    total += expense.getAmount();
                }
            } catch (Exception e) {
            }
        }
        totalExpensesData.postValue(total);
        totalExpensesState.postValue(UiState.HAS_DATA);
    }

    private void processTotalTransactions(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            totalTransactionsState.postValue(UiState.NO_DATA);
            totalTransactionsData.postValue(0);
            return;
        }
        totalTransactionsData.postValue(snapshots.size());
        totalTransactionsState.postValue(UiState.HAS_DATA);
    }

    private void processTotalInventoryValue(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            totalInventoryValueState.postValue(UiState.NO_DATA);
            totalInventoryValueData.postValue(0.0);
            return;
        }
        double totalValue = 0;
        for (DocumentSnapshot document : snapshots.getDocuments()) {
            Product product = document.toObject(Product.class);
            if (product != null) {
                totalValue += product.getQuantity() * product.getPurchasePrice();
            }
        }
        totalInventoryValueData.postValue(totalValue);
        totalInventoryValueState.postValue(UiState.HAS_DATA);
    }

    private void processTopProducts(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            topSellingProductsState.postValue(UiState.NO_DATA);
            topSellingProductsData.postValue(new ArrayList<>());
            topSellingProductsLabels.postValue(new ArrayList<>());
            return;
        }
        Map<String, Integer> productQuantities = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();
        for (DocumentSnapshot document : snapshots.getDocuments()) {
            try {
                Sale sale = document.toObject(Sale.class);
                if (sale != null && sale.getItems() != null) {
                    for (SaleItem item : sale.getItems()) {
                        String id = item.getProductId();
                        if (id != null) {
                            productQuantities.put(id, productQuantities.getOrDefault(id, 0) + item.getQuantity());
                            if (item.getProductName() != null) {
                                productNames.put(id, item.getProductName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Skip corrupted documents
            }
        }

        List<Map.Entry<String, Integer>> sortedProducts = new ArrayList<>(productQuantities.entrySet());
        sortedProducts.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        int limit = Math.min(sortedProducts.size(), 10);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            String id = sortedProducts.get(i).getKey();
            int quantity = sortedProducts.get(i).getValue();
            String name = productNames.getOrDefault(id, "Unknown Product");
            entries.add(new BarEntry(limit - 1 - i, quantity));
            labels.add(name);
        }
        Collections.reverse(labels);
        topSellingProductsData.postValue(entries);
        topSellingProductsLabels.postValue(labels);
        topSellingProductsState.postValue(UiState.HAS_DATA);
    }

    private void processTopPurchasedProducts(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            topPurchasedProductsState.postValue(UiState.NO_DATA);
            topPurchasedProductsData.postValue(new ArrayList<>());
            topPurchasedProductsLabels.postValue(new ArrayList<>());
            return;
        }
        Map<String, Integer> productQuantities = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();
        for (DocumentSnapshot document : snapshots.getDocuments()) {
            try {
                Purchase purchase = document.toObject(Purchase.class);
                if (purchase != null && purchase.getItems() != null) {
                    for (PurchaseItem item : purchase.getItems()) {
                        String id = item.getProductId();
                        if (id != null) {
                            productQuantities.put(id, productQuantities.getOrDefault(id, 0) + item.getQuantity());
                            if (item.getProductName() != null) {
                                productNames.put(id, item.getProductName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Skip corrupted documents
            }
        }

        List<Map.Entry<String, Integer>> sortedProducts = new ArrayList<>(productQuantities.entrySet());
        sortedProducts.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        int limit = Math.min(sortedProducts.size(), 10);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            String id = sortedProducts.get(i).getKey();
            int quantity = sortedProducts.get(i).getValue();
            String name = productNames.getOrDefault(id, "Unknown Product");
            entries.add(new BarEntry(limit - 1 - i, (float) quantity));
            labels.add(name);
        }
        Collections.reverse(labels);
        topPurchasedProductsData.postValue(entries);
        topPurchasedProductsLabels.postValue(labels);
        topPurchasedProductsState.postValue(UiState.HAS_DATA);
    }

    private void processProductProfitability(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            mostProfitableProductsState.postValue(UiState.NO_DATA);
            mostProfitableProductsData.postValue(new ArrayList<>());
            mostProfitableProductsLabels.postValue(new ArrayList<>());
            return;
        }

        Map<String, Double> productProfits = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();

        for (DocumentSnapshot document : snapshots.getDocuments()) {
            try {
                Sale sale = document.toObject(Sale.class);
                if (sale != null && sale.getItems() != null) {
                    for (SaleItem item : sale.getItems()) {
                        String id = item.getProductId();
                        if (id != null) {
                            double revenue = item.getTotalPrice();
                            double cost = item.getCostPrice() * item.getQuantity();
                            double profit = revenue - cost;

                            productProfits.put(id, productProfits.getOrDefault(id, 0.0) + profit);
                            if (item.getProductName() != null) {
                                productNames.put(id, item.getProductName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Skip corrupted documents
            }
        }

        if (productProfits.isEmpty()) {
            mostProfitableProductsState.postValue(UiState.NO_DATA);
            mostProfitableProductsData.postValue(new ArrayList<>());
            mostProfitableProductsLabels.postValue(new ArrayList<>());
            return;
        }

        List<Map.Entry<String, Double>> sortedProfits = new ArrayList<>(productProfits.entrySet());
        sortedProfits.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        int limit = Math.min(sortedProfits.size(), 10);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> entry = sortedProfits.get(i);
            entries.add(new BarEntry(limit - 1 - i, entry.getValue().floatValue()));
            labels.add(productNames.getOrDefault(entry.getKey(), "Unknown Product"));
        }
        Collections.reverse(labels);
        mostProfitableProductsData.postValue(entries);
        mostProfitableProductsLabels.postValue(labels);
        mostProfitableProductsState.postValue(UiState.HAS_DATA);
    }

    private void processLowStockProducts(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            lowStockProductsState.postValue(UiState.NO_DATA);
            lowStockProductsData.postValue(new ArrayList<>());
            return;
        }
        List<Product> products = new ArrayList<>();
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            try {
                Product p = doc.toObject(Product.class);
                if (p != null) {
                    p.setDocumentId(doc.getId());
                    products.add(p);
                }
            } catch (Exception e) {
                /* Skip */}
        }
        lowStockProductsData.postValue(products);
        lowStockProductsState.postValue(UiState.HAS_DATA);
    }

    private void processPurchaseOrdersOverTime(QuerySnapshot snapshots, Date startDate) {
        if (snapshots == null || snapshots.isEmpty()) {
            purchaseOrdersOverTimeState.postValue(UiState.NO_DATA);
            purchaseOrdersOverTimeData.postValue(new ArrayList<>());
            return;
        }
        Map<Long, Double> purchasesByTime = new HashMap<>();
        long startMillis = startDate.getTime();
        long divisor = getGranularityDivisor();
        for (DocumentSnapshot document : snapshots.getDocuments()) {
            Purchase purchase = document.toObject(Purchase.class);
            if (purchase != null && purchase.getPurchaseDate() != null) {
                long purchaseMillis = purchase.getPurchaseDate().toDate().getTime();
                long timeIndex = (purchaseMillis - startMillis) / divisor;
                purchasesByTime.put(timeIndex,
                        purchasesByTime.getOrDefault(timeIndex, 0.0) + purchase.getTotalAmount());
            }
        }
        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<Long, Double> mapEntry : purchasesByTime.entrySet()) {
            entries.add(new Entry(mapEntry.getKey().floatValue(), mapEntry.getValue().floatValue()));
        }
        Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));
        purchaseOrdersOverTimeData.postValue(entries);
        purchaseOrdersOverTimeState.postValue(UiState.HAS_DATA);
    }

    private void processPurchaseOrdersOverTimeVolume(QuerySnapshot snapshots, Date startDate) {
        if (snapshots == null || snapshots.isEmpty()) {
            purchaseOrdersOverTimeVolumeState.postValue(UiState.NO_DATA);
            purchaseOrdersOverTimeVolumeData.postValue(new ArrayList<>());
            return;
        }
        Map<Long, Integer> purchasesByTime = new HashMap<>();
        long startMillis = startDate.getTime();
        long divisor = getGranularityDivisor();
        for (DocumentSnapshot document : snapshots.getDocuments()) {
            Purchase purchase = document.toObject(Purchase.class);
            if (purchase != null && purchase.getPurchaseDate() != null) {
                long purchaseMillis = purchase.getPurchaseDate().toDate().getTime();
                long timeIndex = (purchaseMillis - startMillis) / divisor;
                purchasesByTime.put(timeIndex, purchasesByTime.getOrDefault(timeIndex, 0) + 1);
            }
        }
        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<Long, Integer> mapEntry : purchasesByTime.entrySet()) {
            entries.add(new Entry(mapEntry.getKey().floatValue(), mapEntry.getValue().floatValue()));
        }
        Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));
        purchaseOrdersOverTimeVolumeData.postValue(entries);
        purchaseOrdersOverTimeVolumeState.postValue(UiState.HAS_DATA);
    }

    private void processTopCustomers(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            topCustomersState.postValue(UiState.NO_DATA);
            topCustomersData.postValue(new ArrayList<>());
            topCustomersLabels.postValue(new ArrayList<>());
            return;
        }
        Map<String, Double> customerSpending = new HashMap<>();
        Map<String, String> customerNames = new HashMap<>();
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            try {
                Sale sale = doc.toObject(Sale.class);
                if (sale != null && sale.getCustomerId() != null) {
                    customerSpending.put(sale.getCustomerId(),
                            customerSpending.getOrDefault(sale.getCustomerId(), 0.0) + sale.getTotalAmount());
                    if (sale.getCustomerName() != null) {
                        customerNames.put(sale.getCustomerId(), sale.getCustomerName());
                    }
                }
            } catch (Exception e) {
                /* Skip */}
        }
        List<Map.Entry<String, Double>> sortedCustomers = new ArrayList<>(customerSpending.entrySet());
        sortedCustomers.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        int limit = Math.min(sortedCustomers.size(), 10);
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> entry = sortedCustomers.get(i);
            entries.add(new BarEntry(limit - 1 - i, entry.getValue().floatValue()));
            labels.add(customerNames.getOrDefault(entry.getKey(), "Unknown Customer"));
        }
        Collections.reverse(labels);
        topCustomersData.postValue(entries);
        topCustomersLabels.postValue(labels);
        topCustomersState.postValue(UiState.HAS_DATA);
    }

    private void processSalesByCategory(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            salesByCategoryState.postValue(UiState.NO_DATA);
            salesByCategoryData.postValue(new ArrayList<>());
            return;
        }

        Map<String, Double> categoryRevenue = new HashMap<>();
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            try {
                Sale sale = doc.toObject(Sale.class);
                if (sale != null && sale.getItems() != null) {
                    for (SaleItem item : sale.getItems()) {
                        String category = item.getCategory();
                        if (category == null || category.isEmpty()) {
                            category = "Uncategorized";
                        }
                        categoryRevenue.put(category,
                                categoryRevenue.getOrDefault(category, 0.0) + item.getTotalPrice());
                    }
                }
            } catch (Exception e) {
                // Skip corrupted documents
            }
        }

        if (categoryRevenue.isEmpty()) {
            salesByCategoryState.postValue(UiState.NO_DATA);
            salesByCategoryData.postValue(new ArrayList<>());
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryRevenue.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        salesByCategoryData.postValue(entries);
        salesByCategoryState.postValue(UiState.HAS_DATA);
    }

    private void processTotalSpendBySupplier(QuerySnapshot snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            totalSpendBySupplierState.postValue(UiState.NO_DATA);
            totalSpendBySupplierData.postValue(new ArrayList<>());
            totalSpendBySupplierLabels.postValue(new ArrayList<>());
            return;
        }

        Map<String, Double> SupplierSpending = new HashMap<>();
        Map<String, String> SupplierNames = new HashMap<>();

        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            try {
                Purchase purchase = doc.toObject(Purchase.class);
                if (purchase != null && purchase.getSupplierId() != null) {
                    SupplierSpending.put(purchase.getSupplierId(),
                            SupplierSpending.getOrDefault(purchase.getSupplierId(), 0.0) + purchase.getTotalAmount());
                    if (purchase.getSupplierName() != null) {
                        SupplierNames.put(purchase.getSupplierId(), purchase.getSupplierName());
                    }
                }
            } catch (Exception e) {
                /* Skip */}
        }

        List<Map.Entry<String, Double>> sortedSuppliers = new ArrayList<>(SupplierSpending.entrySet());
        sortedSuppliers.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        int limit = Math.min(sortedSuppliers.size(), 10);
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> entry = sortedSuppliers.get(i);
            String name = SupplierNames.getOrDefault(entry.getKey(), "Unknown Supplier");
            entries.add(new BarEntry(limit - 1 - i, entry.getValue().floatValue()));
            labels.add(name);
        }
        Collections.reverse(labels);

        totalSpendBySupplierData.postValue(entries);
        totalSpendBySupplierLabels.postValue(labels);
        totalSpendBySupplierState.postValue(UiState.HAS_DATA);
    }

    static class ProfitInfo {
        int totalQuantity = 0;
        double totalRevenue = 0.0;
    }
}
