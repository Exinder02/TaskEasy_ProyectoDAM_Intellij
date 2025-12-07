/*Creado por Aitor Benito Heras "ExInDer"*/
package com.aitorbenito.taskeasy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.util.Objects;

/*
     Clase principal (Main) de mi aplicación TaskEasy.

     Esta clase se extiende de "javafx.application.Application",
     lo que la hace el punto de entrada del proyecto por ser en JavaFX.

     Desde esta clase Main:
         - Se inicia JavaFX.
         - Se carga la primera ventana (el login).
         - Se gestiona la transición desde la ventana de login hacia la ventana principal

     El metodo "main()" lanza el sistema JavaFX, que a su vez llama automáticamente al metodo "start()"
*/
public class Main extends Application {

    /*
        Ruta del icono de la aplicación, la creamos aqui para que sea reutilizable
        en los dos metodos que tenemos a continuacion.
    */
    private static final String ICONO_PATH = "/images/TaskEasyGT_Icono.png";

    /*-----------------------
          Metodo Start:
      -----------------------
     Metodo que JavaFX ejecuta automáticamente al iniciar la aplicación.

     Aquí cargamos la primera ventana (Stage) que verá el usuario: el login.
     En este metodo hacemos:

     - Cargar el archivo FXML que define la interfaz de login.
     - Crear la escena con ese contenido.
     - Configurar el escenario (Stage) principal.
     - Mostrar la ventana.

     Stage o escenario, Ventana principal que crea automáticamente JavaFX.
     */
    @Override
    public void start(Stage escenario) throws Exception {

        /* Cargar el archivo FXML del login */
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/logueo.fxml"));
        Parent root = fxmlLoader.load();

        aplicarIcono(escenario);

        /* Configuración básica de la ventana de inicio de sesión, impide modificar tamaño */
        escenario.setTitle("TaskEasy — Inicio de sesión");
        escenario.setScene(new Scene(root, 400, 300));
        escenario.setResizable(false);  // Evitar redimensionar la ventana de login
        escenario.show();               // Mostrarla en pantalla
    }



    /* ----------------------------
            Metodo abrirMain
       ----------------------------

     Metodo estático utilizado por el ControladorLogueo para abrir la ventana principal.

     Este metodo se ejecuta únicamente después de que:
     - El usuario haya introducido credenciales correctas,
     - ControladorLogueo haya validado esos datos,
     - Se haya cerrado la ventana de login.

     ¿Qué hace este metodo?
     - Carga el archivo main.fxml, que contiene el diseño del gestor de tareas.
     - Crea una nueva ventana (Stage).
     - Aplica la escena y muestra el panel principal de la aplicación.

     Al crearse un nuevo Stage, no interfiere con el login que ya se cerró.
     */
    public static void abrirMain() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/main.fxml"));
            Parent root = fxmlLoader.load();

            Stage escenario = new Stage();
            Scene escena = new Scene(root, 900, 600);

            aplicarIcono(escenario);

            /* --------------------------------------
             INYECCIÓN DEL TEMA CLARO POR DEFECTO
             ----------------------------------------

            Usamos Objects.requireNonNull() para asegurar que el recurso
            ha sido encontrado antes de intentar llamar a .toExternalForm(), lo cual previene
            una NullPointerException si la ruta del CSS es incorrecta o el archivo no existe.
            */
            String temaClaroPath = Objects.requireNonNull(
                    Main.class.getResource("/css/temaClaro.css"),
                    "ERROR: No se encontró el archivo /css/temaClaro.css"
            ).toExternalForm(); // .toExternalForm() es NECESARIO para que JavaFX lo acepte como String URL.

            // Aplica la hoja de estilos inicial (Tema Claro) a la Scene.
            escena.getStylesheets().add(temaClaroPath);
            /*Se añade una propiedad a la Scene que MainController
            usará para saber qué tema está activo y poder alternar entre ellos*/
            escena.getProperties().put("dark-mode", false);

            escenario.setTitle("TaskEasy — Gestor de tareas");
            escenario.setScene(escena);
            escenario.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void aplicarIcono(Stage stage) {
        try {
            // Se carga la imagen desde los recursos del paquete.
            Image icono = new Image(
                    Objects.requireNonNull(
                            Main.class.getResourceAsStream(ICONO_PATH),
                            "ERROR: No se encontró el archivo de icono en la ruta: " + ICONO_PATH
                    )
            );
            // Se añade el icono al objeto Stage.
            stage.getIcons().add(icono);

        } catch (NullPointerException e) {
            // Si el icono no se encuentra, imprime el error pero permite que la aplicación continúe sin icono.
            System.err.println("ADVERTENCIA: No se pudo cargar el icono de la aplicación. " + e.getMessage());
        }
    }

    /*----------------------------------
       Metodo main tradicional en Java.
      ----------------------------------

     Su única función es llamar a `launch()`, que es el metodo encargado
     de arrancar el entorno gráfico JavaFX y, posteriormente, llamar a `start()``.
     */
    public static void main(String[] args) {
        launch(args);  // Arranca JavaFX
    }
}
