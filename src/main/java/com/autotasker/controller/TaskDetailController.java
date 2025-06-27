package com.autotasker.controller;

import com.autotasker.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class TaskDetailController {
    @FXML private Label nameLabel;
    @FXML private Label assignedToLabel;
    @FXML private Label dueDateLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label statusLabel;
    @FXML private Label createDateLabel;

    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setTask(Task task) {
        nameLabel.setText("Name: " + task.getName());
        String assignedTo;
        if (task.getAssignedUser() != null) {
            assignedTo = task.getAssignedUser().getUsername();
        } else if (task.getAssignedDepartment() != null) {
            assignedTo = task.getAssignedDepartment().getDepartmentName();
        } else {
            assignedTo = "Unassigned";
        }
        assignedToLabel.setText("Assigned to: " + assignedTo);
        dueDateLabel.setText("Due Date: " + task.getDueDate());
        descriptionArea.setText(task.getDescription());
        statusLabel.setText("Status: " + (task.isInProgress() ? "In Progress" : task.isCompleted() ? "Completed" : "TODO"));
        createDateLabel.setText("Created at: " + task.getCreatedAt());
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }
}
