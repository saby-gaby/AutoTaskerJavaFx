package com.autotasker.controller;

import com.autotasker.util.OneCheckboxSelected;
import com.autotasker.util.WarningAlert;
import com.autotasker.model.Task;
import com.autotasker.model.TaskDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainController {
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
        OneCheckboxSelected.ensureOnlyOneSelected(filterCompleteCheckBox, filterIncompleteCheckBox);
        
        // Setting up the cell value factories for columns
        rowNumberColumn.setCellFactory(col -> new TableCell<>() {
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
                    List<String> classes = getStyleClass();
                    classes.removeAll(List.of("label-todo", "label-inProgress", "label-completed"));
                    if (task.isCompleted()) {
                        setText("✅ Completed");
                        classes.add("label-completed");
                    } else if (task.isInProgress()) {
                        setText("⌛ In Progress");
                        classes.add("label-inProgress");
                    } else {
                        setText("❌ To do");
                        classes.add("label-todo");
                    }
                }
            }
        });

        // Setting up the action buttons in the table
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button setInProgressBtn = new Button("⌛");
            private final Button markCompleteBtn = new Button("✅");
            private final Button editBtn = new Button("✎");

            {
                setInProgressBtn.getStyleClass().add("in-progress-btn");
                markCompleteBtn.getStyleClass().add("completed-btn");
                editBtn.getStyleClass().add("edit-btn");
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
                hBox.getStyleClass().add("action-buttons");
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
        searchField.textProperty().addListener((obs, oldText, newText) -> filterTasksBySearch(newText));

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
    public void handleAddTask() {
        String name = taskNameField.getText();
        String description = taskDescriptionField.getText();
        LocalDate dueDate = dueDatePicker.getValue();

        if (name.isBlank()) {
            new WarningAlert("Name cannot be empty.").showAlert();
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

    @FXML
    private void handleFilterByDate() {
        LocalDate selectedDate = filterDatePicker.getValue();

        if (selectedDate != null) {
            List<Task> tasks = getFilteredTasksByCheckbox().stream()
                    .filter(t -> selectedDate.equals(t.getDueDate()))
                    .toList();
            taskTable.getItems().setAll(tasks);
        } else {
            new WarningAlert("No date selected. Please select a date before filtering.").showAlert();
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/task_detail.fxml"));
            Parent root = loader.load();

            TaskDetailController controller = loader.getController();
            Stage dialog = new Stage();
            controller.setDialogStage(dialog);
            controller.setTask(task);

            dialog.setTitle("Task details");
            setModality(root, dialog);
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAlert();
        }
    }

    private void setModality(Parent root, Stage dialog) {
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(taskTable.getScene().getWindow());
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/autotasker/view/styles.css")).toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void openEditTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/edit_task_dialog.fxml"));
            Parent root = loader.load();
            EditTaskController controller = loader.getController();
            controller.initializeFields(task, taskDAO, () -> {
                allTasksCache = taskDAO.getAllTasks();
                loadTasks();
            });

            Stage dialog = new Stage();
            dialog.setTitle("Edit Task");
            setModality(root, dialog);
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAlert();
        }
    }

    private void handleSetInProgress(Task task) {
        if (task.isCompleted()) {
            new WarningAlert("Can't mark as 'In Progress' - task is already completed.").showAlert();
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
}