package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.Role;
import com.autotasker.model.User;
import com.autotasker.util.ComboboxUtil;
import com.autotasker.util.InformationAlert;
import com.autotasker.util.WarningAlert;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.List;

public class CreateDepartmentController {
    @FXML private TextField departmentNameField;
    @FXML private ComboBox<User> departmentManagerCombobox;

    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleAddDepartment() {
        String departmentName = departmentNameField.getText();
        User departmentManager = departmentManagerCombobox.getSelectionModel().getSelectedItem();

        if (departmentName.isEmpty()) {
            new WarningAlert("Department Name cannot be empty!").showAndWait();
            return;
        }

        List<String> departmentNames = departmentDAO.findAllDepartmentNames();
        if (departmentNames.contains(departmentName)) {
            new WarningAlert("Department Name already exists!").showAndWait();
            return;
        }

        Department department = new Department(departmentName, departmentManager);
        User deptManager = department.getDepartmentManager();
        boolean isDeptManagerUpdated = false;
        if (deptManager != null) {
            deptManager.setRole(Role.ADMIN);
            isDeptManagerUpdated = userDAO.updateUser(deptManager);
        }

        department = departmentDAO.saveDepartment(department);
        if (department != null && (deptManager == null || isDeptManagerUpdated)) {
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
