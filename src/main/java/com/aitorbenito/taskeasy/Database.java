package com.aitorbenito.taskeasy;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Database {
    private static final String URL = "jdbc:sqlite:taskeasy.db";

    // Inicializa la base de datos y crea las tablas si no existen
    public static void ensureInitialized() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Crear tabla de tareas si no existe
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tareas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    titulo TEXT NOT NULL,
                    descripcion TEXT,
                    fecha TEXT,
                    estado TEXT
                );
            """);

            // Crear tabla de usuarios si no existe
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                );
            """);

            // ✅ Comprobar si la columna usuario_id ya existe antes de añadirla
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(tareas);")) {
                boolean existeUsuarioId = false;
                while (rs.next()) {
                    if ("usuario_id".equalsIgnoreCase(rs.getString("name"))) {
                        existeUsuarioId = true;
                        break;
                    }
                }
                if (!existeUsuarioId) {
                    stmt.execute("ALTER TABLE tareas ADD COLUMN usuario_id INTEGER DEFAULT 0;");
                    System.out.println("✅ Columna 'usuario_id' añadida correctamente a la tabla tareas.");
                }
            }

            // Si no hay tareas, insertar una de ejemplo (solo la primera vez)
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM tareas;");
            if (rs.next() && rs.getInt("total") == 0) {
                stmt.executeUpdate("""
                    INSERT INTO tareas (titulo, descripcion, fecha, estado)
                    VALUES ('Bienvenido a TaskEasy',
                            'Puedes editar o eliminar esta tarea. Usa el botón Agregar para crear nuevas.',
                            'Sin fecha establecida',
                            'Pendiente');
                """);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ejecuta sentencias INSERT, UPDATE o DELETE
    public static void ejecutar(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof LocalDate) {
                    stmt.setString(i + 1, ((LocalDate) param).format(fmt)); // Guardar LocalDate como texto legible
                } else {
                    stmt.setObject(i + 1, param);
                }
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ejecuta consultas SELECT y devuelve un ResultSet
    public static ResultSet consultar(String sql, Object... params) throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps.executeQuery();
    }
}
