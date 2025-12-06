package com.aitorbenito.taskeasy;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

/* ---------------------
   Clase LoginController
   ---------------------
 Controlador asociado a la vista 'login.fxml'.
 Se encarga de gestionar la interfaz de inicio de sesión:
 1. Valída las credenciales del usuario contra la base de datos.
 2. Inicia la sesión global del usuario (mediante la clase Session).
 3. Gestiona la navegación a la ventana principal (Main.openMain()).
 */
public class LoginController {

    /*Elementos de la interfaz de usuario (inyección FXML)*/
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContraseña;
    @FXML private Button btnLogeo;

    /* -------------------------
          Metodo inicializa:
       -------------------------

     Se encarga de la inicialización, es llamado automáticamente por JavaFX después de cargar el FXML.
     Se usa para tareas de configuración inicial necesarias.
     */
    @FXML
    private void inicializa() {
        /* Primero se asegura que la estructura de la base de datos exista antes de realizar cualquier intento de login.*/
        Database.asegurarInicio();
    }


    /* ----------------------
           Metodo login:
       ----------------------
     Maneja la acción del botón "Iniciar sesión".
     Es el metodo central de autenticación.
     */
    @FXML
    private void logueo() {
        /*Gestion de los datos introducidos en los campos de usuario y contraseña*/
        String userInput = txtUsuario.getText().trim();
        String password = txtContraseña.getText().trim();

        /*Validacion de campos vacios, en caso de que alguno esté vacio, o los dos,
         da un aviso de error, y solicita introducir los datos*/
        if (userInput.isEmpty() || password.isEmpty()) {
            mostrarAvisos("Error", "Introduce tu nombre / correo y contraseña.");
            return;
        }
        /*En caso de que los datos no estén vacios, hace una consulta a la base de datos,
         verificando por email y nombre en el primer campo, y la contraseña en el segundo*/
        try (ResultSet resulset = Database.consultar(
                "SELECT * FROM usuarios WHERE (email = ? OR nombre = ?) AND password = ?",
                userInput, userInput, password
        )) {
            /*Si la autenticacion es correcta pasa a la siguiente linea*/
            if (resulset.next()) {
                /*Si nos hemos logueado correctamente devuelve el ID del user*/
                int idUsuario = resulset.getInt("id");

                /*Iniciamos la sesion del usuario con ese ID concreto*/
                Session.setUsuarioActual(idUsuario);

                /*Nos devuelve un mensaje de bienvenida con el nombre del user que se ha logueado*/
                mostrarAvisos("Bienvenido",
                        "Has iniciado sesión como: " + resulset.getString("nombre"));

                /*Cerramos la ventana del login*/
                cerrarVentana();
                /*Abrimos la ventana del Main, que es la principal de la app*/
                Main.abrirMain();

                /*En cualquier caso que no encuentre coincidencia con los datos
                de los usuarios que hay en la base de datos, devuelve un mensaje de aviso*/
            } else {
                mostrarAvisos("Error", "Credenciales incorrectas.");
            }

            /*Aquí he metido un catch por si hubiera alguna otra excepción que no estuviese controlada,
            por ejemplo algún error con la base de datos.*/
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAvisos("Error", "Error inesperado al iniciar sesión.");
        }
    }


    /*------------------------------
            Metodo register:
      ------------------------------
     Es el encargado de manejar la accion del boton de registrarse,
     Abre una nueva ventana para el registro de un nuevo usuario con los campos: Email, Nombre, Contraseña
    */
    @FXML
    private void registro() {
        /*Carga la ventana del register.fxml*/
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Scene escena = new Scene(loader.load());

            Stage escenario = new Stage();
            escenario.setTitle("Crear cuenta");
            escenario.setScene(escena);
            escenario.setResizable(false);
            escenario.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*----------------------
        Metodo closeWindow
      ----------------------
     Su función es cerrar la ventana actual (el Stage).
     Se obtiene la referencia al Stage a través de cualquier control de la Scene.
     */
    private void cerrarVentana() {
        Stage escenario = (Stage) btnLogeo.getScene().getWindow();
        escenario.close();
    }



    /* --------------------
         Metodo showAlert:
       --------------------
     Metodo utilitario para mostrar mensajes de alerta al usuario.
    */
    private void mostrarAvisos(String titulo, String msg) {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        aviso.setContentText(msg);
        aviso.showAndWait();
    }
}
