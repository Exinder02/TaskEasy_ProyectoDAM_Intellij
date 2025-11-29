package com.aitorbenito.taskeasy;

/**Importamos java.sql para que se pueda usar JDBC y trabajar con sqlite en la base de datos.
  Sin este import no podremos contectarnos a la base de datos ni ejecutar comandos de sql necesarios para el uso de la app*/
import java.sql.*;

/**Importamos java.time.LocalDate para recoger la fecha y convertirla en texto*/
import java.time.LocalDate;

/** Importamos java.time.format.DateTimeFormater para el formato de la fecha de las tareas.*/
import java.time.format.DateTimeFormatter;


public class Database {

    /**
     * Ruta del archivo SQLite donde se guardará la base de datos.
     * Si el archivo no existe, SQLite lo crea automáticamente.
     */
    private static final String URL = "jdbc:sqlite:taskeasy.db";

    /**
     * Metodo encargado de inicializar la base de datos.
     * <p>
     * Su función principal es:
     * - Crear las tablas necesarias si no existen.
     * - Añadir columnas nuevas si son necesarias (migración de base de datos).
     * - Insertar datos iniciales en caso de primera ejecución.
     * <p>
     * Este metodo se ejecuta UNA vez al iniciar la aplicación.
     */
    public static void ensureInitialized() {

        try (
                // Abrimos la conexión con SQLite
                Connection conexion = DriverManager.getConnection(URL);

                // Creamos un objeto Statement para ejecutar sentencias SQL simples
                Statement stmt = conexion.createStatement()
        ) {

            /** -------------------------------------------
             CREACIÓN DE LA TABLA 'tareas'
             -------------------------------------------
             Esta tabla almacena todas las tareas creadas por los usuarios.
             Usamos IF NOT EXISTS porque evita que nos de error si la tabla ya existe.*/

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS tareas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,   -- Identificador único de la tarea
                            titulo TEXT NOT NULL,                  -- Título obligatorio
                            descripcion TEXT,                      -- Texto opcional
                            fecha TEXT,                            -- Fecha en formato dd/MM/yyyy
                            estado TEXT                            -- Estado de la tarea
                        );
                    """);

            /** -------------------------------------------
             CREACIÓN DE LA TABLA 'usuarios'
             -------------------------------------------
             Esta tabla permite tener múltiples usuarios,
             cada uno con su propia lista de tareas*/
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS usuarios (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,  -- Identificador de usuario
                            nombre TEXT NOT NULL,                  -- Nombre visible
                            email TEXT UNIQUE NOT NULL,            -- Email único
                            password TEXT NOT NULL                 -- Contraseña cifrada o texto (según implementación)
                        );
                    """);


            /** -------------------------------------------
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

                // Si no existe → la añadimos automáticamente
                if (!existeUsuarioId) {
                    stmt.execute("ALTER TABLE tareas ADD COLUMN usuario_id INTEGER DEFAULT 0;");
                    System.out.println("✅ Columna 'usuario_id' añadida correctamente a la tabla tareas.");
                }
            }

            /** -------------------------------------------
             INSERTAR UNA TAREA DE EJEMPLO SI LA TABLA ESTÁ VACÍA
             -------------------------------------------
             Esto mejora la experiencia del usuario al iniciar la app por primera vez.*/
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM tareas;");

            // Si no hay ninguna tarea → insertamos la “tarea de bienvenida”
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
            /** Si ocurre cualquier error al modificar datos en la base de datos, imprimimos el error.
             Es importante para detectar errores en tiempo de desarrollo.*/
            e.printStackTrace();

        }
    }



    /** Ejecuta sentencias INSERT, UPDATE o DELETE*/
    public static void ejecutar(String sql, Object... params) {

        try (
                // Abre una conexión temporal a la base de datos usando la URL definida
                Connection conn = DriverManager.getConnection(URL);

                // Prepara la sentencia SQL con parámetros (?) para evitar inyección SQL
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            // Formateador para convertir LocalDate al formato dd/MM/yyyy antes de guardarlo
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Recorremos los parámetros enviados al método para insertarlos en la consulta SQL
            for (int i = 0; i < params.length; i++) {

                Object param = params[i];

                // Si el parámetro es una fecha LocalDate, se convierte a texto con formato personalizado
                if (param instanceof LocalDate) {
                    stmt.setString(i + 1, ((LocalDate) param).format(fmt)); // Guardar LocalDate como texto legible
                } else {
                    // Para cualquier otro tipo (String, Integer, etc.)
                    stmt.setObject(i + 1, param);
                }
            }

            // Ejecuta la sentencia SQL (INSERT, UPDATE o DELETE)
            stmt.executeUpdate();

        } catch (SQLException e) {
            // Muestra por consola el error si sucede algo con la base de datos
            e.printStackTrace();
        }
    }


    // Ejecuta consultas SELECT y devuelve un ResultSet
    public static ResultSet consultar(String sql, Object... params) throws SQLException {

        // Abre una conexión directa a la base de datos
        Connection conn = DriverManager.getConnection(URL);

        // Prepara la consulta SQL con parámetros
        PreparedStatement ps = conn.prepareStatement(sql);

        // Inserta los parámetros en la consulta
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }

        // Ejecuta la consulta y devuelve un ResultSet con los datos obtenidos
        // (La conexión NO se cierra aquí porque el ResultSet depende de ella)
        return ps.executeQuery();
    }
}