package com.autotasker.controller;

import com.autotasker.model.Department;
import com.autotasker.model.Role;
import com.autotasker.model.User;
import com.autotasker.util.WindowUtil;
import com.autotasker.util.OneCheckboxSelected;
import com.autotasker.util.WarningAlert;
import com.autotasker.model.Task;
import com.autotasker.dao.TaskDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserViewController {
    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, Number> rowNumberColumn;
    @FXML private TableColumn<Task, String> nameColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private TableColumn<Task, Void> statusColumn;
    @FXML private TableColumn<Task, String> assignedToColumn;
    @FXML private TableColumn<Task, Void> actionsColumn;  // For Action buttons
    @FXML private Button upcomingButton;
    @FXML private DatePicker filterDatePicker;
    @FXML private CheckBox filterIncompleteCheckBox;
    @FXML private CheckBox filterCompleteCheckBox;
    @FXML private Button adminViewBtn;

    private User loggedInUser;
    private List<Task> filteredByUser;
    private List<Task> filteredByDepartment;
    private final TaskDAO taskDAO = new TaskDAO();
    private List<Task> allTasksCache = new ArrayList<>();
    private boolean isMyTaskFilterActive = false;
    private boolean isUpcomingSelected = false;
    private boolean isDateFilterActive = false;

    @FXML
    public void initialize() {
        OneCheckboxSelected.ensureOnlyOneSelected(filterCompleteCheckBox, filterIncompleteCheckBox);

        // create dynamic table
        initializeTaskTable();

        searchField.textProperty().addListener(
                (obs, oldText, newText) ->
                        filterTasksBySearch(newText, getTaskList()));

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

    private void initializeTaskTable() {
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

        assignedToColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            if (task.getAssignedUser() != null) {
                return new SimpleStringProperty(task.getAssignedUser().getUsername());
            } else if (task.getAssignedDepartment() != null) {
                return new SimpleStringProperty(task.getAssignedDepartment().getDepartmentName());
            } else {
                return new SimpleStringProperty("Unassigned");
            }
        });


        // Setting up the action buttons in the table
        actionsColumn.setCellFactory(col -> new TableCell<>() {
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
            taskTable.getColumns().add(actionsColumn);
        }

        allTasksCache = taskDAO.getAllTasks();
        // Load tasks initially
        loadTasks();
    }

    public void greetUser() {
        if (welcomeLabel != null && loggedInUser != null) {
            welcomeLabel.setText("Welcome, " + loggedInUser.getUsername() + "!");
        }
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        adminViewBtn.setVisible(user.getRole() == Role.ADMIN);
    }

    public void setFilteredByUser(User user) {
        isMyTaskFilterActive = false;
        this.filteredByUser = taskDAO.findByUser(user);
    }

    public void setFilteredByDepartment(Department department) {
        isMyTaskFilterActive = false;
        this.filteredByDepartment = taskDAO.findByDepartment(department);
    }

    @FXML
    private void handleRefresh() {
        allTasksCache = taskDAO.getAllTasks();
        loadTasks();  // Refresh task table
    }

    @FXML
    private void loadTasks() {
        List<Task> filtered = new ArrayList<>(getFilteredTasksByCheckbox(getTaskList()));
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) filtered = getFilteredTasksBySearch(searchQuery, filtered);
        filtered.sort((t1, t2) -> {
            LocalDate d1 = t1.getDueDate();
            LocalDate d2 = t2.getDueDate();
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return -1;
            if (d2 == null) return 1;
            return d1.compareTo(d2);
        });
        filtered = getFilteredTasksByCheckbox(filtered);
        if (filterCompleteCheckBox.isSelected()) filtered = filtered.reversed();
        if (isUpcomingSelected) filtered = getUpcomingTasks(filtered);
        if (isDateFilterActive) filtered = getFilteredByDate(filtered);

        taskTable.getItems().setAll(filtered);
    }

    @FXML
    public void loadMyTasks() {
        isMyTaskFilterActive = true;
        searchField.clear();
        filterIncompleteCheckBox.setSelected(false);
        filterCompleteCheckBox.setSelected(false);
        filterDatePicker.setValue(null);

        getMyTasks();
        loadTasks();
    }

    @FXML
    private void handleShowAllTasks() {
        searchField.clear();
        filterIncompleteCheckBox.setSelected(false);
        filterCompleteCheckBox.setSelected(false);
        isMyTaskFilterActive = false;
        isUpcomingSelected = false;
        upcomingButton.setText("Upcoming");
        isDateFilterActive = false;
        filterDatePicker.setValue(null);
        filteredByUser = null;
        filteredByDepartment = null;

        taskTable.getItems().setAll(allTasksCache);
    }

    @FXML
    private void createNewTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/task_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initOwner(welcomeLabel.getScene().getWindow());
            stage.setTitle("Create Task");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // After closing form -> Refresh the task table
            allTasksCache = taskDAO.getAllTasks();
            loadTasks();
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }

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
        boolean newDateFilterState = !isDateFilterActive;
        List<Task> tasks = new ArrayList<>();

        if (newDateFilterState) {
            tasks = getFilteredByDate(getTaskList());
        }

        if (tasks != null) {
            isDateFilterActive = newDateFilterState;
            taskTable.getItems().setAll(tasks);
        } else return;

        if (isDateFilterActive) {
            isUpcomingSelected = false;
            upcomingButton.setText("Upcoming");
        } else {
            upcomingButton.setText("Upcoming");
        }
    }

    private List<Task> getFilteredByDate(List<Task> tasks) {
        LocalDate selectedDate = filterDatePicker.getValue();
        if (selectedDate != null) {
            return tasks.stream()
                    .filter(t -> selectedDate.equals(t.getDueDate()))
                    .toList();
        } else {
            new WarningAlert("No date selected. Please select a date before filtering.").showAndWait();
            isDateFilterActive = false;
            return null;
        }
    }

    @FXML
    private void handleShowUpcoming() {
        List<Task> upcoming = getFilteredTasksByCheckbox(getTaskList());

        upcomingBtnControl();
        if (isUpcomingSelected) upcoming = getUpcomingTasks(upcoming);
        taskTable.getItems().setAll(upcoming);
    }

    private List<Task> getUpcomingTasks(List<Task> tasks) {
        LocalDate today = LocalDate.now();
        return tasks
                .stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(today))
                .toList();
    }

    private void upcomingBtnControl() {
        isUpcomingSelected = !isUpcomingSelected;
        if (isUpcomingSelected) {
            upcomingButton.setText("All Dates");
            filterDatePicker.setValue(null);
            isDateFilterActive = false;
        } else {
            upcomingButton.setText("Upcoming");
        }
    }

    private List<Task> getTaskList() {
        if (filteredByUser != null) return filteredByUser;
        else if (filteredByDepartment != null) return filteredByDepartment;
        else if (isMyTaskFilterActive) return getMyTasks();
        else return allTasksCache;
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
            WindowUtil.setWindow(root, dialog, upcomingButton);
            dialog.showAndWait();

        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
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
            WindowUtil.setWindow(root, dialog, upcomingButton);
            dialog.showAndWait();

        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }

    private void handleSetInProgress(Task task) {
        if (task.isCompleted()) {
            new WarningAlert("Can't mark as 'In Progress' - task is already completed.").showAndWait();
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

    private List<Task> getMyTasks() {
        filteredByUser = null;
        filteredByDepartment = null;
        return allTasksCache.stream()
                .filter(task ->
                        (task.getAssignedUser() != null &&
                                task.getAssignedUser().getId().equals(loggedInUser.getId()))
                                ||
                                (task.getAssignedDepartment() != null &&
                                        task.getAssignedDepartment().getId().equals(loggedInUser.getDepartment().getId()) &&
                                        task.getAssignedUser() == null)
                ).toList();
    }

    private void filterTasksBySearch(String query, List<Task> tasks) {
        List<Task> filtered = getFilteredTasksBySearch(query, tasks);
        taskTable.getItems().setAll(filtered);
    }

    private List<Task> getFilteredTasksBySearch(String query, List<Task> tasks) {
        return getFilteredTasksByCheckbox(tasks).stream()
                .filter(t -> t.getName().toLowerCase().contains(query.toLowerCase())
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
    }

    private List<Task> getFilteredTasksByCheckbox(List<Task> taskList) {
        List<Task> tasks;
        if (filterIncompleteCheckBox.isSelected()) tasks = getIncompleteTasks(taskList);
        else if (filterCompleteCheckBox.isSelected()) tasks = getCompleteTasks(taskList);
        else tasks = taskList;
        return tasks;
    }

    private List<Task> getIncompleteTasks(List<Task> tasks) {
        return tasks
                .stream()
                .filter(t -> !t.isCompleted())
                .toList();
    }

    private List<Task> getCompleteTasks(List<Task> tasks) {
        return tasks
                .stream()
                .filter(Task::isCompleted)
                .toList();
    }

    @FXML
    public void filterForUserOrDepartment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/filter_user_department_view.fxml"));
            Parent root = loader.load();

            // getting pop-up controller
            FilterUserDepartmentController filterController = loader.getController();
            // pass reference to this (UserViewController)
            filterController.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Filter by User/Department");

            WindowUtil.setWindow(root, stage, upcomingButton);
            stage.showAndWait();

            // After closing form -> Refresh the task table
            allTasksCache = taskDAO.getAllTasks();
            loadTasks();
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void openAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/admin_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("AutoTasker Admin: " + loggedInUser.getUsername());
            WindowUtil.setWindow(root, stage, upcomingButton);
            stage.show();
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void showUpdatePasswordWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/change_user_password_view.fxml"));
            Parent root = loader.load();

            ChangeUserPasswordController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);
            Stage stage = new Stage();
            stage.setTitle("Change Password");
            WindowUtil.setWindow(root, stage, upcomingButton);
            stage.show();

        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }

    }
}