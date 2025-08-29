package com.autotasker.controller;

import com.autotasker.dao.EmailDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Email;
import com.autotasker.model.User;
import com.autotasker.util.ComboboxUtil;
import com.autotasker.util.ConfirmationUtil;
import com.autotasker.util.InformationAlert;
import com.autotasker.util.SortListUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Optional;

public class EditEmailController {

    @FXML private TextField emailField;
    @FXML private ComboBox<User> ownerCombobox;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    private final UserDAO USER_DAO = new UserDAO();
    private final EmailDAO EMAIL_DAO = new EmailDAO();

    public void initFields(Email email) {
        this.emailField.setText(email.getEmail());
        ArrayList<User> users = new ArrayList<>((USER_DAO.findAll()));
        SortListUtil.sortList(users, User.class);
        ComboboxUtil.initializeDropdown(ownerCombobox, users);
        ComboboxUtil.userCellFactory(ownerCombobox);
        ownerCombobox.getSelectionModel().select(email.getOwner());
        
        saveButton.setOnAction(e -> updateEmail(email));
        deleteButton.setOnAction(e -> deleteEmail(email));
    }

    private void updateEmail(Email email) {
        String oldEmail = email.getEmail();
        String newEmail = emailField.getText();
        User oldOwner = email.getOwner();
        User newOwner = ownerCombobox.getSelectionModel().getSelectedItem();
        boolean isEmailUpdated;
        if (!oldEmail.equals(newEmail) || !oldOwner.equals(newOwner)) {
            email.setEmail(newEmail);
            email.setOwner(newOwner);
            isEmailUpdated = EMAIL_DAO.updateEmail(email);
        } else isEmailUpdated = false;

        new InformationAlert(
                isEmailUpdated ?
                        "Email updated successfully" :
                        "There are no changes to be saved!"
        ).showAndWait() ;

        if (isEmailUpdated) {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        }
    }

    private void deleteEmail(Email email) {
        if (email != null) {
            Optional<ButtonType> buttonType;
            String emailAddress = email.getEmail();
            buttonType = new ConfirmationUtil(
                    "Are you sure you want to delete email address '" + emailAddress + "'?"
            ).showAndWait();

            boolean isDeleted = false;
            if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
                isDeleted = EMAIL_DAO.deleteEmail(email);
            }

            if (isDeleted) {
                new InformationAlert("Email '" + email.getEmail() + "' deleted successfully!").showAndWait();
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        }
    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) ownerCombobox.getScene().getWindow();
        stage.close();
    }
}
