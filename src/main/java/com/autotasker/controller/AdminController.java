package com.autotasker.controller;

import com.autotasker.dao.DepartmentDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Department;
import com.autotasker.model.Role;
import com.autotasker.model.User;
import com.autotasker.util.JpaUtil;
import com.autotasker.util.WarningAlert;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.Objects;

public class AdminController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void createAdmin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Please fill all fields!");
            return;
        }

        if (!password.equals(confirm)) {
            showError("The passwords do not match!");
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            UserDAO userDAO = new UserDAO(em);
            DepartmentDAO departmentDAO = new DepartmentDAO(em);

            // create Department without manager
            Department department = new Department();
            department.setDepartmentName("Administration");
            department.setDepartmentManager(null); // temp null
            department = departmentDAO.saveInitDepartment(department);

            // create ADMIN with department
            User admin = new User();
            admin.setUsername(username);
            admin.setPasswordHash(hashedPassword);
            admin.setRole(Role.ADMIN);
            admin.setDepartment(department);
            admin = userDAO.saveInitUser(admin);


            // connect department to department-manager (same EM -> admin is managed)
            department.setDepartmentManager(admin);
            // merge not needed, admin managed in same transaction

            tx.commit();

            // navigation to login.fxml
            Stage newStage = new Stage();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/autotasker/view/login.fxml")));
            newStage.setScene(new Scene(root, 450, 200));
            newStage.setTitle("Login");
            newStage.show();

            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            if (tx.isActive()) tx.rollback();
            new WarningAlert("Error saving admin: " + e.getMessage()).showAndWait();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
