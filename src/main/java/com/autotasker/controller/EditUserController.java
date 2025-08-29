package com.autotasker.controller;

import com.autotasker.util.*;
import javafx.fxml.FXML;
import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.Role;
import com.autotasker.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;

public class EditUserController {
    @FXML private TextField usernameField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private ComboBox<Department> departmentComboBox;
    @FXML private Button updatePasswordButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    private final UserDAO USER_DAO = new UserDAO();
    private final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();

    public void initFields(User user, TableView<User> userTable, Stage editStage) {
        User currUser = USER_DAO.findByUsername(user.getUsername());
        ArrayList<Role> rolesList = new ArrayList<>(Arrays.asList(Role.values()));
        ComboboxUtil.initializeSortedDropdown(roleComboBox, rolesList, null);
        ArrayList<Department> departmentsList = new ArrayList<>(DEPARTMENT_DAO.findAll());
        ComboboxUtil.initializeSortedDropdown(departmentComboBox, departmentsList, Department.class);
        // visible options for department
        ComboboxUtil.departmentCellFactory(departmentComboBox);

        usernameField.setText(user.getUsername());
        usernameField.setEditable(false);
        roleComboBox.getSelectionModel().select(user.getRole());
        departmentComboBox.getSelectionModel().select(currUser.getDepartment());
        saveButton.setOnAction(e -> editUser(user, userTable, editStage));
        updatePasswordButton.setOnAction(e -> changePassword(user));
        deleteButton.setOnAction(e -> deleteUser(user, userTable));
    }

    public void editUser(User user, TableView<User> userTable, Stage editStage) {
        user.setRole(roleComboBox.getSelectionModel().getSelectedItem());
        Department oldDepartment = user.getDepartment();
        Department newDepartment = departmentComboBox.getSelectionModel().getSelectedItem();
        user.setDepartment(newDepartment);
        Department departmentWithCurrentUserAsManager = DEPARTMENT_DAO.findByDepartmentManager(user);
        // user is department manager (user is foreign key in department - one to one)
        if (departmentWithCurrentUserAsManager != null && !oldDepartment.equals(newDepartment)) {
            new WarningAlert(
                    "User '" + user.getUsername() + "' is a department manager. " +
                            "\nTo assign this user to new Department, you must first assign a new manager for department: '" +
                            oldDepartment.getDepartmentName() + "'."
            ).showAndWait();
            editStage.close();
        }else {
            boolean isUpdated = USER_DAO.updateUser(user);
            if (isUpdated) {
                new InformationAlert("User '" + user.getUsername() + "' successfully updated")
                        .showAndWait();
                LoadTableUtil.loadUserTable(userTable, USER_DAO);
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        }
    }

    private void deleteUser(User user, TableView<User> userTable) {
        if (user != null) {
            Department department = DEPARTMENT_DAO.findByDepartmentManager(user);
            // user is department manager (user is foreign key in department - one to one)
            if (department != null) {
                new WarningAlert(
                        "User '" + user.getUsername() + "' is a department manager. " +
                                "\nTo delete this user, you must first assign a new manager for department: '" +
                                department.getDepartmentName() + "'."
                ).showAndWait();
            }
            // user is Not department manager
            boolean isDeleted = USER_DAO.deleteUser(user);
            if (isDeleted) {
                new InformationAlert("User " + user.getUsername() + " has been deleted").showAndWait();
                LoadTableUtil.loadUserTable(userTable, USER_DAO);
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        }
    }

    @FXML
    public void changePassword(User user) {

        Optional<ButtonType> bType = new ConfirmationUtil(
                "Are you sure you want to change the password for user '"
                        + user.getUsername() + "'?"
        ).showAndWait();

        if (bType.isPresent() && bType.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/edit_user_password_view/edit_user_password.fxml"));
                Parent root = loader.load();
                EditUserPasswordController controller = loader.getController();
                controller.initWindow(user);

                Stage stage = new Stage();
                stage.setTitle("Set new Password for User '" + user.getUsername() + "'");
                WindowUtil.setWindow(root, stage, saveButton);
                stage.showAndWait();

            } catch (IOException e) {
                new WarningAlert(e.getMessage()).showAndWait();
            }
        }

    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
