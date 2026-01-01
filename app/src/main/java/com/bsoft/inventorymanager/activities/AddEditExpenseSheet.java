package com.bsoft.inventorymanager.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.models.Expense;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.Locale;

public class AddEditExpenseSheet extends BottomSheetDialogFragment {

    public interface OnExpenseSavedListener {
        void onExpenseSaved(Expense expense);
    }

    private OnExpenseSavedListener mListener;
    private Expense currentExpense;

    private TextInputEditText etDescription, etAmount, etCategory;
    private TextView tvTitle;

    public static AddEditExpenseSheet newInstance(Expense expense) {
        AddEditExpenseSheet fragment = new AddEditExpenseSheet();
        Bundle args = new Bundle();
        if (expense != null) {
            args.putParcelable("ARG_EXPENSE", expense);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnExpenseSavedListener) {
            mListener = (OnExpenseSavedListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentExpense = getArguments().getParcelable("ARG_EXPENSE");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_edit_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tv_title);
        etDescription = view.findViewById(R.id.et_expense_description);
        etAmount = view.findViewById(R.id.et_expense_amount);
        etCategory = view.findViewById(R.id.et_expense_category);
        Button btnSave = view.findViewById(R.id.btn_save_expense);
        Button btnCancel = view.findViewById(R.id.btn_cancel_expense);

        if (currentExpense != null) {
            tvTitle.setText("Edit Expense");
            etDescription.setText(currentExpense.getDescription());
            etAmount.setText(String.format(Locale.US, "%.2f", currentExpense.getAmount()));
            etCategory.setText(currentExpense.getCategory());
            btnSave.setText("Save Changes");
        }

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String desc = etDescription.getText().toString().trim();
            String amtStr = etAmount.getText().toString().trim();
            String cat = etCategory.getText().toString().trim();

            if (desc.isEmpty() || amtStr.isEmpty()) {
                Toast.makeText(getContext(), "Description and Amount are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amtStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentExpense == null) {
                currentExpense = new Expense();
                currentExpense.setDate(new Timestamp(new Date()));
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    currentExpense.setUserId(auth.getCurrentUser().getUid());
                }
            }

            currentExpense.setDescription(desc);
            currentExpense.setAmount(amount);
            currentExpense.setCategory(cat.isEmpty() ? "General" : cat);

            if (mListener != null) {
                mListener.onExpenseSaved(currentExpense);
            }
            dismiss();
        });
    }

    public void setListener(OnExpenseSavedListener listener) {
        this.mListener = listener;
    }
}
