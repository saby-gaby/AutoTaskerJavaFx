package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.TaskDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Task;
import com.autotasker.model.User;
import com.autotasker.model.Department;
import com.autotasker.util.ComboboxUtil;
import com.autotasker.util.WarningAlert;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class TaskFormController {

    @FXML private TextField taskNameField;
    @FXML private TextField taskDescriptionField;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<User> userComboBox;
    @FXML private ComboBox<Department> departmentComboBox;

    private final TaskDAO taskDAO = new TaskDAO();
    private final UserDAO userDAO = new UserDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    @FXML
    public void initialize() {
        // dynamic options with dependency
        ComboboxUtil.initializeAssignTaskDropdowns(
                departmentDAO, userDAO, departmentComboBox, userComboBox
        );
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) taskNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleAddTask() {
        String name = taskNameField.getText();
        String description = taskDescriptionField.getText();
        LocalDate dueDate = dueDatePicker.getValue();
        User assignedUser = userComboBox.getValue();
        Department assignedDepartment = departmentComboBox.getValue();

        if (name.isBlank()) {
            new WarningAlert("Name cannot be empty.").showAndWait();
            return;
        }

        Task task = new Task(name, description);

        if (dueDate != null) {
            task.setDueDate(dueDate);
        }

        task.setAssignedUser(assignedUser);
        task.setAssignedDepartment(assignedDepartment);

        if (task.getAssignedUser() == null && task.getAssignedDepartment() == null) {
            new WarningAlert("Please assign the task to a user or department.").showAndWait();
            return;
        }
            taskDAO.addTask(task);
            Stage stage = (Stage) taskNameField.getScene().getWindow();
            stage.close();
    }

//    private static void initializeAssignTaskDropdowns(DepartmentDAO departmentDAO, UserDAO userDAO, ComboBox<Department> departmentComboBox, ComboBox<User> userComboBox) {
//        // load departments + null option
//        List<Department> departmentList = new ArrayList<>();
//        departmentList.add(null); // empty option
//        departmentList.addAll(departmentDAO.findAll());
//        ObservableList<Department> departments = FXCollections.observableArrayList(departmentList);
//        departmentComboBox.setItems(departments);
//
//        // load all users + null option
//        List<User> userList = new ArrayList<>();
//        userList.add(null); // empty option
//        userList.addAll(userDAO.findAll());
//        ObservableList<User> allUsers = FXCollections.observableArrayList(userList);
//        userComboBox.setItems(allUsers);
//
//        // CellFactory for Department
//        // visible options
//        departmentComboBox.setCellFactory(cb -> new ListCell<>() {
//            @Override
//            protected void updateItem(Department dep, boolean empty) {
//                super.updateItem(dep, empty);
//                setText(empty ? null : (dep == null ? "" : dep.getDepartmentName()));
//            }
//        });
//        // selected option
//        departmentComboBox.setButtonCell(new ListCell<>() {
//            @Override
//            protected void updateItem(Department dep, boolean empty) {
//                super.updateItem(dep, empty);
//                setText(empty ? null : (dep == null ? "Select Department" : dep.getDepartmentName()));
//            }
//        });
//
//        // CellFactory for User
//        // visible options
//        userComboBox.setCellFactory(cb -> new ListCell<>() {
//            @Override
//            protected void updateItem(User user, boolean empty) {
//                super.updateItem(user, empty);
//                setText(empty ? null : (user == null ? "" : user.getUsername()));
//            }
//        });
//        // selected option
//        userComboBox.setButtonCell(new ListCell<>() {
//            @Override
//            protected void updateItem(User user, boolean empty) {
//                super.updateItem(user, empty);
//                setText(empty ? null : (user == null ? "Select User" : user.getUsername()));
//            }
//        });
//
//        // --- Dependency logic ---
//        // when select Department → filter User list
//        departmentComboBox.valueProperty().addListener((obs, oldDep, newDep) -> {
//            if (newDep != null) {
//                List<User> filtered = new ArrayList<>();
//                filtered.add(null); // празен избор
//                filtered.addAll(userDAO.findAll().stream()
//                        .filter(u -> u.getDepartment() != null && u.getDepartment().equals(newDep))
//                        .toList());
//                userComboBox.setItems(FXCollections.observableArrayList(filtered));
//                userComboBox.setDisable(false);
//            } else {
//                // if no department chose → show all users
//                userComboBox.setItems(allUsers);
//                userComboBox.setDisable(false);
//            }
//        });
//
//        // when select User → set department automatic (or unlock when nothing selected)
//        userComboBox.valueProperty().addListener((obs, oldUser, newUser) -> {
//            if (newUser != null) {
//                departmentComboBox.setValue(newUser.getDepartment());
//                departmentComboBox.setDisable(true); // lock department
//            } else {
//                // no user selected -> unlock departments
//                departmentComboBox.setDisable(false);
//            }
//        });
//    }
}
