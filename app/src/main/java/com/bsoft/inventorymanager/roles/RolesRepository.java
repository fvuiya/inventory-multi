package com.bsoft.inventorymanager.roles;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.Executor;

public class RolesRepository {

    private final FirebaseAuth mAuth;
    private final CollectionReference employeesCollection;
    private final CollectionReference salesCollection;
    private final CollectionReference purchasesCollection;

    public RolesRepository() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        employeesCollection = db.collection("employees");
        salesCollection = db.collection("sales");
        purchasesCollection = db.collection("purchases");
    }

    public ListenerRegistration loadEmployees(Executor executor,
            com.google.firebase.firestore.EventListener<QuerySnapshot> listener) {
        return employeesCollection.whereEqualTo("isActive", true).addSnapshotListener(executor, listener);
    }

    public DocumentReference getEmployee(String employeeId) {
        return employeesCollection.document(employeeId);
    }

    public Query getSalesByEmployee(String employeeId) {
        return salesCollection.whereEqualTo("userId", employeeId).orderBy("saleDate", Query.Direction.DESCENDING)
                .limit(10);
    }

    public Query getPurchasesByEmployee(String employeeId) {
        return purchasesCollection.whereEqualTo("userId", employeeId)
                .orderBy("purchaseDate", Query.Direction.DESCENDING).limit(10);
    }

    public Task<AuthResult> createUser(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> saveEmployee(String uid, Employee employee) {
        return employeesCollection.document(uid).set(employee);
    }

    public Task<Void> updateEmployee(Employee employee) {
        return employeesCollection.document(employee.getDocumentId()).set(employee);
    }

    public Task<Void> deleteEmployee(Employee employee) {
        DocumentReference employeeRef = employeesCollection.document(employee.getDocumentId());
        return employeeRef.update("isActive", false);
    }

    public Task<Void> updateFCMToken(String employeeId, String token) {
        if (employeeId == null || token == null) {
            return null;
        }
        return employeesCollection.document(employeeId).update("fcmToken", token);
    }
}
