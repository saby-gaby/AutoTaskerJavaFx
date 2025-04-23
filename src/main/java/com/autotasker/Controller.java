package com.autotasker;

import com.autotasker.model.Task;
import com.autotasker.model.TaskDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {
    @FXML
    private TextField taskNameField;
    @FXML
    private TextField taskDescriptionField;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> nameColumn;
    @FXML
    private TableColumn<Task, String> descriptionColumn;
    @FXML
    private TableColumn<Task, String> dueDateColumn;
    @FXML
    private TableColumn<Task, Boolean> completedColumn;
    @FXML
    private TableColumn<Task, Void> actionColumn;  // For Action buttons

    @FXML
    private DatePicker filterDatePicker;
    @FXML
    private CheckBox filterIncompleteCheckBox;
    @FXML
    private CheckBox filterCompleteCheckBox;

    private final TaskDAO taskDAO = new TaskDAO(); // Task DAO instance
    private List<Task> allTasksCache = new ArrayList<>();

    @FXML
    public void initialize() {
        ensureOnlyOneCheckboxIsSelected();


        // Setting up the cell value factories for columns
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        completedColumn.setCellFactory(col -> new TableCell<Task, Boolean>() {
            @Override
            protected void updateItem(Boolean completed, boolean empty) {
                super.updateItem(completed, empty);
                if (empty || completed == null) {
                    setText(null);
                } else {
                    setText(completed ? "✅" : "❌");
                }
            }
        });

        // Setting up the action buttons in the table
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button markCompleteBtn = new Button("Mark as completed");
            private final Button deleteBtn = new Button("Delete task");
            private final Button editBtn = new Button("✎");

            {
                markCompleteBtn.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    markAsCompleted(task);
                });
                deleteBtn.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    deleteTask(task);
                });
                editBtn.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    handleEditTask(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Create a horizontal layout with two buttons
                    HBox hBox = new HBox(10, markCompleteBtn, deleteBtn, editBtn);
                    setGraphic(hBox);
                }
            }
        });

        // Adding the action column to the table
        boolean actionsColumnExists = taskTable
                .getColumns()
                .stream()
                .anyMatch(c -> "Actions".equals(c.getText()));
        if (!actionsColumnExists) {
            taskTable.getColumns().add(actionColumn);
        }

        allTasksCache = taskDAO.getAllTasks();
        // Load tasks initially
        loadTasks();
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterTasksBySearch(newText);
        });

        taskTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Task clickedTask = row.getItem();
                    showTaskDetailDialog(clickedTask);
                }
            });
            return row;
        });
    }

    private void showTaskDetailDialog(Task task) {
        Stage dialog = new Stage();
        dialog.setTitle("Task details");
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(taskTable.getScene().getWindow());

        Label nameLabel = new Label("Name: " + task.getName());
        Label dueDateLabel = new Label("Due Date: " + task.getDueDate());
        Label descriptionLabel = new Label("Description: ");
        TextArea descriptionArea = new TextArea(task.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle(" -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        Label createDateLabel = new Label("Created at: " + task.getCreatedAt());
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(event -> {
            dialog.close();
        });

        VBox vBox = new VBox(10, nameLabel, dueDateLabel, descriptionLabel, descriptionArea, createDateLabel, closeBtn);
        vBox.setPadding(new Insets(15));

        Scene scene = new Scene(vBox, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void loadTasks() {
        List<Task> filtered = new ArrayList<>(getFilteredTasksByCheckbox());
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) filtered = getFilteredTasksBySearch(searchQuery);
        filtered.sort((t1, t2) -> {
            LocalDate d1 = t1.getDueDate();
            LocalDate d2 = t2.getDueDate();
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return -1;
            if (d2 == null) return 1;
            return d1.compareTo(d2);
        });
        if (filterCompleteCheckBox.isSelected()) filtered = filtered.reversed();

        taskTable.getItems().setAll(filtered);
    }

    @FXML
    private void handleShowAllTasks() {
        filterIncompleteCheckBox.setSelected(false);
        filterCompleteCheckBox.setSelected(false);
        taskTable.getItems().setAll(allTasksCache);
    }

    @FXML
    public void handleAddTask(ActionEvent event) {
        String name = taskNameField.getText();
        String description = taskDescriptionField.getText();
        LocalDate dueDate = dueDatePicker.getValue();

        if (name.isBlank()) {
            showAlert("Name cannot be empty.");
            return;
        }

        Task task = new Task(name, description);

        if (dueDate != null) {
            task.setDueDate(dueDate);
        }
        taskDAO.addTask(task);
        allTasksCache = taskDAO.getAllTasks();

        // Clear the input fields after adding the task
        taskNameField.clear();
        taskDescriptionField.clear();
        dueDatePicker.setValue(null);

        // Refresh the task table
        loadTasks();
    }

    // Mark task as completed
    @FXML
    private void markAsCompleted(Task task) {
        if (task != null) {
            task.setCompleted(true);
            taskDAO.updateTask(task);
            allTasksCache = taskDAO.getAllTasks();
            loadTasks();  // Refresh task table
        }
    }

    // Delete the task
    @FXML
    private void deleteTask(Task task) {
        if (task != null) {
            taskDAO.deleteTask(task);
            allTasksCache = taskDAO.getAllTasks();
            loadTasks();  // Refresh task table
        }
    }

    @FXML
    private void handleFilterByDate() {
        LocalDate selectedDate = filterDatePicker.getValue();

        if (selectedDate != null) {
            List<Task> tasks = getFilteredTasksByCheckbox().stream()
                    .filter(t -> selectedDate.equals(t.getDueDate()))
                    .toList();
            taskTable.getItems().setAll(tasks);
        } else {
            showAlert("No date selected. Please select a date before filtering.");
        }
    }

    @FXML
    private void handleShowUpcoming() {
        LocalDate today = LocalDate.now();

        List<Task> upcoming = getFilteredTasksByCheckbox().stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(today))
                .toList();
        taskTable.getItems().setAll(upcoming);
    }

    private void openEditTaskDialog(Task task) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Task");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(taskTable.getScene().getWindow());

        TextField nameField = new TextField(task.getName());
        TextField descriptionField = new TextField(task.getDescription());
        DatePicker dueDatePicker = new DatePicker(task.getDueDate());

        VBox nameBox = createLabeledInput("Name", nameField);
        VBox descriptionBox = createLabeledInput("Description", descriptionField);
        VBox dueDateBox = createLabeledInput("Due Date", dueDatePicker);

        Button saveButton = new Button("Save Changes");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(event -> {
            task.setName(nameField.getText());
            task.setDescription(descriptionField.getText());
            task.setDueDate(dueDatePicker.getValue());
            taskDAO.updateTask(task);
            allTasksCache = taskDAO.getAllTasks();
            loadTasks();
            dialogStage.close();
        });

        cancelButton.setOnAction(event -> dialogStage.close());

        HBox buttons = new HBox(10, saveButton, cancelButton);

        VBox layout = new VBox(20);
        layout.getChildren().addAll(nameBox, descriptionBox, dueDateBox, buttons);
        layout.setPadding(new Insets(15));

        Scene scene = new Scene(layout);
        dialogStage.setMinWidth(400);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private VBox createLabeledInput(String labelText, Control oldValue) {
        Label label = new Label(labelText);
        return new VBox(5, label, oldValue);
    }

    private void handleEditTask(Task task) {
        if (task != null) {
            openEditTaskDialog(task);
        }
    }

    private void filterTasksBySearch(String query) {
        List<Task> filtered = getFilteredTasksBySearch(query);
        taskTable.getItems().setAll(filtered);
    }

    private List<Task> getFilteredTasksBySearch(String query) {
        return getFilteredTasksByCheckbox().stream()
                .filter(t -> t.getName().toLowerCase().contains(query.toLowerCase())
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
    }

    private List<Task> getFilteredTasksByCheckbox() {
        List<Task> tasks;
        if (filterIncompleteCheckBox.isSelected()) tasks = getIncompleteTasks();
        else if (filterCompleteCheckBox.isSelected()) tasks = getCompleteTasks();
        else tasks = allTasksCache;
        return tasks;
    }

    private void ensureOnlyOneCheckboxIsSelected() {
        filterIncompleteCheckBox.selectedProperty()
                .addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        filterCompleteCheckBox.setSelected(false);
                    }
                });
        filterCompleteCheckBox.selectedProperty()
                .addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        filterIncompleteCheckBox.setSelected(false);
                    }
                });
    }

    private List<Task> getIncompleteTasks() {
        return allTasksCache
                .stream()
                .filter(t -> !t.isCompleted())
                .toList();
    }

    private List<Task> getCompleteTasks() {
        return allTasksCache
                .stream()
                .filter(Task::isCompleted)
                .toList();
    }

    // Show an alert with a given message
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}