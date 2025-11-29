package com.aitorbenito.taskeasy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 Clase principal (Main) de la aplicación TaskEasy.

 Esta clase se extiende de `javafx.application.Application`, lo que la hace
 el punto de entrada de cualquier proyecto JavaFX.

 Desde esta clase Main:

 - Se inicia JavaFX.
 - Se carga la primera ventana (el login).
 - Se gestiona la transición desde la ventana de login hacia la ventana principal.

 IMPORTANTE:
 El metodo `main()` NO abre ventanas. Solo lanza el sistema JavaFX,
 * que a su vez llama automáticamente al metodo `start()`.
 */

public class Main extends Application {

    /**
     Metodo que JavaFX ejecuta automáticamente al iniciar la aplicación.

     Aquí cargamos la primera ventana (Stage) que verá el usuario: el login.
     En este metodo hacemos:

     - Cargar el archivo FXML que define la interfaz de login.
     - Crear la escena con ese contenido.
     - Configurar el escenario (Stage) principal.
     - Mostrar la ventana.

     @param stage Ventana principal que crea automáticamente JavaFX.
     */
    @Override
    public void start(Stage stage) throws Exception {

        /** Cargar el archivo FXML del login */
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = fxmlLoader.load();

        /** Configuración básica de la ventana de inicio de sesión, impide modificar tamaño */
        stage.setTitle("TaskEasy — Inicio de sesión");
        stage.setScene(new Scene(root, 400, 300));
        stage.setResizable(false);  // Evitar redimensionar la ventana de login
        stage.show();               // Mostrarla en pantalla
    }

    /**
     Metodo estático utilizado por el LoginController para abrir la ventana principal.

     Este metodo se ejecuta únicamente después de que:
     - El usuario haya introducido credenciales correctas,
     - LoginController haya validado esos datos,
     - Se haya cerrado la ventana de login.

     ¿Qué hace este metodo?
     - Carga el archivo main.fxml, que contiene el diseño del gestor de tareas.
     - Crea una nueva ventana (Stage).
     - Aplica la escena y muestra el panel principal de la aplicación.

     Al crearse un nuevo Stage, no interfiere con el login que ya se cerró.

     */
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

    /**
     Metodo main tradicional en Java.

     Su única función es llamar a `launch()`, que pertenece a la clase Application
     y se encarga de arrancar el entorno gráfico JavaFX.

     Una vez ejecutado `launch()`, JavaFX llamará automáticamente al metodo `start(Stage stage)`.

     */
    public static void main(String[] args) {
        launch(args);  // Arranca JavaFX
    }
}
