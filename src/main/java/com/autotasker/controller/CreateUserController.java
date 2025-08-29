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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateUserController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> userRoleComboBox;
    @FXML private ComboBox<Department> departmentComboBox;

    private final UserDAO USER_DAO = new UserDAO();
    private final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();

    @FXML
    public void initialize() {
        ArrayList<Role> rolesList = new ArrayList<>(Arrays.asList(Role.values()));
        ComboboxUtil.initializeSortedDropdown(userRoleComboBox, rolesList, null);
        ArrayList<Department> departmentsList = new ArrayList<>(DEPARTMENT_DAO.findAll());
        ComboboxUtil.initializeSortedDropdown(departmentComboBox, departmentsList, Department.class);
        // visible options for department
        ComboboxUtil.departmentCellFactory(departmentComboBox);
    }

    @FXML
    public void handleAddUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        Role userRole = userRoleComboBox.getSelectionModel().getSelectedItem();
        Department department = departmentComboBox.getSelectionModel().getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || userRole == null || department == null) {
            new WarningAlert("Please fill all fields!").showAndWait();
            return;
        }

        if (USER_DAO.findAllUsernames().contains(username)) {
            new WarningAlert("Username already exists!").showAndWait();
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hashedPassword);
        user.setRole(userRole);
        user.setDepartment(department);

        user = USER_DAO.saveUser(user);
        if (user != null) {
            String msg = username + " - " + user.getRole().toString() + " in " +
                    user.getDepartment().getDepartmentName() +
                    " has been created! ";
            new InformationAlert(msg).showAndWait();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
