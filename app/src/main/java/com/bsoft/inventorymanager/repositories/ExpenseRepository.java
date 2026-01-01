package com.bsoft.inventorymanager.repositories;

import androidx.lifecycle.LiveData;
import com.bsoft.inventorymanager.models.Expense;
import java.util.List;

public interface ExpenseRepository {
    interface ExpenseCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    LiveData<List<Expense>> getExpenses();

    LiveData<Boolean> getIsLoading();

    void loadNextPageExpenses();

    void resetPagination();

    void preloadExpenses();

    void saveExpense(Expense expense, ExpenseCallback callback);

    void deleteExpense(String expenseId, ExpenseCallback callback);
}
