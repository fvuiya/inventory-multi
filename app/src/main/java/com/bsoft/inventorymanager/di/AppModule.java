package com.bsoft.inventorymanager.di;

import com.bsoft.inventorymanager.network.ProductApiService;
import com.bsoft.inventorymanager.network.RetrofitClient;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public FirebaseFirestore provideFirebaseFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @Singleton
    public com.google.firebase.functions.FirebaseFunctions provideFirebaseFunctions() {
        return com.google.firebase.functions.FirebaseFunctions.getInstance();
    }

    @Provides
    @Singleton
    public ProductApiService provideProductApiService() {
        return RetrofitClient.getClient().create(ProductApiService.class);
    }

    @Provides
    @Singleton
    public com.google.firebase.auth.FirebaseAuth provideFirebaseAuth() {
        return com.google.firebase.auth.FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repositories.SaleRepository provideSaleRepository(
            com.bsoft.inventorymanager.repositories.SaleRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repositories.PurchaseRepository providePurchaseRepository(
            com.bsoft.inventorymanager.repositories.PurchaseRepositoryImpl impl) {
        return impl;
    }

    // [KMP MIGRATION] Shared ProductRepository
    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repository.ProductRepository provideSharedProductRepository(
            com.bsoft.inventorymanager.repository.ProductRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repository.ProductRepositoryImpl provideSharedProductRepositoryImpl(
            FirebaseFirestore db) {
        return new com.bsoft.inventorymanager.repository.ProductRepositoryImpl(db);
    }

    // [LEGACY] Kept for backward compatibility with SelectProductViewModel - TODO:
    // Migrate
    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repositories.ProductRepository provideLegacyProductRepository(
            com.bsoft.inventorymanager.repositories.ProductRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repositories.ExpenseRepository provideExpenseRepository(
            com.bsoft.inventorymanager.repositories.ExpenseRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repository.SupplierRepository provideSupplierRepository(
            com.bsoft.inventorymanager.repository.SupplierRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repository.SupplierRepositoryImpl provideSupplierRepositoryImpl(
            FirebaseFirestore db) {
        return new com.bsoft.inventorymanager.repository.SupplierRepositoryImpl(db);
    }

    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repository.CustomerRepository provideCustomerRepository(
            com.bsoft.inventorymanager.repository.CustomerRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public com.bsoft.inventorymanager.repository.CustomerRepositoryImpl provideCustomerRepositoryImpl(
            FirebaseFirestore db) {
        return new com.bsoft.inventorymanager.repository.CustomerRepositoryImpl(db);
    }
}
