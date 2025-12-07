/*Creado por Aitor Benito Heras "ExInDer"*/
package com.aitorbenito.taskeasy;
/*
Imports javaFX
*/
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;


/*
Import java SQL
*/
import java.sql.SQLException;

/*Clase RegisterController*/
public class ControladorRegistro {

    /*
    Elementos del FXML.
    */
    @FXML private TextField txtEmail;
    @FXML private TextField txtNombre;
    @FXML private PasswordField txtContraseña;



    /* ----------------------------------

               Metodo crearCuenta

       ----------------------------------
       Maneja el funcionamiento del botón de registro.
    */
    @FXML
    private void crearCuenta() throws SQLException {

        String email = txtEmail.getText().trim();
        String nombre = txtNombre.getText().trim();
        String password = txtContraseña.getText().trim();

        // Comprueba que no hay campos vacíos en el formulario
        if (email.isEmpty() || nombre.isEmpty() || password.isEmpty()) {
            mostrar("Campos incompletos", "Rellena todos los campos.");
            return;
        }

        // Comprueba si el email está duplicado
        if (BaseDeDatos.existe("email", email)) {
            mostrar("Email en uso", "Ese correo ya está registrado.");
            return;
        }

        // Comprueba si el nombre de usuario duplicado
        if (BaseDeDatos.existe("nombre", nombre)) {
            mostrar("Nombre en uso", "Ese nombre ya está registrado. Usa otro.");
            return;
        }

        // Si las validaciones pasan, inserta el nuevo usuario.
        // Utiliza el metodo genérico "BaseDeDatos.ejecutar"` para la operación INSERT del usuario en la base de datos.
        BaseDeDatos.ejecutar(
                "INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)",
                nombre, email, password
        );

        mostrar("Cuenta creada", "Tu usuario ha sido registrado correctamente.");
        cerrar(); // Cierra la ventana de registro.
    }



    /* ----------------------------------

                Metodo cerrar

       ----------------------------------
    Lo he creado para cerrar las ventanas
    */
    @FXML
    private void cerrar() {
        Stage escenario = (Stage) txtEmail.getScene().getWindow();
        escenario.close();
    }



    /* ----------------------------------

               Metodo  mostrar

       ----------------------------------
    */
    private void mostrar(String titulo, String mensaje) {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        aviso.setContentText(mensaje);
        aviso.showAndWait();
    }
}