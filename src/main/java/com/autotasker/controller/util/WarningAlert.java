package com.autotasker.controller.util;

import javafx.scene.control.Alert;

public class WarningAlert {
    private final Alert alert;

    public WarningAlert(String message) {
        alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
    }

    // Show an alert with a given message
    public void showAlert() {
        alert.showAndWait();
    }
}
