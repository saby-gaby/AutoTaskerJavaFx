package com.autotasker.util;

import javafx.scene.control.Alert;

public class ConfirmationUtil extends Alert {

    public ConfirmationUtil(String message) {
        super(AlertType.CONFIRMATION);
        this.setHeaderText(message);
    }
}
