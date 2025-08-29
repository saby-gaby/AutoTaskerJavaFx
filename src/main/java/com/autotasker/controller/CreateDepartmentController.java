package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.model.Department;
import com.autotasker.util.InformationAlert;
import com.autotasker.util.WarningAlert;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.List;

public class CreateDepartmentController {
    @FXML private TextField departmentNameField;

    private final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();

    @FXML
    public void handleAddDepartment() {
        String departmentName = departmentNameField.getText();

        if (departmentName.isEmpty()) {
            new WarningAlert("Department Name cannot be empty!").showAndWait();
            return;
        }

        List<String> departmentNames = DEPARTMENT_DAO.findAllDepartmentNames();
        if (departmentNames.contains(departmentName)) {
            new WarningAlert("Department Name already exists!").showAndWait();
            return;
        }

        Department department = new Department(departmentName, null);

        department = DEPARTMENT_DAO.saveDepartment(department);
        if (department != null) {
            String message = "Department " + department.getDepartmentName() + " successfully added!";
            new InformationAlert(message).showAndWait();
            Stage stage = (Stage) departmentNameField.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) departmentNameField.getScene().getWindow();
        stage.close();
    }
}
