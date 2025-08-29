package com.autotasker.util;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import java.util.ArrayList;
import java.util.List;

public class ComboboxUtil {
    public static void initializeAssignTaskDropdowns(DepartmentDAO departmentDAO, UserDAO userDAO, ComboBox<Department> departmentComboBox, ComboBox<User> userComboBox) {
        // load departments + null option
        ArrayList<Department> departmentList = new ArrayList<>(departmentDAO.findAll());
        SortListUtil.sortList(departmentList, Department.class);
        departmentList.add(0, null); // empty option
        initializeDropdown(departmentComboBox, departmentList);

        // load all users + null option
        ArrayList<User> userList = new ArrayList<>(userDAO.findAll());
        SortListUtil.sortList(userList, User.class);
        userList.add(0, null); // empty option
        initializeDropdown(userComboBox, userList);

        // CellFactory for Department
        departmentCellFactory(departmentComboBox);

        // CellFactory for User
        userCellFactory(userComboBox);

        // --- Dependency logic ---
        // when select Department → filter User list
        departmentComboBox.valueProperty().addListener((obs, oldDep, newDep) -> {
            if (newDep != null) {
                ArrayList<User> filtered = new ArrayList<>(userDAO.findAll().stream()
                        .filter(u -> u.getDepartment() != null && u.getDepartment().equals(newDep))
                        .toList());
                SortListUtil.sortList(filtered, User.class);
                filtered.add(0, null); // empty option
                userComboBox.setItems(FXCollections.observableArrayList(filtered));
                userComboBox.setDisable(false);
            } else {
                // if no department chose → show all users
                userComboBox.setItems(FXCollections.observableArrayList(userList));
                userComboBox.setDisable(false);
            }
        });

        // when select User → set department automatic (or unlock when nothing selected)
        userComboBox.valueProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                departmentComboBox.setValue(newUser.getDepartment());
                departmentComboBox.setDisable(true); // lock department
            } else {
                // no user selected -> unlock departments
                departmentComboBox.setDisable(false);
            }
        });
    }

    public static <T> void initializeSortedDropdown(ComboBox<T> comboBox, ArrayList<T> list, Class<T> clazz) {
        SortListUtil.sortList(list, clazz);
        initializeDropdown(comboBox, list);
    }

    public static <T> void initializeDropdown(ComboBox<T> comboBox, ArrayList<T> list) {
        ObservableList<T> itemsList = FXCollections.observableArrayList(list);
        comboBox.setItems(itemsList);
    }

    public static void userCellFactory(ComboBox<User> userComboBox) {
        // visible options
        userComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty ? null : (user == null ? "" : user.getUsername()));
            }
        });
        // selected option
        userComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty ? null : (user == null ? "Select User" : user.getUsername()));
            }
        });
    }

    public static void departmentCellFactory(ComboBox<Department> departmentComboBox) {
        // visible options
        departmentComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Department dep, boolean empty) {
                super.updateItem(dep, empty);
                setText(empty ? null : (dep == null ? "" : dep.getDepartmentName()));
            }
        });
        // selected option
        departmentComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Department dep, boolean empty) {
                super.updateItem(dep, empty);
                setText(empty ? null : (dep == null ? "Select Department" : dep.getDepartmentName()));
            }
        });
    }

    public static void initDepartmentManagerDropdown(
            DepartmentDAO departmentDAO,
            UserDAO userDAO,
            ComboBox<User> departmentManagerField,
            User currentDepartmentManager, Department currDepartment) {

        // department table has foreign key user(id) - one to one
        // find all department managers
        List<User> managers = departmentDAO.findAllDepartmentManagers();

        ArrayList<User> availableUsers;
        if (currDepartment == null) {
            availableUsers = new ArrayList<>();
        } else {
            // find all users who are not department managers
            availableUsers = new ArrayList<>(
                    userDAO.findAll().stream()
                            .filter(u -> !managers.contains(u))
                            .toList());

            // when edit onlyUsers from current department

            availableUsers = new ArrayList<>(availableUsers.stream()
                    .filter(u -> u.getDepartment().equals(currDepartment))
                    .toList());
            // sort alphabetically
            SortListUtil.sortList(availableUsers, User.class);
        }
        // empty option
        availableUsers.add(0, null);
        // if already there is manager
        if (currentDepartmentManager != null) {
            // add current manager first
            availableUsers.add(0, currentDepartmentManager);
        }

        // fill dropdown with available users
        ComboboxUtil.initializeDropdown(departmentManagerField, availableUsers);
        ComboboxUtil.userCellFactory(departmentManagerField);
    }
}
