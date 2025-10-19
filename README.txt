Proyecto CFGS DAM Aitor Benito Heras

El proyecto TaskEasy corresponde a una aplicación de escritorio para gestionar tareas. 


* Requisitos del sistema:
	- Java JDK 17 instalado en el sistema operativo.
	- Intellij IDEA (Recomendado por ser el IDE usado para la creación de TaskEasy) o cualquier IDe compatible con Maven. 
	- Conexión a internet la primera vez que se usa el programa, para que maven descargue las dependencias necesarias. 

* ¿Como abro el proyecto en IntelliJ IDEA?:
	- Descomprime el archivo TaskEasy_ProyectoDAM_Intellij.zip en la carpeta que desees, recomiendo hacerlo en la carpeta donde tengas otros proyectos de IntelliJ
	- En IntelliJ IDEA abre File + Open + Selecciona el archivo pom.xml de la carpeta donde has descomprimido el proyecto
	- Una vez cargado el archivo, tendrás que esperar a que se descarguen todas las dependencias necesarias, habrá un mensaje que dice "Indexing...", espera a que termine.
	- Abre ** src/main/java/com/aitorbenito/TaskEasy/Main.java **
	- Ejecuta con CLICK DERECHO: ** Run 'Main.main()' **

* ¿Qué hace la aplicación?
	- Crear automáticamente la base de datos de SQLite (TaskEasy.db) en la carpeta raíz del proyecto. 
	- Muestra una tabla de tareas y un formulario para crear y editarlas. 
	- Tiene botones para Crear, Actualizar y Eliminar las tareas. 
	- Tiene un filtro por estado de las tareas, donde se encuentran: Todas las tareas, Tareas pendientes, Tareas completadas.

* Estructura del proyecto creado en Maven:
	- En el archivo ** pom.xml ** se encuentra la configuración de dependencias de JavaFX y SQLite
	- En la ruta ** src/main/java/com/aitorbenito/taskeasy ** se encuentra el código Java. (Main, Database, Tarea, TareaDAO, MainController)
	- En la ruta ** src/main/resources/view ** se encuentra la interfaz de JavaFX (main.fxml)

* Posibles errores que puede dar el programa: 

	- No se abre la ventana / error de JavaFX

		- Asegúrate que estás usando Java JDK 17 y que el proyecto está importado con MAVEN.
		- Vuelve a ejecutar ** Run 'Main.main()' ** tras finalizar la descarga de dependencias, si lo ejecutas antes de que termine dará error. 

	- Error con SQLite JDBC:

		- Deja que MAVEN termine de descargarlo por completo. 
		- Si no has dejado descargar por completo y lo has intentado ejecutar, fallará aunque hayas esperado que se descargue del todo y lo ejecutes de nuevo. 
		- En caso de que no funcione después de descárgalo todo, deberías eliminarlo y hacer un "Rebuild Project" y esperar para que se cargue correctamente. 

	- FXML NO encontrado:

		- Verifica que el archivo existe en ** src/main/resources/view/main.fxml **
		- Comprueba que el recurso se carga con la ruta ** view/main.fmxl **

	- No aparece la base de datos:

		- Se crea en el primer arranque del programa. Revise que existe ** taskeasy.db ** en la raíz del proyecto. 