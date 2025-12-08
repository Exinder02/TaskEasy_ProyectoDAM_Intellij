/*Creado por Aitor Benito Heras "ExInDer"*/
package com.aitorbenito.taskeasy;

/* Importamos java.sql para que se pueda usar JDBC y trabajar con sqlite en la base de datos.
  Sin este import no podremos conectarnos a la base de datos ni ejecutar comandos de sql necesarios para el uso de la app*/
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.*;

/* Importamos java.time.LocalDate para recoger la fecha y convertirla en texto*/
import java.time.LocalDate;

/* Importamos java.time.format.DateTimeFormater para el formato de la fecha de las tareas*/
import java.time.format.DateTimeFormatter;

/*----------------------------

      Clase BaseDeDatos

  ----------------------------
Esta clase es la responsable de todas las comunicaciones con la base de datos SQLite
*/
public class BaseDeDatos {

    /*
     Ruta del archivo SQLite donde se guardará la base de datos.
     Si el archivo no existe, se copia automáticamente a LOCALAPPDATA.
     */
    private static final String URL;

    static {
        try {   /*
                    Obtenemos el nombre del sistema operativo (OS) donde se ejecuta la app
                    (“Windows 10”, “Linux”, “Mac OS X”) para decidir dónde guardar la base de datos.
                */
            String os = System.getProperty("os.name").toLowerCase();
            String dataDir;

            if (os.contains("win")) {
                /*
                Si es win (Windows) lo metemos en LOCALAPPDATA
                 */
                dataDir = System.getenv("LOCALAPPDATA") + File.separator + "TaskEasy";

            } else {
                /*
                 En caso de que sea Linux o macOS lo metemos en ~/.local/share/TaskEasy
                 */
                dataDir = System.getProperty("user.home")
                        + File.separator + ".local"
                        + File.separator + "share"
                        + File.separator + "TaskEasy";
            }
            /*
            Nos aseguramos de que exista la ruta de la base de datos para poder guardarlos
             */
            File carpeta = new File(dataDir);
            if (!carpeta.exists()) carpeta.mkdirs();

            /*
                Definimos como se debe crear la ruta final de la base de datos
            */
            File dbDestino = new File(dataDir + File.separator + "taskeasy.db");

            /*
             Si no existe base de datos creada localmente, la copia desde el JAR
             */
            if (!dbDestino.exists()) {
                InputStream inputStream = BaseDeDatos.class.getResourceAsStream("/taskeasy.db");

                if (inputStream != null) {
                    Files.copy(inputStream, dbDestino.toPath());
                } else {
                    throw new RuntimeException("No se encontró taskeasy.db dentro del instalador/JAR");
                }
            }

            /*Direccion final de la ruta de la base de datos*/
            URL = "jdbc:sqlite:" + dbDestino.getAbsolutePath();

        } catch (Exception excepcion) {
            throw new RuntimeException("No se pudo inicializar la base de datos", excepcion);
        }
    }



    /*-------------------------

      Metodo asegurarInicio

      -------------------------

     Metodo encargado de inicializar la base de datos.

     Su función principal es:
     - Crear las tablas necesarias si no existen.
     - Añadir columnas nuevas si son necesarias (migración de base de datos).
     - Insertar datos iniciales en caso de primera ejecución.

     Este metodo se ejecuta UNA vez al iniciar la aplicación.
     */
    public static void asegurarInicio() {

        try (
                // Abrimos la conexión con SQLite
                Connection conexion = DriverManager.getConnection(URL);

                // Creamos un objeto Statement para ejecutar sentencias SQL simples
                Statement stat = conexion.createStatement()
        ) {

            /* -------------------------------------------

                       CREACIÓN DE LA TABLA 'tareas'

               -------------------------------------------
             Esta tabla almacena todas las tareas creadas por los usuarios.
             Usamos IF NOT EXISTS porque evita que nos dé error si la tabla ya existe
             */
            stat.execute("""
                        CREATE TABLE IF NOT EXISTS tareas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,   -- Identificador único de la tarea
                            titulo TEXT NOT NULL,                   -- Título obligatorio
                            descripcion TEXT,                       -- Texto opcional
                            fecha TEXT,                             -- Fecha en formato dd/MM/yyyy
                            estado TEXT,                            -- Estado de la tarea
                            usuario_id INTEGER DEFAULT 0            -- Relación con el usuario
                        );
                    """);

            /*  -------------------------------------------

                     CREACIÓN DE LA TABLA 'categorias'

                -------------------------------------------
                Esta tabla permite asignar etiquetas a las tareas.
            */
            stat.execute("""
            CREATE TABLE IF NOT EXISTS categorias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                color TEXT
            );
        """);

            /*
                    INSERTAR CATEGORÍAS POR DEFECTO
                     (solo si la tabla está vacía)
            */
            try (ResultSet rs = stat.executeQuery("SELECT COUNT(*) AS total FROM categorias")) {
                if (rs.next() && rs.getInt("total") == 0) {
                    stat.execute("INSERT INTO categorias (nombre) VALUES ('Sin categoría');");
                    stat.execute("INSERT INTO categorias (nombre) VALUES ('Trabajo');");
                    stat.execute("INSERT INTO categorias (nombre) VALUES ('Personal');");
                    stat.execute("INSERT INTO categorias (nombre) VALUES ('Urgente');");
                    System.out.println("Categorías iniciales insertadas.");
                }
            }


            /* -------------------------------------------

                     CREACIÓN DE LA TABLA 'usuarios'

               -------------------------------------------
             Esta tabla permite tener múltiples usuarios, cada uno con su propia lista de tareas
             */
            stat.execute("""
                        CREATE TABLE IF NOT EXISTS usuarios (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,       -- Identificador de usuario
                            nombre TEXT UNIQUE NOT NULL,                -- Nombre visible
                            email TEXT UNIQUE NOT NULL,                 -- Email único
                            password TEXT NOT NULL                      -- Contraseña cifrada o texto (según implementación)
                        );
                    """);

            /* ---------------------------------------------------------------------------------

                         AÑADIR COLUMNA id_categoria A 'tareas' SI NO EXISTE

              ---------------------------------------------------------------------------------
            */
            try (ResultSet resultSet = stat.executeQuery("PRAGMA table_info(tareas);")) {

                boolean existeIdCategoria = false;

                while (resultSet.next()) {
                    if ("id_categoria".equalsIgnoreCase(resultSet.getString("name"))) {
                        existeIdCategoria = true;
                        break;
                    }
                }

                if (!existeIdCategoria) {
                    stat.execute("ALTER TABLE tareas ADD COLUMN id_categoria INTEGER DEFAULT NULL;");
                    System.out.println("Columna 'id_categoria' añadida correctamente a la tabla tareas.");
                }

            }

            /* -------------------------------------------------------

                AÑADIR COLUMNA usuario_id A 'tareas' SI NO EXISTE

               -------------------------------------------------------
             PRAGMA table_info devuelve el esquema de la tabla
             */
            try (ResultSet resultSet = stat.executeQuery("PRAGMA table_info(tareas);")) {

                boolean existeIdUsuario = false;

                // Recorremos la lista de columnas existentes en la tabla
                while (resultSet.next()) {
                    if ("usuario_id".equalsIgnoreCase(resultSet.getString("name"))) {
                        existeIdUsuario = true;
                        break;
                    }
                }

                if (!existeIdUsuario) {
                    stat.execute("ALTER TABLE tareas ADD COLUMN usuario_id INTEGER DEFAULT 0;");
                    System.out.println("Columna 'usuario_id' añadida correctamente a la tabla tareas.");
                }
            }

        } catch (SQLException excepcion) {
            excepcion.printStackTrace();
        }
    }



    /* ----------------------

          Metodo ejecutar

       ----------------------
       Sirve para ejecutar sentencias INSERT, UPDATE o DELETE
       */
    public static void ejecutar(String sql, Object... params) throws SQLException {

        try (
                Connection conexion = DriverManager.getConnection(URL);
                PreparedStatement prepstat = conexion.prepareStatement(sql)

        ) {
            DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (int i = 0; i < params.length; i++) {
                Object param = params[i];

                if (param instanceof LocalDate) {
                    prepstat.setString(i + 1, ((LocalDate) param).format(formatoFecha));
                } else {
                    prepstat.setObject(i + 1, param);
                }
            }

            prepstat.executeUpdate();

        } catch (SQLException excepcion) {
            throw excepcion;
        }
    }



    /*---------------------------

        Metodo consultar

      ---------------------------
      Ejecuta consultas SELECT y devuelve un ResultSet
      */
    public static ResultSet consultar(String sql, Object... params) throws SQLException {

        Connection conexion = DriverManager.getConnection(URL);

        try {
            PreparedStatement prepstat = conexion.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                prepstat.setObject(i + 1, params[i]);
            }

            return prepstat.executeQuery();

        } catch (SQLException e) {
            conexion.close();
            throw e;
        }

    }


    /* ----------------------------------

                Metodo existe
       ----------------------------------
    Metodo para verificar que ese campo es único.
    */
    public static boolean existe(String campo, String valor) {

        try (var conexion = DriverManager.getConnection(URL);
             var prepStat = conexion.prepareStatement("SELECT 1 FROM usuarios WHERE " + campo + " = ? LIMIT 1")) {

            prepStat.setString(1, valor);

            try (var resultSet = prepStat.executeQuery()) {
                return resultSet.next();
            }

        } catch (Exception excepcion) {
            excepcion.printStackTrace();
            return true;
        }

    }

    /* -------------------------------------------------------
                Obtener todas las categorías
   ------------------------------------------------------- */
    public static java.util.List<Categoria> obtenerCategorias() {
        java.util.List<Categoria> lista = new java.util.ArrayList<>();

        try (ResultSet rs = consultar("SELECT id, nombre, color FROM categorias ORDER BY id")) {
            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("color")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

}
