package com.bsoft.inventorymanager.activities;

import android.content.Intent;
import android.os.Bundle;
import com.bsoft.inventorymanager.R;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bsoft.inventorymanager.roles.CurrentUser;
import com.bsoft.inventorymanager.roles.Employee;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView textViewStatus, textViewForgotPassword;

    @Inject
    FirebaseAuth mAuth;
    @Inject
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // mAuth and db are injected

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        buttonLogin.setOnClickListener(v -> loginUser());
        textViewForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            textViewStatus.setText("Please enter email and password.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);
        textViewStatus.setText("");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    progressBar.setVisibility(View.GONE);
                    buttonLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        fetchEmployeeAndProceed(task.getResult().getUser());
                    } else {
                        textViewStatus.setText("Authentication failed: "
                                + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchEmployeeAndProceed(FirebaseUser firebaseUser) {
        db.collection("employees").document(firebaseUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Employee employee = document.toObject(Employee.class);
                            CurrentUser.getInstance().setEmployee(employee);

                            // Update FCM Token
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String token = tokenTask.getResult();
                                            new com.bsoft.inventorymanager.roles.RolesRepository()
                                                    .updateFCMToken(firebaseUser.getUid(), token);
                                        }
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this, "Employee data not found.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error fetching employee data.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        final EditText emailInput = new EditText(this);
        emailInput.setHint("Enter your email");

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(emailInput)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(email)) {
                        sendPasswordResetEmail(email);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent to " + email, Toast.LENGTH_LONG)
                                .show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }
}
