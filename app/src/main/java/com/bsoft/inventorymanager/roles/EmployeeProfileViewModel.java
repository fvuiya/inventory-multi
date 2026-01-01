package com.bsoft.inventorymanager.roles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EmployeeProfileViewModel extends ViewModel {

    private final RolesRepository repository;
    private final MutableLiveData<Employee> employee = new MutableLiveData<>();

    public EmployeeProfileViewModel() {
        this.repository = new RolesRepository();
    }

    public LiveData<Employee> getEmployee() {
        return employee;
    }

    public void loadEmployee(String employeeId) {
        repository.getEmployee(employeeId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                // Handle error
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Employee emp = snapshot.toObject(Employee.class);
                if (emp != null) {
                    emp.setDocumentId(snapshot.getId());
                    employee.setValue(emp);
                }
            }
        });
    }
}
