package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.TaskDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.Task;
import com.autotasker.model.User;
import com.autotasker.util.OneCheckboxSelected;
import com.autotasker.util.ComboboxUtil;
import com.autotasker.util.WarningAlert;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditTaskController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<User> userComboBox;
    @FXML private ComboBox<Department> departmentComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private CheckBox completedCheckBox;
    @FXML private CheckBox inProgressCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button deleteButton;

    public void initializeFields(Task task, TaskDAO taskDAO, Runnable onCloseCallback) {

        nameField.setText(task.getName());
        descriptionArea.setWrapText(true);
        descriptionArea.setText(task.getDescription());

        ComboboxUtil.initializeAssignTaskDropdowns(new DepartmentDAO(), new UserDAO(), departmentComboBox, userComboBox);
        if (task.getAssignedDepartment() != null) {
            departmentComboBox.getSelectionModel().select(task.getAssignedDepartment());
        }
        if (task.getAssignedUser() != null) {
            userComboBox.getSelectionModel().select(task.getAssignedUser());
        }

        if (task.getDueDate() != null) dueDatePicker.setValue(task.getDueDate());

        completedCheckBox.setSelected(task.isCompleted());
        inProgressCheckBox.setSelected(task.isInProgress());

        OneCheckboxSelected.ensureOnlyOneSelected(completedCheckBox, inProgressCheckBox);

        saveButton.setOnAction(event -> {
            task.setName(nameField.getText());
            if (task.getName() == null || task.getName().isEmpty()) {
                new WarningAlert("Please enter a task name.").showAndWait();
                return;
            }
            task.setDescription(descriptionArea.getText());
            task.setDueDate(dueDatePicker.getValue());
            task.setCompleted(completedCheckBox.isSelected());
            task.setInProgress(inProgressCheckBox.isSelected());
            task.setAssignedDepartment(departmentComboBox.getSelectionModel().getSelectedItem());
            task.setAssignedUser(userComboBox.getSelectionModel().getSelectedItem());
            if (task.getAssignedUser() == null && task.getAssignedDepartment() == null) {
                new WarningAlert("Please assign the task to a user or department.").showAndWait();
                return;
            }
            taskDAO.updateTask(task);
            onCloseCallback.run();
            closeDialog();
        });
        cancelButton.setOnAction(event -> closeDialog());
        deleteButton.setOnAction(event -> {
            taskDAO.deleteTask(task);
            if (onCloseCallback != null) onCloseCallback.run();
            closeDialog();
        });
    }

    private void closeDialog() {
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.close();
    }
}
