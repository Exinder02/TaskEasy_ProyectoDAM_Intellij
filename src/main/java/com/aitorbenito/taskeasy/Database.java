package com.aitorbenito.taskeasy;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Database {
    private static final String URL = "jdbc:sqlite:taskeasy.db";

    public static void ensureInitialized() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tareas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    titulo TEXT NOT NULL,
                    descripcion TEXT,
                    fecha TEXT,
                    estado TEXT
                )
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void ejecutar(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:tareas.db");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof LocalDate) {
                    // Guardar LocalDate en formato dd/MM/yyyy
                    stmt.setString(i + 1, ((LocalDate) param).format(fmt));
                } else {
                    stmt.setObject(i + 1, param);
                }
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static ResultSet consultar(String sql, Object... params) throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
        return ps.executeQuery();
    }
}
