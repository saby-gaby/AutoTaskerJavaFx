package com.autotasker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // load FXML file from right directory
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autotasker/main_scene.fxml"));
            VBox root = loader.load();

            // create scene
            Scene scene = new Scene(root, 1000, 600);

            // setup on main window
            primaryStage.setTitle("AutoTasker");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}