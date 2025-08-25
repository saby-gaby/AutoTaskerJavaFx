package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.Role;
import com.autotasker.model.User;
import com.autotasker.util.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditDepartmentController {
    @FXML private TextField departmentNameField;
    @FXML private ComboBox<User> departmentManagerCombobox;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final UserDAO userDAO = new UserDAO();

    public void initFields(Department department, TableView<Department> departmentTable, TableView<User> userTable) {
        departmentNameField.setText(department.getDepartmentName());

        ComboboxUtil.initDepartmentManagerDropdown(departmentDAO, userDAO, departmentManagerCombobox, department.getDepartmentManager(), department);
        departmentManagerCombobox.getSelectionModel().select(department.getDepartmentManager());
        saveButton.setOnAction(e -> updateDepartment(userTable, department, departmentTable));
        deleteButton.setOnAction(e -> deleteDepartment(department, departmentTable, userTable));
    }

    private void updateDepartment(TableView<User> userTable, Department department, TableView<Department> departmentTable) {
        User oldManager = department.getDepartmentManager();
        User newManager = departmentManagerCombobox.getValue();
        department.setDepartmentName(departmentNameField.getText());
        department.setDepartmentManager(newManager);
        boolean isOldManagerUpdated = false;
        boolean isNewManagerUpdated = false;

        if (oldManager != null) {
            oldManager.setRole(Role.USER);
            isOldManagerUpdated = userDAO.updateUser(oldManager);
        }

        if (newManager != null) {
            newManager.setRole(Role.ADMIN);
            isNewManagerUpdated = userDAO.updateUser(newManager);
        }

        boolean isDepartmentUpdated = departmentDAO.updateDepartment(department);

        if (isDepartmentUpdated &&
                ((oldManager == null || isOldManagerUpdated) && (newManager == null || isNewManagerUpdated))) {
            new InformationAlert("Department updated successfully");
            LoadTableUtil.loadDepartmentTable(departmentTable, departmentDAO);
            if (isOldManagerUpdated || isNewManagerUpdated)
                LoadTableUtil.loadUserTable(userTable, userDAO);

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        }
    }

    private void deleteDepartment(Department department, TableView<Department> departmentTable, TableView<User> userTable) {
        if (department != null) {
            List<User> usersInThisDepartment = userDAO.getUsersByDepartment(department);
            Optional<ButtonType> buttonType;
            String departmentName = department.getDepartmentName();
            while (!usersInThisDepartment.isEmpty()) {
                buttonType = new ConfirmationUtil(
                        "There are users in department '" + departmentName + "'.\n" +
                                "To delete department '" + departmentName + "', " +
                                "you must first move the users from it to another department.\n" +
                                "\nDo you want to move user" +
                                (usersInThisDepartment.size() > 1 ? "s " : " ") +
                                createStringFromUsernamesInTheDepartment(usersInThisDepartment) +
                                " to another department now?"
                ).showAndWait();

                if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
                    for (User user : usersInThisDepartment) {
                        showChangeDepartmentDialog(user);
                    }
                } else {
                    return;
                }
                userTable.getItems().setAll(userDAO.findAll());
                usersInThisDepartment = userDAO.getUsersByDepartment(department);
            }
            buttonType = new ConfirmationUtil(
                    "Are you sure you want to delete department  '" + departmentName + "'?"
            ).showAndWait();
            if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
                departmentDAO.deleteDepartment(department);
                departmentTable.getItems().setAll(departmentDAO.findAll());
                Stage stage = (Stage) deleteButton.getScene().getWindow();
                stage.close();
            }
        }
    }

    private void showChangeDepartmentDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/change_user_department_view.fxml"));
            Parent root = loader.load();

            EditDepartmentForUserController controller = loader.getController();
            controller.initFields(user);

            Stage stage = new Stage();
            stage.setTitle("Change department for " + user.getUsername());
            WindowUtil.setWindow(root, stage, saveButton);
            stage.showAndWait();

        } catch (IOException e) {
            new WarningAlert("Could not load change_user_department_view.fxml").showAndWait();
        }
    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private String createStringFromUsernamesInTheDepartment(List<User> users) {
        List<String> userNames = new ArrayList<>(users.stream().map(u -> "'" + u.getUsername() + "'").toList());
        if (userNames.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            String lastUsername = userNames.removeLast();
            stringBuilder.append(String.join(", ", userNames)).append(" and ").append(lastUsername);
            return stringBuilder.toString();
        } else {
            return userNames.getFirst();
        }
    }
}
