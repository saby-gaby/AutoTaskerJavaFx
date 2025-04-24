package com.autotasker;

import com.autotasker.controller.util.WarningAlert;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // load FXML file from right directory
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/view/main_scene.fxml"));
            VBox root = loader.load();

            // create scene
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/autotasker/view/styles.css")).toExternalForm());

            // setup on main window
            primaryStage.setTitle("AutoTasker");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            new WarningAlert(e.getMessage()).showAlert();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}