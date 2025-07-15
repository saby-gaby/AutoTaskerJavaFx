package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.User;
import com.autotasker.util.LoadTableUtil;
import com.autotasker.util.WarningAlert;
import com.autotasker.util.WindowUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminViewController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> departmentColumn;
    @FXML private TableColumn<User, Void> editUserColumn;

    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, String> deptNameColumn;
    @FXML private TableColumn<Department, String> managerColumn;
    @FXML private TableColumn<Department, Void> editDepartmentColumn;

    private final UserDAO userDAO = new UserDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    @FXML
    public void initialize() {
        // Users
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(u -> new SimpleStringProperty(u.getValue().getRole().name()));
        departmentColumn.setCellValueFactory(u -> {
            Department dept = u.getValue().getDepartment();
            return new SimpleStringProperty(dept != null ? dept.getDepartmentName() : "");
        });

        // load users table
        LoadTableUtil.loadUserTable(userTable, userDAO);

        // Departments
        deptNameColumn.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        managerColumn.setCellValueFactory(d -> {
            User manager = d.getValue().getDepartmentManager();
            return new SimpleStringProperty(manager != null ? manager.getUsername() : "");
        });

        // load department table
        LoadTableUtil.loadDepartmentTable(departmentTable, departmentDAO);

        editUserColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.getStyleClass().add("edit-btn");
                editButton.setOnAction(e -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleEditUser(user);
                    }
                });
            }

            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        editDepartmentColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.getStyleClass().add("edit-btn");
                editButton.setOnAction(e -> {
                    Department department = getTableRow().getItem();
                    if (department != null) {
                        handleEditDepartment(department);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

    }

    @FXML
    public void showCreateUserWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/create_user.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create User");
            WindowUtil.setWindow(root, stage, userTable);
            stage.showAndWait();

            // After closing form
            LoadTableUtil.loadUserTable(userTable, userDAO);
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }

    private void handleEditUser(User user) {
        if (user != null) {
            openEditUserDialog(user);
        }
    }

    private void openEditUserDialog(User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/edit_user_view.fxml"));
            Parent root = loader.load();
            EditUserController controller = loader.getController();
            Stage stage = new Stage();
            controller.initFields(currentUser, userTable, stage);

            stage.setTitle("Edit User");
            WindowUtil.setWindow(root, stage, userTable);
            stage.showAndWait();

        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void showCreateDepartmentWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/create_department_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create Department");
            WindowUtil.setWindow(root, stage, departmentTable);
            stage.showAndWait();

            LoadTableUtil.loadUserTable(userTable, userDAO);
            LoadTableUtil.loadDepartmentTable(departmentTable, departmentDAO);
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }

    private void handleEditDepartment(Department department) {
        if (department != null) {
            openEditDepartmentDialog(department);
        }
    }

    private void openEditDepartmentDialog(Department currentDepartment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/edit_department_view.fxml"));
            Parent root = loader.load();
            EditDepartmentController controller = loader.getController();
            controller.initFields(currentDepartment, departmentTable, userTable);

            Stage stage = new Stage();
            stage.setTitle("Edit Department");
            WindowUtil.setWindow(root, stage, departmentTable);
            stage.showAndWait();

        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }
}
