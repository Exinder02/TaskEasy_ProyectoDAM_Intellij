package com.aitorbenito.taskeasy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Cargamos primero la ventana de inicio de sesión
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = fxmlLoader.load();

        stage.setTitle("TaskEasy — Inicio de sesión");
        stage.setScene(new Scene(root, 400, 300));
        stage.setResizable(false);
        stage.show();
    }

    // Método que usará LoginController para abrir la ventana principal tras el login
    public static void openMain() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/main.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("TaskEasy — Gestor de tareas");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
