package com.bsoft.inventorymanager.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.adapters.PassiveIncomeAdapter;
import com.bsoft.inventorymanager.models.PassiveIncome;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class PassiveIncomeActivity extends AppCompatActivity {

    private PassiveIncomeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passive_income);

        Toolbar toolbar = findViewById(R.id.toolbar_passive_income);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_passive_income);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PassiveIncomeAdapter();
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_passive_income);
        fab.setOnClickListener(v -> showAddPassiveIncomeDialog());

        loadPassiveIncomes();
    }

    private void showAddPassiveIncomeDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_add_passive_income, null);
        final EditText sourceEditText = view.findViewById(R.id.et_source);
        final EditText noteEditText = view.findViewById(R.id.et_note);
        final EditText amountEditText = view.findViewById(R.id.et_amount);

        new AlertDialog.Builder(this)
                .setTitle("Add Passive Income")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String source = sourceEditText.getText().toString();
                    String note = noteEditText.getText().toString();
                    double amount = Double.parseDouble(amountEditText.getText().toString());

                    PassiveIncome income = new PassiveIncome(source, note, amount, Timestamp.now());
                    FirebaseFirestore.getInstance().collection("passive_incomes").add(income);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadPassiveIncomes() {
        CollectionReference passiveIncomesRef = FirebaseFirestore.getInstance().collection("passive_incomes");
        passiveIncomesRef.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
            }
            adapter.submitList(snapshots.toObjects(PassiveIncome.class));
        });
    }
}
