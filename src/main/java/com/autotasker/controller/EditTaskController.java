package com.autotasker.controller;

import com.autotasker.util.OneCheckboxSelected;
import com.autotasker.model.Task;
import com.autotasker.model.TaskDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditTaskController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
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

        if (task.getDueDate() != null) dueDatePicker.setValue(task.getDueDate());

        completedCheckBox.setSelected(task.isCompleted());
        inProgressCheckBox.setSelected(task.isInProgress());

        OneCheckboxSelected.ensureOnlyOneSelected(completedCheckBox, inProgressCheckBox);

        saveButton.setOnAction(event -> {
            task.setName(nameField.getText());
            task.setDescription(descriptionArea.getText());
            task.setDueDate(dueDatePicker.getValue());
            task.setCompleted(completedCheckBox.isSelected());
            task.setInProgress(inProgressCheckBox.isSelected());
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
