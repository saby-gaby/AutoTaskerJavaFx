package com.autotasker.util;

import javafx.scene.control.Alert;

public class WarningAlert extends Alert {

    public WarningAlert(String message) {
        super(Alert.AlertType.WARNING);
        this.setTitle("Warning");
        this.setHeaderText(message);
    }
}
