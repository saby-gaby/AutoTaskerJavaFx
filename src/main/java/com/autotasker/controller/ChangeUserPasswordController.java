package com.autotasker.controller;

import com.autotasker.dao.UserDAO;
import com.autotasker.model.User;
import com.autotasker.util.InformationAlert;
import com.autotasker.util.WarningAlert;
import jakarta.persistence.PreUpdate;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class ChangeUserPasswordController {
    @FXML private Label changePswInfo;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmedPasswordField;
    @FXML private Button saveNewPsw;

    private User loggedInUser;
    UserDAO userDAO = new UserDAO();

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        changePswInfo.setText("Changing Password for user '" +
                loggedInUser.getUsername()+"'.");
    }

    @FXML
    private void initialize() {
        saveNewPsw.setOnAction(event -> changePassword());
    }

    public void changePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmedPassword = confirmedPasswordField.getText();

        if (oldPassword.isEmpty() || !BCrypt.checkpw(oldPassword, loggedInUser.getPasswordHash())) {
            new WarningAlert("Current password is incorrect").showAndWait();
            return;
        }else if (newPassword.isEmpty() || confirmedPassword.isEmpty()) {
            new WarningAlert("Please enter a new password and confirm it.").showAndWait();
            return;
        }else if (!newPassword.equals(confirmedPassword)) {
            new WarningAlert("Passwords do not match.").showAndWait();
            return;
        }

        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        loggedInUser.setPasswordHash(hashedPassword);
        boolean isPasswordUpdated = userDAO.updateUser(loggedInUser);
        if (isPasswordUpdated) {
            new InformationAlert("Password changed successfully.").showAndWait();
            Stage stage = (Stage) saveNewPsw.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) saveNewPsw.getScene().getWindow();
        stage.close();
    }


}
