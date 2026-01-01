package com.bsoft.inventorymanager.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;
import com.bsoft.inventorymanager.R;
import com.bsoft.inventorymanager.roles.CurrentUser;
import com.bsoft.inventorymanager.roles.Employee;
import com.bsoft.inventorymanager.viewmodels.MainViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.preloadData();

        observeDataAndNavigate();
    }

    private void observeDataAndNavigate() {
        mainViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && !isLoading) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    fetchEmployeeAndProceed(currentUser);
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
    }

    private void fetchEmployeeAndProceed(FirebaseUser firebaseUser) {
        FirebaseFirestore.getInstance().collection("employees").document(firebaseUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Employee employee = task.getResult().toObject(Employee.class);
                        CurrentUser.getInstance().setEmployee(employee);

                        // Update FCM Token
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(tokenTask -> {
                                    if (tokenTask.isSuccessful()) {
                                        String token = tokenTask.getResult();
                                        new com.bsoft.inventorymanager.roles.RolesRepository()
                                                .updateFCMToken(firebaseUser.getUid(), token);
                                    }
                                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                    finish();
                                });
                    } else {
                        // If employee data is not found, sign out and go to login
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }
}
