package com.aitorbenito.taskeasy;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RegisterController {

    /* Inyección de elementos del FXML. */
    @FXML private TextField txtEmail;
    @FXML private TextField txtNombre;
    @FXML private PasswordField txtPassword;

    /* ----------------------------------
       Metodo crearCuenta
       ----------------------------------
       Maneja el evento del botón de registro.
       Implementa la lógica de validación y persistencia.
    */
    @FXML
    private void crearCuenta() throws SQLException {

        String email = txtEmail.getText().trim();
        String nombre = txtNombre.getText().trim();
        String password = txtPassword.getText().trim();

        // **VALIDACIÓN 1**: Comprueba campos vacíos.
        if (email.isEmpty() || nombre.isEmpty() || password.isEmpty()) {
            mostrar("Campos incompletos", "Rellena todos los campos.");
            return;
        }

        // **VALIDACIÓN 2**: Comprueba email duplicado (Lógica de negocio clave).
        if (existe("email", email)) {
            mostrar("Email en uso", "Ese correo ya está registrado.");
            return;
        }

        // **VALIDACIÓN 3**: Comprueba nombre de usuario duplicado (Lógica de negocio clave).
        if (existe("nombre", nombre)) {
            mostrar("Nombre en uso", "Ese nombre ya está registrado. Usa otro.");
            return;
        }

        // **PERSISTENCIA**: Si las validaciones pasan, inserta el nuevo usuario.
        // Utiliza el método genérico `Database.ejecutar` para la operación INSERT.
        Database.ejecutar(
                "INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)",
                nombre, email, password
        );

        mostrar("Cuenta creada", "Tu usuario ha sido registrado correctamente.");
        cerrar(); // Cierra la ventana de registro.
    }

    /* ----------------------------------
       Metodo existe
       ----------------------------------
       Método utilitario para verificar la unicidad de un campo.
       IMPORTANTE: Este método DEBERÍA estar idealmente en la clase `Database`
       para centralizar toda la lógica SQL, siguiendo mejor el patrón DAO.
    */
    private boolean existe(String campo, String valor) {
        // Uso de try-with-resources para asegurar el cierre automático de Connection, PreparedStatement y ResultSet.
        try (var conn = java.sql.DriverManager.getConnection("jdbc:sqlite:taskeasy.db");
             var ps = conn.prepareStatement("SELECT 1 FROM usuarios WHERE " + campo + " = ? LIMIT 1")) {

            ps.setString(1, valor);
            try (var rs = ps.executeQuery()) {
                // Si `rs.next()` devuelve true, significa que se encontró al menos una fila (el valor ya existe).
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // **Manejo de errores**: En caso de fallo de BD, se asume que existe para prevenir un registro fallido (comportamiento de "falla seguro").
            return true;
        }
    }

    /* ----------------------------------
       Métodos de Utilidad (cerrar y mostrar)
       ----------------------------------
       Funcionalidad estándar de interfaz (UI).
    */
    @FXML
    private void cerrar() {
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        stage.close();
    }

    private void mostrar(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}