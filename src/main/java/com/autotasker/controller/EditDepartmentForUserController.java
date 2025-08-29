package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.User;
import com.autotasker.util.ComboboxUtil;
import com.autotasker.util.InformationAlert;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;

public class EditDepartmentForUserController {

    @FXML private TextField usernameField;
    @FXML private ComboBox<Department> departmentComboBox;
    @FXML private Button saveButton;

    private final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();
    private final UserDAO USER_DAO = new UserDAO();

    public void initFields(User user) {
        ArrayList<Department> departmentsList = new ArrayList<>(DEPARTMENT_DAO.findAll());
        ComboboxUtil.initializeSortedDropdown(departmentComboBox, departmentsList, Department.class);
        // visible options for department
        ComboboxUtil.departmentCellFactory(departmentComboBox);
        usernameField.setText(user.getUsername());
        usernameField.setEditable(false);
        departmentComboBox.getSelectionModel().select(user.getDepartment());
        saveButton.setOnAction(e -> saveNewDepartment(user));
    }

    private void saveNewDepartment(User user) {
        user.setDepartment(departmentComboBox.getSelectionModel().getSelectedItem());
        boolean isUpdated = USER_DAO.updateUser(user);
        if (isUpdated) {
            new InformationAlert(
                    user.getUsername() + "'s department has been updated to '" +
                            user.getDepartment().getDepartmentName() + "'!"
            ).showAndWait();
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
