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

    private final TaskDAO TASK_DAO = new TaskDAO();
    private final UserDAO USER_DAO = new UserDAO();
    private final DepartmentDAO DEPARTMENT_DAO = new DepartmentDAO();

    @FXML
    public void initialize() {
        // dynamic options with dependency
        ComboboxUtil.initializeAssignTaskDropdowns(
                DEPARTMENT_DAO, USER_DAO, departmentComboBox, userComboBox
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
            TASK_DAO.addTask(task);
            Stage stage = (Stage) taskNameField.getScene().getWindow();
            stage.close();
    }
}
