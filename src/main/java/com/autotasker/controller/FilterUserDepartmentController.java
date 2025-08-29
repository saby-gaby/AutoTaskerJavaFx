package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.User;
import com.autotasker.util.ComboboxUtil;
import com.autotasker.util.WarningAlert;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class FilterUserDepartmentController {

    @FXML private ComboBox<Department> filterDepartmentComboBox;
    @FXML private ComboBox<User> filterUserComboBox;
    @FXML private Button filterBtn;

    private final UserDAO USER_DAO = new UserDAO();
    private final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();
    private UserViewController parentController;

    @FXML
    private void initialize() {
        ComboboxUtil.initializeAssignTaskDropdowns(
                DEPARTMENT_DAO, USER_DAO, filterDepartmentComboBox, filterUserComboBox
        );
    }

    @FXML
    private void handleFilter() {
        User user = filterUserComboBox.getValue();
        Department department = filterDepartmentComboBox.getValue();

        if (user != null) {
            parentController.setFilteredByUser(user);
        } else if (department != null) {
            parentController.setFilteredByDepartment(department);
        } else {
            new WarningAlert("Please select a user or department to search for.").showAndWait();
            return;
        }

        Stage stage = (Stage) filterBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) filterDepartmentComboBox.getScene().getWindow();
        stage.close();
    }

    public void setParentController(UserViewController parentController) {
        this.parentController = parentController;
    }
}
