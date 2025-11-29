package com.aitorbenito.taskeasy;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;


    /**
     * Método initialize()
     * --------------------
     * Este método se ejecuta automáticamente cuando se carga la vista del Login (login.fxml).
     *
     * Aquí lo usamos para asegurarnos de que la base de datos esté correctamente creada
     * antes de que el usuario intente iniciar sesión o registrarse.
     *
     * - Si es la primera vez que se abre la app, se crearán las tablas necesarias.
     * - Si ya existe la base de datos, no hace nada.
     *
     * Es una forma de garantizar que el sistema siempre tiene la infraestructura mínima
     * para funcionar.
     */
    @FXML
    private void initialize() {
        Database.ensureInitialized();
    }


    /**
     * Método login()
     * ----------------
     * Este método se ejecuta cuando el usuario pulsa el botón "Iniciar sesión".
     *
     * 1. Obtiene los valores de email y contraseña escritos en los campos.
     * 2. Comprueba que no estén vacíos.
     * 3. Realiza una consulta SQL para verificar si existe un usuario con ese email y contraseña.
     * 4. Si existe:
     *      - Se guarda su ID en la clase Session.
     *      - Se muestra un mensaje de bienvenida.
     *      - Se cierra la ventana del login.
     *      - Se abre la ventana principal (Main.openMain()).
     * 5. Si NO existe, muestra un mensaje de error.
     *
     * Este método usa Database.consultar(), que devuelve un ResultSet.
     */
    @FXML
    private void login() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Introduce tu correo y contraseña.");
            return;
        }

        try (ResultSet rs = Database.consultar(
                "SELECT * FROM usuarios WHERE email = ? AND password = ?",
                email, password
        )) {

            if (rs.next()) {
                int userId = rs.getInt("id");

                /**
                 * Guardamos temporalmente el ID del usuario que inició sesión.
                 * Esto permite que otras partes de la aplicación sepan qué usuario está usando la app.
                 */
                Session.setUsuarioActual(userId);

                showAlert("Bienvenido", "Inicio de sesión correcto.");

                closeWindow();      // Cierra la ventana de login
                Main.openMain();    // Abre la ventana principal

            } else {
                showAlert("Error", "Credenciales incorrectas.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Método register()
     * -------------------
     * Este método se ejecuta cuando el usuario pulsa el botón "Registrarse".
     *
     * 1. Obtiene email y contraseña.
     * 2. Comprueba que no estén vacíos.
     * 3. Insertamos un nuevo usuario en la tabla 'usuarios'.
     *    - Usamos el email también como nombre temporal.
     * 4. Si el correo ya existe (porque tiene restricción UNIQUE):
     *      - Saltará una excepción y mostramos un mensaje de error.
     *
     * Este método NO inicia sesión automáticamente.
     * Simplemente crea la cuenta.
     */
    @FXML
    private void register() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Rellena todos los campos para registrarte.");
            return;
        }

        try {
            Database.ejecutar(
                    "INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)",
                    email, email, password
            );

            showAlert("Registro exitoso", "Tu cuenta ha sido creada.");

        } catch (Exception e) {
            showAlert("Error", "El correo ya está en uso.");
        }
    }


    /**
     * Cierra la ventana actual del login.
     * Se usa cuando el usuario inicia sesión correctamente.
     */
    private void closeWindow() {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        stage.close();
    }


    /**
     * Muestra un cuadro de diálogo informativo.
     * Se usa tanto para errores como para mensajes de éxito.
     *
     * @param titulo Título de la ventana emergente
     * @param msg    Mensaje que se muestra al usuario
     */
    private void showAlert(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
