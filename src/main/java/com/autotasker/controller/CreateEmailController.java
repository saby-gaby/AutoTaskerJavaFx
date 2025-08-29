package com.autotasker.controller;

import com.autotasker.dao.EmailDAO;
import com.autotasker.dao.UserDAO;
import com.autotasker.model.Email;
import com.autotasker.model.User;
import com.autotasker.util.ComboboxUtil;
import com.autotasker.util.InformationAlert;
import com.autotasker.util.SortListUtil;
import com.autotasker.util.WarningAlert;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class CreateEmailController {
    @FXML private TextField emailField;
    @FXML private ComboBox<User> ownerComboBox;

    private final EmailDAO EMAIL_DAO = new EmailDAO();
    private final UserDAO USER_DAO = new UserDAO();

    @FXML
    public void initialize() {
        ArrayList<User> users = new ArrayList<>((USER_DAO.findAll()));
        SortListUtil.sortList(users, User.class);
        ComboboxUtil.initializeDropdown(ownerComboBox, users);
        ComboboxUtil.userCellFactory(ownerComboBox);
    }

    @FXML
    public void handleAddEmail() {
        String email = emailField.getText().trim();
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!Pattern.compile(emailRegex).matcher(email).matches()) {
            new WarningAlert("Email address is incorrect!").showAndWait();
            return;
        }
        User owner = ownerComboBox.getSelectionModel().getSelectedItem();
        if (owner == null) {
            new WarningAlert("You must select an owner!").showAndWait();
            return;
        }
        Email emailEntry = new Email();
        emailEntry.setEmail(email);
        emailEntry.setOwner(owner);
        emailEntry = EMAIL_DAO.insertEmail(emailEntry);

        if (emailEntry != null) {
            String message = String.format("Successfully created new email address: %s", emailEntry.getEmail());
            new InformationAlert(message).showAndWait();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
}
