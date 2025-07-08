package com.autotasker.controller;

import com.autotasker.dao.UserDAO;
import com.autotasker.model.User;
import com.autotasker.util.InformationAlert;
import com.autotasker.util.WarningAlert;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class EditUserPasswordController {
    public Label changePswInfo;
    public PasswordField passwordField;
    public PasswordField confirmedPasswordField;
    public Button saveNewPsw;

    public void initWindow(User user) {
        changePswInfo.setText("Setting new password for user '" + user.getUsername() + "'");
        saveNewPsw.setOnAction(event -> updateUserPassword(user));
    }

    private void updateUserPassword(User user) {
        String password = passwordField.getText();
        String confirmedPassword = confirmedPasswordField.getText();

        System.out.println(password + password.isEmpty() + " " + confirmedPassword + confirmedPassword.isEmpty());

        if (password.isEmpty() || confirmedPassword.isEmpty()) {
            new WarningAlert("Enter a new password and confirm it.").showAndWait();
        } else if (password.equals(confirmedPassword)) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            user.setPasswordHash(hashedPassword);
            new UserDAO().updateUser(user);
            new InformationAlert("Password changed successfully").showAndWait();
            Stage currentStage = (Stage) confirmedPasswordField.getScene().getWindow();
            currentStage.close();
        } else {
            new WarningAlert("Passwords do not match.").showAndWait();
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) saveNewPsw.getScene().getWindow();
        stage.close();
    }
}
