package com.autotasker.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Stage;

public class WindowUtil {

    public static void setWindow(Parent parent, Stage stage, Control control) {
        // set parent to window (closing parent window closes this window too)
        stage.initOwner(control.getScene().getWindow());
        Scene scene = new Scene(parent);
        // put scene in new window
        stage.setScene(scene);
    }
}
