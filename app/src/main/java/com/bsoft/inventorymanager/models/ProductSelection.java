package com.bsoft.inventorymanager.models;

import java.util.Objects;

public class ProductSelection {
    private Product product;
    private int quantityInSale;
    private int maxReturnableQuantity = -1; // -1 indicates no limit (normal sale/purchase)

    public ProductSelection(Product product, int quantityInSale) {
        this.product = product;
        this.quantityInSale = (quantityInSale > 0) ? quantityInSale : 1;
        if (this.product != null && this.product.getSellingPrice() == 0 && this.product.getMrp() > 0) {
            this.product.setSellingPrice(this.product.getMrp());
        }
    }

    public void setMaxReturnableQuantity(int max) {
        this.maxReturnableQuantity = max;
    }

    public int getMaxReturnableQuantity() {
        return this.maxReturnableQuantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantityInSale() {
        return quantityInSale;
    }

    public void setQuantityInSale(int quantityInSale) {
        this.quantityInSale = (quantityInSale > 0) ? quantityInSale : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProductSelection that = (ProductSelection) o;
        return Objects.equals(product != null ? product.getDocumentId() : null,
                that.product != null ? that.product.getDocumentId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product != null ? product.getDocumentId() : null);
    }
}
