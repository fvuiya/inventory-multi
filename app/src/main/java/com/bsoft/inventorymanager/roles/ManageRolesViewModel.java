package com.bsoft.inventorymanager.roles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ManageRolesViewModel extends ViewModel {

    private final RolesRepository repository;
    private final MutableLiveData<List<Employee>> employees = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private Map<String, Permission> currentPermissions = new HashMap<>();
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    public ManageRolesViewModel() {
        repository = new RolesRepository();
        repository.loadEmployees(backgroundExecutor, (value, error) -> {
            if (error != null) {
                toastMessage.postValue("Error fetching employees: " + error.getMessage());
                return;
            }
            if (value != null) {
                List<Employee> employeeList = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : value) {
                    Employee employee = document.toObject(Employee.class);
                    employee.setDocumentId(document.getId());
                    employeeList.add(employee);
                }
                employees.postValue(employeeList);
            }
        });
    }

    public LiveData<List<Employee>> getEmployees() {
        return employees;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void saveEmployee(String name, String email, String password, String photo, double salary, String phone, String address, int age, String designation, Employee existingEmployee) {
        if (name.isEmpty() || email.isEmpty()) {
            toastMessage.setValue("Name and email are required.");
            return;
        }

        if (existingEmployee == null) { // Create new employee
            if (password.isEmpty()) {
                toastMessage.setValue("Password is required for new employees.");
                return;
            }
            repository.createUser(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = task.getResult().getUser().getUid();
                    Employee newEmployee = new Employee(name, email, currentPermissions);
                    newEmployee.setDocumentId(uid);
                    newEmployee.setPhoto(photo);
                    newEmployee.setSalary(salary);
                    newEmployee.setPhone(phone);
                    newEmployee.setAddress(address);
                    newEmployee.setAge(age);
                    newEmployee.setDesignation(designation);
                    repository.saveEmployee(uid, newEmployee)
                            .addOnSuccessListener(aVoid -> toastMessage.postValue("Employee added successfully!"))
                            .addOnFailureListener(e -> toastMessage.postValue("Error adding employee: " + e.getMessage()));
                } else {
                    toastMessage.postValue("Error creating user: " + task.getException().getMessage());
                }
            });
        } else { // Update existing employee
            existingEmployee.setName(name);
            existingEmployee.setPermissions(currentPermissions);
            existingEmployee.setPhoto(photo);
            existingEmployee.setSalary(salary);
            existingEmployee.setPhone(phone);
            existingEmployee.setAddress(address);
            existingEmployee.setAge(age);
            existingEmployee.setDesignation(designation);
            repository.updateEmployee(existingEmployee)
                    .addOnSuccessListener(aVoid -> toastMessage.postValue("Employee updated successfully!"))
                    .addOnFailureListener(e -> toastMessage.postValue("Error updating employee: " + e.getMessage()));
        }
    }

    public void deleteEmployee(Employee employee) {
        repository.deleteEmployee(employee)
                .addOnSuccessListener(aVoid -> toastMessage.postValue("Employee deleted."))
                .addOnFailureListener(e -> toastMessage.postValue("Error deleting employee: " + e.getMessage()));
    }

    public void setCurrentPermissions(Map<String, Permission> permissions) {
        this.currentPermissions = permissions != null ? new HashMap<>(permissions) : new HashMap<>();
    }

    public Map<String, Permission> getCurrentPermissions() {
        return currentPermissions;
    }

    public void onPermissionToggled(String permissionName, boolean isChecked) {
        if (!currentPermissions.containsKey(permissionName)) {
            currentPermissions.put(permissionName, new Permission());
        }
        Permission permission = currentPermissions.get(permissionName);
        permission.setGranted(isChecked);
        if (!isChecked) {
            permission.setExpires(null);
        }
    }

    public void setPermissionExpiry(String permissionName, Timestamp expiry) {
        if (currentPermissions.containsKey(permissionName)) {
            Permission permission = currentPermissions.get(permissionName);
            permission.setExpires(expiry);
            permission.setGranted(true);
        }
    }

    public void onTillChipClicked(String permissionName, java.util.function.Consumer<Permission> datePickerCallback) {
        if (currentPermissions.containsKey(permissionName)) {
            datePickerCallback.accept(currentPermissions.get(permissionName));
        }
    }
}
