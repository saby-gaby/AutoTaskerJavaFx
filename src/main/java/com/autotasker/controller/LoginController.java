package com.autotasker.controller;

import com.autotasker.dao.UserDAO;
import com.autotasker.model.User;
import com.autotasker.util.WarningAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password =   passwordField.getText();

        User user = userDAO.findByUsername(username);

        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {

            try {
                // load FXML file from right directory
                Stage newStage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/user_view_view/user_view_scene.fxml"));
                Parent root = loader.load();

                UserViewController userViewController = loader.getController();
                userViewController.setLoggedInUser(user);
                userViewController.greetUser();

                newStage.setScene(new Scene(root));
                newStage.setTitle("Autotasker");
                newStage.show();

                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                new WarningAlert(e.getMessage()).showAndWait();
            }
        } else {
            new WarningAlert("Invalid username or password").showAndWait();
        }
    }
}
