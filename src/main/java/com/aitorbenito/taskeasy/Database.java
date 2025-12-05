package com.aitorbenito.taskeasy;

/* Importamos java.sql para que se pueda usar JDBC y trabajar con sqlite en la base de datos.
  Sin este import no podremos contectarnos a la base de datos ni ejecutar comandos de sql necesarios para el uso de la app*/
import java.sql.*;

/* Importamos java.time.LocalDate para recoger la fecha y convertirla en texto*/
import java.time.LocalDate;

/* Importamos java.time.format.DateTimeFormater para el formato de la fecha de las tareas.*/
import java.time.format.DateTimeFormatter;

/*----------------------------
      Clase Database
  ----------------------------
Esta clase es la responsable de todas la comunicacion con la base de datos SQLite. Es la unica clase que controla SQL,
 así separamos la parte de la persistencia de los datos de la parte de los controllers.

 Esta arquitectura es parte del patrón DAO (Data Access Object)*/
public class Database {

    /*
     Ruta del archivo SQLite donde se guardará la base de datos.
     Si el archivo no existe, SQLite lo crea automáticamente.
     */
    private static final String URL = "jdbc:sqlite:taskeasy.db";

    /*-------------------------
      Metodo ensureInitialized
      -------------------------

     Metodo encargado de inicializar la base de datos.

     Su función principal es:
     - Crear las tablas necesarias si no existen.
     - Añadir columnas nuevas si son necesarias (migración de base de datos).
     - Insertar datos iniciales en caso de primera ejecución.

     Este metodo se ejecuta UNA vez al iniciar la aplicación.
     */
    public static void ensureInitialized() {

        try (
                // Abrimos la conexión con SQLite
                Connection conexion = DriverManager.getConnection(URL);

                // Creamos un objeto Statement para ejecutar sentencias SQL simples
                Statement stmt = conexion.createStatement()
        ) {

            /* -------------------------------------------
               CREACIÓN DE LA TABLA 'tareas'
               -------------------------------------------
             Esta tabla almacena todas las tareas creadas por los usuarios.
             Usamos IF NOT EXISTS porque evita que nos de error si la tabla ya existe.*/

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS tareas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,   -- Identificador único de la tarea
                            titulo TEXT NOT NULL,                   -- Título obligatorio
                            descripcion TEXT,                       -- Texto opcional
                            fecha TEXT,                             -- Fecha en formato dd/MM/yyyy
                            estado TEXT                             -- Estado de la tarea
                        );
                    """);

            /* -------------------------------------------
             CREACIÓN DE LA TABLA 'usuarios'
             -------------------------------------------
             Esta tabla permite tener múltiples usuarios,
             cada uno con su propia lista de tareas*/
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS usuarios (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,       -- Identificador de usuario
                            nombre TEXT UNIQUE NOT NULL,                -- Nombre visible
                            email TEXT UNIQUE NOT NULL,                 -- Email único
                            password TEXT NOT NULL                      -- Contraseña cifrada o texto (según implementación)
                        );
                    """);


            /* -------------------------------------------
             AÑADIR COLUMNA usuario_id A 'tareas' SI NO EXISTE
             -------------------------------------------
             Esta parte es importante para la evolución del sistema.
             Si la tabla tareas se creó antes de implementar usuarios,
             no tendría esta columna, así que debemos añadirla.

             PRAGMA table_info devuelve el esquema de la tabla.*/
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(tareas);")) {

                boolean existeUsuarioId = false;

                // Recorremos la lista de columnas existentes en la tabla
                while (rs.next()) {
                    // Si ya existe una columna llamada usuario_id → no la añadimos
                    if ("usuario_id".equalsIgnoreCase(rs.getString("name"))) {
                        existeUsuarioId = true;
                        break;
                    }
                }

                // Si no existe, la añadimos automáticamente y le damos un valor
                if (!existeUsuarioId) {
                    stmt.execute("ALTER TABLE tareas ADD COLUMN usuario_id INTEGER DEFAULT 0;");
                    System.out.println("✅ Columna 'usuario_id' añadida correctamente a la tabla tareas.");
                }
            }


        } catch (SQLException e) {
            /* Si ocurre cualquier error al modificar datos en la base de datos, imprimimos el error.
             Es importante para detectar errores en tiempo de desarrollo.*/
            e.printStackTrace();

        }
    }



    /* ----------------------
        Metodo ejecutar:
       ----------------------
       Sirve para ejecutar sentencias INSERT, UPDATE o DELETE*/
    public static void ejecutar(String sql, Object... params) throws SQLException {

        try (
                //Recursos: Conexión y PreparedStatement, cerrados automáticamente.
                Connection conn = DriverManager.getConnection(URL);
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Iteramos sobre los parámetros establecidos para asignarlos a los datos concretos del SQL.
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];

                // Convertimos objetos LocalDate al formato de texto deseado (dd/MM/yyyy)
                if (param instanceof LocalDate) {
                    stmt.setString(i + 1, ((LocalDate) param).format(fmt));
                } else {
                    // Para String, Integer, etc., usamos setObject
                    stmt.setObject(i + 1, param);
                }
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            /* Si se detecta alguna excepcion, con "throw e", se lanza la excepción para que el Controlador
            (ej. LoginController) pueda informar al usuario del fallo.*/
            throw e;
        }
    }

    /*---------------------------
        Metodo consultar
      ---------------------------
      Ejecuta consultas SELECT y devuelve un ResultSet
      */

    public static ResultSet consultar(String sql, Object... params) throws SQLException {
        /* La conexión debe manejarse con cuidado, ya que el ResultSet devuelto
           depende de esta conexión y no se puede cerrar automáticamente aquí*/
        Connection conn = DriverManager.getConnection(URL);

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            /* Asignación de parámetros (para prevenir Inyección SQL)*/
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            // Devolvemos el resultado. El llamador (MainController) debe cerrar el resultSet y la conexion.
            return ps.executeQuery();

        } catch (SQLException e) {
            // MUY IMPORTANTE: Si algo falla al crear el PreparedStatement, cerramos la conexión para evitar fugas
            conn.close();
            throw e;
        }
    }
}