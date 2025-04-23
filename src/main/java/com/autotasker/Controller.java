package com.autotasker;

import com.autotasker.model.Task;
import com.autotasker.model.TaskDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
    private TableColumn<Task, Number> rowNumberColumn;
    @FXML
    private TableColumn<Task, String> nameColumn;
    @FXML
    private TableColumn<Task, String> descriptionColumn;
    @FXML
    private TableColumn<Task, String> dueDateColumn;
    @FXML
    private TableColumn<Task, Void> statusColumn;
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
        ensureOnlyOneCheckboxIsSelected(filterCompleteCheckBox, filterIncompleteCheckBox);
        
        // Setting up the cell value factories for columns
        rowNumberColumn.setCellFactory(col -> new TableCell<Task, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Task task = getTableRow().getItem();
                    if (task.isCompleted()) {
                        setText("✅ Completed");
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (task.isInProgress()) {
                        setText("⌛ In Progress");
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setText("❌ To do");
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Setting up the action buttons in the table
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button setInProgressBtn = new Button("⌛");
            private final Button markCompleteBtn = new Button("✅");
            private final Button editBtn = new Button("✎");
//            buttons style
//            {
//                setInProgressBtn.setStyle("-fx-background-color: #f8deae; -fx-text-fill: #4f3605; -fx-border-color: #f5cb7d; -fx-border-width: 2; -fx-border-radius: 50%; -fx-background-radius: 50%;");
//                markCompleteBtn.setStyle("-fx-background-color: #bffcbf; -fx-text-fill: #014101; -fx-border-color: #6af86a; -fx-border-width: 2; -fx-border-radius: 50%; -fx-background-radius: 50%;");
//                editBtn.setStyle("-fx-background-color: #79f3f3; -fx-text-fill: #3c4e54;  -fx-border-color: #20b0b0; -fx-border-width: 2; -fx-border-radius: 50%; -fx-background-radius: 50%;");
//            }

            {
                setInProgressBtn.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    handleSetInProgress(task);
                });
                markCompleteBtn.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    markAsCompleted(task);
                });
                editBtn.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    handleEditTask(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Task task = getTableRow().getItem();

                HBox hBox = new HBox(10);
                hBox.setAlignment(Pos.CENTER_RIGHT);
                if (!task.isCompleted() && !task.isInProgress()) {
                    hBox.getChildren().add(setInProgressBtn);
                }
                if (!task.isCompleted()) {
                    hBox.getChildren().add(markCompleteBtn);
                }

                hBox.getChildren().addAll(editBtn);
                setGraphic(hBox);
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
            task.setInProgress(false);
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
        Label statusLabel = new Label("Status: " + (task.isInProgress() ? "In Progress" : task.isCompleted() ? "Completed" : "TODO"));
        Label createDateLabel = new Label("Created at: " + task.getCreatedAt());
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(event -> {
            dialog.close();
        });

        VBox vBox = new VBox(10, nameLabel, dueDateLabel, descriptionLabel, descriptionArea, statusLabel, createDateLabel, closeBtn);
        vBox.setPadding(new Insets(15));

        Scene scene = new Scene(vBox, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void openEditTaskDialog(Task task) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Task");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(taskTable.getScene().getWindow());

        TextField nameField = new TextField(task.getName());
        TextField descriptionField = new TextField(task.getDescription());
        DatePicker dueDatePicker = new DatePicker(task.getDueDate());
        CheckBox completedCheckBox = new CheckBox("Completed");
        completedCheckBox.setSelected(task.isCompleted());
        CheckBox inProgressCheckBox = new CheckBox("In Progress");
        inProgressCheckBox.setSelected(task.isInProgress());

        ensureOnlyOneCheckboxIsSelected(completedCheckBox, inProgressCheckBox);

        VBox nameBox = createLabeledInput("Name", nameField);
        VBox descriptionBox = createLabeledInput("Description", descriptionField);
        VBox dueDateBox = createLabeledInput("Due Date", dueDatePicker);
        HBox completedProgressBox = new HBox(10);
        completedProgressBox.getChildren().addAll(completedCheckBox, inProgressCheckBox);

        Button saveButton = new Button("Save Changes");
        Button deleteBtn = new Button("Delete task");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(event -> {
            task.setName(nameField.getText());
            task.setDescription(descriptionField.getText());
            task.setDueDate(dueDatePicker.getValue());
            task.setCompleted(completedCheckBox.isSelected());
            task.setInProgress(inProgressCheckBox.isSelected());
            taskDAO.updateTask(task);
            allTasksCache = taskDAO.getAllTasks();
            loadTasks();
            dialogStage.close();
        });

        deleteBtn.setOnAction(event -> {
            deleteTask(task);
            dialogStage.close();
        });

        cancelButton.setOnAction(event -> dialogStage.close());

        HBox buttons = new HBox(10, saveButton, cancelButton);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox allBtns = new HBox(10, buttons, spacer, deleteBtn);
        VBox layout = new VBox(20);
        layout.getChildren().addAll(nameBox, descriptionBox, dueDateBox, completedProgressBox, allBtns);
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

    private void handleSetInProgress(Task task) {
        if (task.isCompleted()) {
            showAlert("Can't mark as 'In Progress' - task is already completed.");
            return;
        }
        task.setInProgress(true);
        taskDAO.updateTask(task);
        allTasksCache = taskDAO.getAllTasks();
        loadTasks();
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

    private void ensureOnlyOneCheckboxIsSelected(CheckBox completed, CheckBox second) {
        second.selectedProperty()
                .addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        completed.setSelected(false);
                    }
                });
        completed.selectedProperty()
                .addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        second.setSelected(false);
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