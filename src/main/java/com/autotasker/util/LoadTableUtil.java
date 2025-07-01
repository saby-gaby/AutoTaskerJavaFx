package com.autotasker.util;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.User;
import javafx.scene.control.TableView;

import java.util.ArrayList;

public class LoadTableUtil {

    public static void loadUserTable(TableView<User> table, UserDAO dao) {
        ArrayList<User> users = (ArrayList<User>) dao.findAll();
        SortListUtil.sortList(users, User.class);
        table.getItems().setAll(users);
    }

    public static void loadDepartmentTable(TableView<Department> table, DepartmentDAO dao) {
        ArrayList<Department> departments = (ArrayList<Department>) dao.findAll();
        SortListUtil.sortList(departments, Department.class);
        table.getItems().setAll(departments);
    }
}
