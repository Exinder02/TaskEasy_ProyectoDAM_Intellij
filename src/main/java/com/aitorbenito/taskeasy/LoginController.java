package com.aitorbenito.taskeasy;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;

    @FXML
    private void initialize() {
        Database.ensureInitialized();
    }

    @FXML
    private void login() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Introduce tu correo y contraseña.");
            return;
        }

        try (ResultSet rs = Database.consultar("SELECT * FROM usuarios WHERE email = ? AND password = ?", email, password)) {
            if (rs.next()) {
                int userId = rs.getInt("id");
                Session.setUsuarioActual(userId);
                showAlert("Bienvenido", "Inicio de sesión correcto.");
                closeWindow();
                Main.openMain();
            } else {
                showAlert("Error", "Credenciales incorrectas.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void register() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Rellena todos los campos para registrarte.");
            return;
        }

        try {
            Database.ejecutar("INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)",
                    email, email, password);
            showAlert("Registro exitoso", "Tu cuenta ha sido creada.");
        } catch (Exception e) {
            showAlert("Error", "El correo ya está en uso.");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
