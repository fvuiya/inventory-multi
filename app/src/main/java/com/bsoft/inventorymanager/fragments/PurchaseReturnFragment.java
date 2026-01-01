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
import com.bsoft.inventorymanager.activities.SelectPurchaseToReturnActivity;
import com.bsoft.inventorymanager.adapters.PurchaseReturnHistoryAdapter;
import com.bsoft.inventorymanager.models.PurchaseReturn;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class PurchaseReturnFragment extends Fragment {

    private RecyclerView recyclerView;
    private PurchaseReturnHistoryAdapter adapter;
    private final List<PurchaseReturn> purchaseReturnsList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_return, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_purchase_return);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PurchaseReturnHistoryAdapter(purchaseReturnsList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_purchase_return);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SelectPurchaseToReturnActivity.class);
            startActivity(intent);
        });

        loadPurchaseReturns();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPurchaseReturns(); // Refresh list when returning
    }

    private void loadPurchaseReturns() {
        db.collection("purchase_returns")
                .orderBy("returnDate", Query.Direction.DESCENDING)
                .limit(50) // Limit to recent 50 returns
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    purchaseReturnsList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                            PurchaseReturn purchaseReturn = document.toObject(PurchaseReturn.class);
                            if (purchaseReturn != null) {
                                purchaseReturn.setDocumentId(document.getId());
                                purchaseReturnsList.add(purchaseReturn);
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
