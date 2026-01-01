package com.bsoft.inventorymanager.utils;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PaginationHelper {

    private static final String TAG = "PaginationHelper";
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Fetches paginated data from Firestore
     * 
     * @param collectionPath The Firestore collection path
     * @param lastDocument   The last document from the previous page (null for
     *                       first page)
     * @param pageSize       Number of items per page
     * @param callback       Callback to handle the results
     */
    /**
     * Fetches paginated data from Firestore with custom sorting
     * 
     * @param collectionPath The Firestore collection path
     * @param lastDocument   The last document from the previous page (null for
     *                       first page)
     * @param pageSize       Number of items per page
     * @param orderByField   The field to sort by
     * @param direction      The sort direction (ASCENDING or DESCENDING)
     * @param callback       Callback to handle the results
     */
    public static void fetchPaginatedData(Query query, DocumentSnapshot lastDocument,
            int pageSize, PaginationCallback callback) {
        Query paginatedQuery = query.limit(pageSize);

        if (lastDocument != null) {
            paginatedQuery = paginatedQuery.startAfter(lastDocument);
        }

        paginatedQuery.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> documents = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        documents.add(document);
                    }

                    boolean hasMore = querySnapshot.size() == pageSize;
                    callback.onSuccess(documents, hasMore);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching paginated data", e);
                    callback.onError(e);
                });
    }

    public static void fetchPaginatedData(String collectionPath, DocumentSnapshot lastDocument,
            int pageSize, String orderByField, Query.Direction direction,
            PaginationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection(collectionPath).orderBy(orderByField, direction);
        fetchPaginatedData(query, lastDocument, pageSize, callback);
    }

    /**
     * Fetches paginated data sorted by name ASC (Backward Compatibility)
     */
    public static void fetchPaginatedData(String collectionPath, DocumentSnapshot lastDocument,
            int pageSize, PaginationCallback callback) {
        fetchPaginatedData(collectionPath, lastDocument, pageSize, "name", Query.Direction.ASCENDING, callback);
    }

    /**
     * Fetches paginated data with default page size and name sorting
     */
    public static void fetchPaginatedData(String collectionPath, DocumentSnapshot lastDocument,
            PaginationCallback callback) {
        fetchPaginatedData(collectionPath, lastDocument, DEFAULT_PAGE_SIZE, "name", Query.Direction.ASCENDING,
                callback);
    }

    /**
     * Callback interface for pagination results
     */
    public interface PaginationCallback {
        void onSuccess(List<DocumentSnapshot> documents, boolean hasMore);

        void onError(Exception e);
    }
}