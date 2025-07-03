package com.autotasker.util;

import javafx.scene.control.Alert;

public class InformationAlert extends Alert {

    public InformationAlert(String message) {
        super(AlertType.INFORMATION);
        this.setTitle("Information");
        this.setHeaderText(message);
    }
}
