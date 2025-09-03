package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.EmailDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.Email;
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
import java.util.List;
import java.util.stream.Collectors;

public class AdminViewController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> departmentColumn;
    @FXML
    private TableColumn<User, Void> editUserColumn;
    @FXML
    private TableColumn<User, String> emails;

    @FXML
    private TableView<Department> departmentTable;
    @FXML
    private TableColumn<Department, String> deptNameColumn;
    @FXML
    private TableColumn<Department, String> managerColumn;
    @FXML
    private TableColumn<Department, Void> editDepartmentColumn;

    @FXML
    private TableView<Email> emailTable;
    @FXML
    private TableColumn<Email, String> emailColumn;
    @FXML
    private TableColumn<Email, String> ownerColumn;
    @FXML
    private TableColumn<Email, Void> editEmailColumn;

    private final UserDAO USER_DAO = new UserDAO();
    private final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();
    private final EmailDAO EMAIL_DAO = new EmailDAO();

    @FXML
    public void initialize() {
        // Users
        initUsersTable();

        // Departments
        initDepartmentsTable();

        // Emails
        initEmailsTable();
    }

    private void initUsersTable() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(u -> new SimpleStringProperty(u.getValue().getRole().name()));
        departmentColumn.setCellValueFactory(u -> {
            Department dept = u.getValue().getDepartment();
            return new SimpleStringProperty(dept != null ? dept.getDepartmentName() : "");
        });
        emails.setCellValueFactory(u -> {
            User user = u.getValue();
            List<Email> emailList = EMAIL_DAO.findEmailsByUser(user);
            String concatenated = (emailList != null)
                    ? emailList.stream()
                    .map(Email::getEmail) // joins values
                    .collect(Collectors.joining("; "))
                    : "";
            return new SimpleStringProperty(concatenated);
        });

        // load users table
        LoadTableUtil.loadUserTable(userTable, USER_DAO);

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
    }

    private void initDepartmentsTable() {
        deptNameColumn.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        managerColumn.setCellValueFactory(d -> {
            User manager = d.getValue().getDepartmentManager();
            return new SimpleStringProperty(manager != null ? manager.getUsername() : "");
        });

        // load department table
        LoadTableUtil.loadDepartmentTable(departmentTable, DEPARTMENT_DAO);

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

    private void initEmailsTable() {
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        ownerColumn.setCellValueFactory(e -> {
            User owner = e.getValue().getOwner();
            return new SimpleStringProperty(owner != null ? owner.getUsername() : "");
        });

        // load email table
        LoadTableUtil.loadEmailTable(emailTable, EMAIL_DAO);

        editEmailColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.getStyleClass().add("edit-btn");
                editButton.setOnAction(e -> {
                    Email email = getTableRow().getItem();
                    if (email != null) {
                        handleEditEmail(email);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/create_user_view/create_user_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create User");
            WindowUtil.setWindow(root, stage, userTable);
            stage.showAndWait();

            // After closing form
            LoadTableUtil.loadUserTable(userTable, USER_DAO);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/edit_user_view/edit_user.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/create_department_view/create_department.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create Department");
            WindowUtil.setWindow(root, stage, departmentTable);
            stage.showAndWait();

            LoadTableUtil.loadUserTable(userTable, USER_DAO);
            LoadTableUtil.loadDepartmentTable(departmentTable, DEPARTMENT_DAO);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/edit_department_view/edit_department.fxml"));
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

    @FXML
    public void showCreateEmailWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/create_email_view/create_email.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create Email");
            WindowUtil.setWindow(root, stage, emailTable);
            stage.showAndWait();

            LoadTableUtil.loadEmailTable(emailTable, EMAIL_DAO);
            LoadTableUtil.loadUserTable(userTable, USER_DAO);
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }

    private void handleEditEmail(Email email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/edit_email_view/edit_email.fxml"));
            Parent root = loader.load();

            EditEmailController controller = loader.getController();
            controller.initFields(email);
            Stage stage = new Stage();
            stage.setTitle("Edit Email");
            WindowUtil.setWindow(root, stage, emailTable);
            stage.showAndWait();

            LoadTableUtil.loadUserTable(userTable, USER_DAO);
            LoadTableUtil.loadEmailTable(emailTable, EMAIL_DAO);
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }

    }
}
