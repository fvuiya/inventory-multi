package com.bsoft.inventorymanager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.activities.SelectSaleToReturnActivity;
import com.bsoft.inventorymanager.adapters.SalesReturnHistoryAdapter;
import com.bsoft.inventorymanager.models.SaleReturn;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class SalesReturnFragment extends Fragment {

    private RecyclerView recyclerView;
    private SalesReturnHistoryAdapter adapter;
    private final List<SaleReturn> saleReturnsList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales_return, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_sales_return);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SalesReturnHistoryAdapter(saleReturnsList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_sales_return);
        fab.setOnClickListener(v -> {
            // Direct flow: Open SelectSaleToReturnActivity which will show all sales
            Intent intent = new Intent(getActivity(), SelectSaleToReturnActivity.class);
            startActivity(intent);
        });

        loadSalesReturns();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSalesReturns(); // Refresh list when returning
    }

    private void loadSalesReturns() {
        db.collection("sales_returns")
                .orderBy("returnDate", Query.Direction.DESCENDING)
                .limit(50) // Limit to recent 50 returns
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    saleReturnsList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                            SaleReturn saleReturn = document.toObject(SaleReturn.class);
                            if (saleReturn != null) {
                                saleReturn.setDocumentId(document.getId());
                                saleReturnsList.add(saleReturn);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading returns: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}
