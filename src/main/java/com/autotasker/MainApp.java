package com.autotasker;

import com.autotasker.util.JpaUtil;
import com.autotasker.util.WarningAlert;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        boolean hasUsers = JpaUtil.hasAnyUsers();

        String fxmlToLoad;
        String title;

        if (hasUsers) {
            fxmlToLoad = "/com/autotasker/view/login_view/login.fxml";
            title = "Login";
        } else {
            fxmlToLoad = "/com/autotasker/view/create_admin.fxml";
            title = "Create Admin";
        }

        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlToLoad)));
            primaryStage.setScene(new Scene(root, 450, 200));
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}