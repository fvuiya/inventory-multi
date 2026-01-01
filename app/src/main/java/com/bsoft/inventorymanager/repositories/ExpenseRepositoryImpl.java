package com.bsoft.inventorymanager.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bsoft.inventorymanager.models.Expense;
import com.bsoft.inventorymanager.utils.PaginationHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExpenseRepositoryImpl implements ExpenseRepository {

    private final FirebaseFirestore db;
    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private DocumentSnapshot lastVisibleExpense;
    private boolean isLastPageExpenses = false;

    @Inject
    public ExpenseRepositoryImpl(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public LiveData<List<Expense>> getExpenses() {
        return expenses;
    }

    @Override
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @Override
    public void loadNextPageExpenses() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || isLastPageExpenses)
            return;

        isLoading.setValue(true);
        PaginationHelper.fetchPaginatedData("expenses", lastVisibleExpense, 20, "date",
                Query.Direction.DESCENDING, new PaginationHelper.PaginationCallback() {
                    @Override
                    public void onSuccess(List<DocumentSnapshot> documents, boolean hasMore) {
                        if (!documents.isEmpty()) {
                            lastVisibleExpense = documents.get(documents.size() - 1);
                            List<Expense> newItems = new ArrayList<>();
                            for (DocumentSnapshot doc : documents) {
                                Expense item = doc.toObject(Expense.class);
                                if (item != null) {
                                    item.setDocumentId(doc.getId());
                                    newItems.add(item);
                                }
                            }
                            appendToList(expenses, newItems);
                        }
                        isLastPageExpenses = !hasMore;
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onError(Exception e) {
                        isLoading.setValue(false);
                    }
                });
    }

    @Override
    public void resetPagination() {
        expenses.setValue(new ArrayList<>());
        lastVisibleExpense = null;
        isLastPageExpenses = false;
    }

    @Override
    public void preloadExpenses() {
        if (expenses.getValue() != null && !expenses.getValue().isEmpty())
            return;
        resetPagination();
        loadNextPageExpenses();
    }

    @Override
    public void saveExpense(Expense expense, ExpenseCallback callback) {
        isLoading.setValue(true);
        if (expense.getDocumentId() == null || expense.getDocumentId().isEmpty()) {
            db.collection("expenses")
                    .add(expense)
                    .addOnSuccessListener(docRef -> {
                        isLoading.setValue(false);
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        callback.onFailure(e);
                    });
        } else {
            db.collection("expenses").document(expense.getDocumentId())
                    .set(expense)
                    .addOnSuccessListener(aVoid -> {
                        isLoading.setValue(false);
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        callback.onFailure(e);
                    });
        }
    }

    @Override
    public void deleteExpense(String expenseId, ExpenseCallback callback) {
        isLoading.setValue(true);
        db.collection("expenses").document(expenseId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    callback.onFailure(e);
                });
    }

    private <T> void appendToList(MutableLiveData<List<T>> liveData, List<T> newItems) {
        List<T> currentList = liveData.getValue();
        if (currentList == null)
            currentList = new ArrayList<>();
        currentList.addAll(newItems);
        liveData.setValue(new ArrayList<>(currentList)); // Use new list to trigger observers correctly
    }
}
