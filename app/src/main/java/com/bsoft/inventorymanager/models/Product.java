package com.bsoft.inventorymanager.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import java.io.Serializable; // Add this import

public class Product implements Parcelable, Serializable { // Add Serializable here

    public static final String FIELD_DOCUMENT_ID = "documentId";

    private String documentId;
    private String name;
    private String imageUrl;
    private String brand;
    private String category;
    private String productCode;
    private String barcode;
    private int quantity; // Available stock
    private int minStockLevel; // Minimum stock level for alerts
    private String unit;
    private double costPrice; // Cost price for profit calculations
    private double purchasePrice; // Price we buy at
    private double mrp;
    private double wholesalePrice;
    private double dealerPrice;
    private String supplierId; // Primary supplier
    private String supplierName;
    private Timestamp expiryDate; // Expiry date for perishable goods
    private String batchNumber; // Batch/lot number
    private int quantityToSell; // Quantity selected for the current sale transaction
    private double sellingPrice; // Actual price it's being sold at in this transaction

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    public Product(String name, String imageUrl, String brand, String category, String productCode,
                   int quantity, String unit, double costPrice, double purchasePrice, double mrp,
                   double wholesalePrice, double dealerPrice) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.category = category;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unit = unit;
        this.costPrice = costPrice;
        this.purchasePrice = purchasePrice;
        this.mrp = mrp;
        this.wholesalePrice = wholesalePrice;
        this.dealerPrice = dealerPrice;
        this.quantityToSell = 0; // Default for a new product instance not yet in a sale list
        this.sellingPrice = mrp; // Default selling price to MRP
        this.minStockLevel = 0; // Default minimum stock level
    }

    protected Product(Parcel in) {
        documentId = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        brand = in.readString();
        category = in.readString();
        productCode = in.readString();
        barcode = in.readString();
        quantity = in.readInt();
        minStockLevel = in.readInt();
        unit = in.readString();
        costPrice = in.readDouble();
        purchasePrice = in.readDouble();
        mrp = in.readDouble();
        wholesalePrice = in.readDouble();
        dealerPrice = in.readDouble();
        supplierId = in.readString();
        supplierName = in.readString();
        expiryDate = (Timestamp) in.readValue(Timestamp.class.getClassLoader());
        batchNumber = in.readString();
        quantityToSell = in.readInt();
        sellingPrice = in.readDouble(); // <<< READ sellingPrice
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentId);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(brand);
        dest.writeString(category);
        dest.writeString(productCode);
        dest.writeString(barcode);
        dest.writeInt(quantity);
        dest.writeInt(minStockLevel);
        dest.writeString(unit);
        dest.writeDouble(costPrice);
        dest.writeDouble(purchasePrice);
        dest.writeDouble(mrp);
        dest.writeDouble(wholesalePrice);
        dest.writeDouble(dealerPrice);
        dest.writeString(supplierId);
        dest.writeString(supplierName);
        dest.writeValue(expiryDate);
        dest.writeString(batchNumber);
        dest.writeInt(quantityToSell);
        dest.writeDouble(sellingPrice); // <<< WRITE sellingPrice
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getMrp() {
        return mrp;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
    }

    public double getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(double wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public double getDealerPrice() {
        return dealerPrice;
    }

    public void setDealerPrice(double dealerPrice) {
        this.dealerPrice = dealerPrice;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Timestamp getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Timestamp expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public int getQuantityToSell() {
        return quantityToSell;
    }

    public void setQuantityToSell(int quantityToSell) {
        this.quantityToSell = quantityToSell;
    }

    public double getSellingPrice() { // <<< GETTER for sellingPrice
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) { // <<< SETTER for sellingPrice
        this.sellingPrice = sellingPrice;
    }
}
