TaskEasy Gestor de tareas. (Proyecto Final CFGS DAM)

Autor: Aitor Benito Heras (ExInDer)
Versión: 1.0
Año: 2025

Descripción del proyecto:
TaskEasy es una aplicación de escritorio multiplataforma diseñada para gestionar tareas de forma sencilla e intuitiva con persistencia local.

Esta Aplicación ha sido desarrollada como Proyecto Final del CFGS DAM en Ilerna.

Incluye:

- Sistema de usuarios (login y registro).
- Tareas persistentes por usuario.
- Base de datos SQLite integrada.
- Interfaz en JavaFX moderna.
- Modo claro / modo oscuro.
- Colores según estado de tarea.
- CRUD completo: Crear, Editar, Eliminar.
- Paquete instalable para Windows (.exe) y Linux (.deb).


Requisitos del sistema:
Para usuarios finales (instaladores .exe y .deb)

No requiere JDK ni Maven ni dependencias externas.

Los instaladores incluyen:

Java Runtime personalizado (JDK 21 recortado con jlink)

Todas las dependencias JavaFX

SQLite JDBC integrado

Sistema operativo compatible:

- Windows 10 / 11 (64 bits)

- Ubuntu / Debian y derivados (64 bits)


Para desarrolladores / profesores que quieran abrir el código en IntelliJ
- Java JDK 21
- IntelliJ IDEA (recomendado)
- Conexión a internet la primera vez para que Maven descargue dependencias



Cómo abrir el proyecto en IntelliJ IDEA

- Descomprimir TaskEasy_ProyectoDAM_Intellij.zip
- Abrir IntelliJ → File → Open
- Seleccionar el archivo pom.xml
- Esperar a que Maven descargue todas las dependencias (mensaje "Indexing...")
- Abrir:src/main/java/com/aitorbenito/taskeasy/Main.java
- Ejecutar: Run 'Main.main()'


Instalación para usuarios finales
Windows (.exe)

- Ejecutar el archivo TaskEasy-1.0.exe
- El instalador añadirá acceso directo al menú Inicio
- Abrir "TaskEasy" desde el acceso directo


Linux 
- (.deb) Doble clic → “Instalar con Software” 
- O bien, vía terminal: sudo dpkg -i TaskEasy_1.0_amd64.deb
- La app aparecerá en el menú de aplicaciones.


Funcionamiento interno de la base de datos

Al iniciar TaskEasy:

Si no existe la base de datos local → se copia automáticamente desde los recursos del programa.


Ubicación según sistema operativo:

- Windows %LOCALAPPDATA%\TaskEasy\taskeasy.db
- Linux / macOS ~/.local/share/TaskEasy/taskeasy.db


La base de datos incluida contiene:

Usuario 1 (para docentes)
Email: ilerna@ilerna.com
Usuario: ilerna
Contraseña: 12345

Usuario 2 (para alumnos)
Email: alumno@ilerna.com
Usuario: alumno
Contraseña: 54321


Ambos perfiles contienen tareas preconfiguradas para que se puedan probar todas las funcionalidades.

Estructura del proyecto (código fuente)

src/main/java/com/aitorbenito/taskeasy/
 ├─ Main.java                     (Punto de entrada de la app)
 ├─ BaseDeDatos.java              (Gestión de SQLite)
 ├─ ControladorLogueo.java        (Login)
 ├─ ControladorRegistro.java      (Registro de usuarios)
 ├─ ControladorPrincipal.java     (Ventana principal y CRUD)
 ├─ ControladorFormularioTareas.java (Formulario Crear/Editar)
 ├─ SesionUsuario.java            (Gestión de sesión)
 └─ Tarea.java                    (Modelo de datos)



Interfaces JavaFX:

src/main/resources/view/
 ├─ logueo.fxml
 ├─ registro.fxml
 ├─ main.fxml
 └─ formularioTareas.fxml



Temas visuales:

src/main/resources/css/
 ├─ temaClaro.css
 └─ temaOscuro.css



Base de datos incluida:

src/main/resources/taskeasy.db

Posibles errores y soluciones:

1. FXML no encontrado
Verificar rutas en: src/main/resources/view/


2. Error de dependencias en IntelliJ
Ejecutar: Maven → Reload Project

3. Error con SQLite en ejecución dentro del IDE
Borrar la base local: %LOCALAPPDATA%\TaskEasy\

TaskEasy la regenerará automáticamente.


Estado final del proyecto

- Código completo
- Base de datos incluida y funcional
- Usuarios y tareas precargadas
- Instaladores Windows y Linux creados con éxito
- App totalmente operativa en ambos SO. 