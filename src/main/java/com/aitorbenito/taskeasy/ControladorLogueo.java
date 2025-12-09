/*Creado por Aitor Benito Heras "ExInDer"*/
package com.aitorbenito.taskeasy;

/*
Imports javafx
*/
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

/*
Imports java.sql
*/
import java.sql.ResultSet;
import java.sql.SQLException;

/* ------------------------------------------

            Clase ControladorLogueo

   ------------------------------------------
 Controlador asociado a la vista 'logueo.fxml'.
 Se encarga de gestionar la interfaz de inicio de sesión:
 - Valída las credenciales del usuario
 - Inicia la sesión del usuario (mediante la clase SesionUsuario).
 - Gestiona la navegación a la ventana principal (Main.abrirMain()).
 */
public class ControladorLogueo {
    //Boton registro
    public Button btnRegister;
    /*
     Elementos de la interfaz de usuario
    */
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContraseña;
    @FXML private Button btnLogeo;


    /* ----------------------

           Metodo logueo

       ----------------------
     Maneja la acción del botón que usamos para Iniciar sesión
     */
    @FXML
    private void logueo() {
        /*
        Gestion de los datos introducidos en los campos de usuario y contraseña
        */
        String userInput = txtUsuario.getText().trim();
        String password = txtContraseña.getText().trim();

        /*
        Valida los campos vacíos, en caso de que alguno esté vacío,
         da un aviso de error y solicita introducir los datos
         */
        if (userInput.isEmpty() || password.isEmpty()) {
            mostrarAvisos("Error", "Introduce tu nombre / correo y contraseña.");
            return;
        }
        /*
        En caso de que los datos no estén vacíos:
        - Hace una consulta a la base de datos
        - Verifica por email y nombre de usuario en el primer campo
        - Verifica la contraseña en el segundo campo
        */
        try (ResultSet resulset = BaseDeDatos.consultar(
                "SELECT * FROM usuarios WHERE (email = ? OR nombre = ?) AND password = ?",
                userInput, userInput, password
        )) {
            /*
            Si las credenciales son correctas pasa a la siguiente línea
            */
            if (resulset.next()) {
                /*
                Si nos hemos logueado correctamente devuelve el ID del user
                */
                int idUsuario = resulset.getInt("id");

                /*
                Iniciamos la sesion del usuario con ese ID concreto
                */
                SesionUsuario.setUsuarioActual(idUsuario);

                /*
                Nos devuelve un mensaje de bienvenida con el nombre del user que se ha logueado
                */
                mostrarAvisos("Bienvenido",
                        "Has iniciado sesión como: " + resulset.getString("nombre"));

                /*
                Cerramos la ventana del login
                */
                cerrarVentana();
                /*
                Abrimos la ventana del Main, que es la principal de la app
                */
                Main.abrirMain();

                /*
                En caso de que no encuentre coincidencia con los datos introducidos
                por pantalla con los usuarios que hay en la base de datos,
                devuelve un mensaje de aviso
                */
            } else {
                mostrarAvisos("Error", "Credenciales incorrectas.");
            }

            /*
            Aquí he metido un catch por si hubiera alguna otra excepción
            que no estuviese controlada, por ejemplo algún error con la base de datos.
            */
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAvisos("Error", "Error inesperado al iniciar sesión.");
        }
    }


    /*------------------------------

            Metodo registro

      ------------------------------
     Este metodo maneja la acción del botón de registrarse
     Abre una nueva ventana para el registro de un usuario con los campos obligatorios:
      - Email
      - Nombre
      - Contraseña
    */
    @FXML
    private void registro() {
        /*
        Carga la ventana del registro.fxml
        */
        try {
            FXMLLoader cargadorFXML = new FXMLLoader(getClass().getResource("/view/registro.fxml"));
            Scene escena = new Scene(cargadorFXML.load());

            Stage escenario = new Stage();
            escenario.setTitle("Crear cuenta");
            escenario.setScene(escena);
            escenario.setResizable(false);
            escenario.show();

            /*Capturamos las excepciones que puedan salir*/
        } catch (Exception excepcion) {
            excepcion.printStackTrace();
        }
    }


    /* -----------------------------------

             Metodo cerrarVentana

       -----------------------------------
     Su función es cerrar la ventana actual (el Stage).
     Se obtiene la referencia al Stage a través de cualquier control de la Scene.
     */
    private void cerrarVentana() {
        Stage escenario = (Stage) btnLogeo.getScene().getWindow();
        escenario.close();
    }



    /* ---------------------------------

             Metodo mostrarAvisos

       ---------------------------------
     Metodo para mostrar mensajes de alerta
    */
    private void mostrarAvisos(String titulo, String msg) {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        aviso.setContentText(msg);
        aviso.showAndWait();
    }
}
