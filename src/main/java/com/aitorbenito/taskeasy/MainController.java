package com.aitorbenito.taskeasy;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/* ----------------------------------
      Clase MainController
   ----------------------------------
   Controlador principal de la aplicación, asociado a 'main.fxml'.
   Gestiona la visualización, la manipulación de tareas y las opciones de sesión.
   */
public class MainController {

    /* Inyección de elementos del FXML: La tabla y sus columnas. */
    @FXML private TableView<Tarea> tablaTareas;
    @FXML private TableColumn<Tarea, String> colTitulo;
    @FXML private TableColumn<Tarea, String> colDescripcion;
    @FXML private TableColumn<Tarea, String> colFecha;
    @FXML private TableColumn<Tarea, String> colEstado;

    /* Contenedores para elementos de interfaz (Ej. para la leyenda de colores). */
    @FXML private VBox contenedorBottom;
    @FXML private HBox contenedorLeyenda;

    /* Estructura de datos crucial: Lista que se enlaza al TableView (Data Binding).
       ObservableList permite que la tabla se actualice automáticamente al cambiar la lista. */
    private final ObservableList<Tarea> listaTareas = FXCollections.observableArrayList();

    /* Formateador de fecha reutilizable. */
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* --------------------------------
           Metodo initialize:
       --------------------------------
       Llamado automáticamente por JavaFX después de cargar el FXML.
       Se usa para:
       1. Configurar el 'Data Binding' de las columnas.
       2. Aplicar estilos y *callbacks* (doble clic, colores).
       3. Cargar los datos iniciales.
    */
    @FXML
    public void initialize() {

        // 1. CONFIGURACIÓN DE LAS CELDAS (Data Binding)
        // Se define cómo la propiedad de cada objeto Tarea se mapea a la columna.
        colTitulo.setCellValueFactory(data -> data.getValue().tituloProperty());
        colDescripcion.setCellValueFactory(data -> data.getValue().descripcionProperty());
        colFecha.setCellValueFactory(data -> data.getValue().fechaProperty());
        colEstado.setCellValueFactory(data -> data.getValue().estadoProperty());

        // 2. CONFIGURACIÓN PERSONALIZADA DE LA COLUMNA FECHA (Cell Factory)
        colFecha.setCellFactory(column -> new TableCell<Tarea, String>() {
            @Override
            protected void updateItem(String fecha, boolean empty) {
                super.updateItem(fecha, empty);

                if (empty) {
                    // Si la fila está vacía (no hay tarea), no ponemos texto.
                    setText(null);
                    setGraphic(null);
                    setStyle(""); // Limpiar cualquier estilo remanente
                } else if (fecha == null || fecha.trim().isEmpty()) {
                    // Lógica para el caso de TAREA con FECHA vacía/no establecida.
                    setText("Sin fecha establecida");
                    setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                } else {
                    // Mostrar la fecha normal.
                    setText(fecha);
                    setStyle("");
                }
            }
        });

        // Habilitar la selección de múltiples filas para la eliminación masiva.
        tablaTareas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Mensaje cuando la tabla está vacía.
        tablaTareas.setPlaceholder(new Label("No hay tareas disponibles. Usa el botón +"));

        // Se asegura de que la estructura de la DB exista. (Idempotente, ya se llamó en LoginController, pero no está de más.)
        Database.ensureInitialized();

        // ------------------------------------------------------------
        // CARGAR TAREAS EN LA TABLA
        // ------------------------------------------------------------
        cargarTareas();
        tablaTareas.setItems(listaTareas); // Enlazar la ObservableList a la tabla.

        // ------------------------------------------------------------
        // ROWFACTORY: LÓGICA DE COLORES POR ESTADO + EVENTO DOBLE CLIC
        // ------------------------------------------------------------
        tablaTareas.setRowFactory(tv -> {
            TableRow<Tarea> row = new TableRow<>() {
                @Override
                protected void updateItem(Tarea item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setStyle(""); // Limpiar estilo si no hay fila
                    } else {
                        // Aplica un color de fondo diferente a la fila basándose en el estado de la tarea.
                        String estado = item.getEstado();

                        // Protección contra estados nulos que podrían causar un fallo.
                        if (estado == null) {
                            estado = "sin estado";
                        }

                        // Uso de `switch` en la cadena de texto para aplicar estilos CSS.
                        switch (estado.toLowerCase()) {
                            case "completada":
                                setStyle("-fx-background-color: #b6f7b0;"); // Verde
                                break;
                            case "pendiente":
                                setStyle("-fx-background-color: #fff4a3;"); // Amarillo
                                break;
                            case "en curso":
                                setStyle("-fx-background-color: #cfe3ff;"); // Azul
                                break;
                            default:
                                setStyle("-fx-background-color: #ffd4a3;"); // Naranja suave
                                break;
                        }
                    }
                }
            };

            // Evento de doble clic: Si se hace doble clic en una fila, abre el formulario para editar.
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    abrirFormularioTarea(row.getItem());
                }
            });

            return row;
        });

        // Generar la leyenda de colores debajo de la tabla.
        crearLeyendaColores();
    }

    /* ----------------------------------------------------
       Metodo abrirFormularioTarea
       ----------------------------------------------------
       Método utilitario para abrir la ventana de TaskForm.fxml (edición/creación).
       Implementa el patrón de Diseño **Modal**, bloqueando la ventana principal.
       Utiliza un patrón de **Callback (función de retorno)**:
       pasa el método `cargarTareas` para que el TaskFormController lo llame al guardar,
       actualizando la tabla automáticamente.
    */
    private void abrirFormularioTarea(Tarea tarea) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/taskForm.fxml"));
            Parent root = loader.load();

            // Obtenemos el controlador del formulario.
            TaskFormController controller = loader.getController();
            // Inyectamos la tarea a editar (si es null, es una nueva tarea) y el callback.
            controller.configurar(tarea, this::cargarTareas);

            Stage stage = new Stage();
            // Modalidad: APPLICATION_MODAL bloquea la interacción con la ventana principal.
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle(tarea == null ? "Nueva tarea" : "Editar tarea");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de edición.");
        }
    }

    /* ----------------------------------------------------
           Métodos de acción (Botones)
       ---------------------------------------------------- */

    @FXML
    private void agregarTarea() {
        abrirFormularioTarea(null); // Pasa `null` para indicar que es una tarea nueva.
    }

    @FXML
    private void modificarTarea() {
        ObservableList<Tarea> seleccionadas = tablaTareas.getSelectionModel().getSelectedItems();

        if (seleccionadas.isEmpty()) {
            mostrarAlerta("Aviso", "Selecciona una tarea para modificar.");
            return;
        }

        if (seleccionadas.size() > 1) {
            // Se fuerza a que solo se pueda modificar una tarea a la vez.
            mostrarAlerta("Aviso", "Solo puedes modificar las tareas de una en una. Por favor, selecciona solo una.");
            return;
        }

        // Si solo hay una seleccionada, abre el formulario.
        Tarea seleccionada = seleccionadas.get(0);
        abrirFormularioTarea(seleccionada);
    }

    @FXML
    private void eliminarTarea() throws SQLException {
        ObservableList<Tarea> seleccionadas = tablaTareas.getSelectionModel().getSelectedItems();

        if (seleccionadas.isEmpty()) {
            mostrarAlerta("Aviso", "Selecciona una o varias tareas para eliminar.");
            return;
        }

        // Muestra un diálogo de confirmación antes de la operación destructiva.
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Confirmar eliminación");
        confirmar.setHeaderText("Eliminar tareas seleccionadas");

        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        // Itera sobre las seleccionadas y llama al método DAO (`Database.ejecutar`) para eliminarlas una a una.
        for (Tarea tarea : seleccionadas) {
            Database.ejecutar("DELETE FROM tareas WHERE id = ?", tarea.getId());
        }

        // Una vez eliminadas, recarga la tabla para reflejar los cambios.
        cargarTareas();
    }

    /* ----------------------------------------------------
           Metodo cargarTareas
       ----------------------------------------------------
       Método clave para la persistencia. Se comunica con la capa DAO (`Database`).
       Carga las tareas del usuario logueado (`Session.getUsuarioActual()`).
    */
    @FXML
    private void cargarTareas() {
        listaTareas.clear(); // Limpiar la lista actual para evitar duplicados.
        int usuarioId = Session.getUsuarioActual();
        if (usuarioId <= 0) return; // Si no hay sesión iniciada, sale.

        // Se usa `try-with-resources` para asegurar el cierre del ResultSet y la conexión.
        try (java.sql.ResultSet rs = Database.consultar(
                // Consulta las tareas asociadas al ID del usuario actual.
                "SELECT * FROM tareas WHERE usuario_id = ? ORDER BY fecha ASC",
                usuarioId
        )) {
            while (rs.next()) {
                // Mapeo de la fila del ResultSet a un objeto Tarea.
                listaTareas.add(new Tarea(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getString("fecha"),
                        rs.getString("estado")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar las tareas desde la base de datos.");
        }
    }

    /* ----------------------------------------------------
           Métodos de Sesión y Configuración
       ---------------------------------------------------- */

    @FXML
    private void cerrarSesion() {
        try {
            // 1. Limpia la sesión actual.
            Session.setUsuarioActual(0);
            // 2. Cierra la ventana principal actual.
            Stage current = (Stage) tablaTareas.getScene().getWindow();
            current.close();
            // 3. Abre una nueva instancia de la ventana de login.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Stage login = new Stage();
            login.setScene(new Scene(loader.load()));
            login.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void salirAplicacion() {
        // Cierra la aplicación JavaFX completamente.
        Platform.exit();
    }


    /* ----------------------------------------------------
             Metodo cambiarTema
         ----------------------------------------------------
         Alterna entre las hojas de estilo CSS para el modo claro y oscuro.
         La lógica se basa en la propiedad `dark-mode` almacenada en la Scene.
     */
    @FXML
    private void cambiarTema() {
        Scene scene = tablaTareas.getScene();
        if (scene == null) return; // Protección

        // Determinar el estado actual del modo oscuro.
        boolean esModoOscuroActual = scene.getProperties().getOrDefault("dark-mode", false).equals(true);

        // Obtener las rutas de los archivos CSS.
        String temaClaroPath = getClass().getResource("/css/temaClaro.css").toExternalForm();
        String temaOscuroPath = getClass().getResource("/css/temaOscuro.css").toExternalForm();

        // LÓGICA DE SWITCH: Quitar uno y poner el otro.
        if (esModoOscuroActual) {
            // Cambiar a CLARO
            scene.getStylesheets().remove(temaOscuroPath);
            scene.getStylesheets().add(temaClaroPath);
            scene.getProperties().put("dark-mode", false);

        } else {
            // Cambiar a OSCURO
            scene.getStylesheets().remove(temaClaroPath);
            scene.getStylesheets().add(temaOscuroPath);
            scene.getProperties().put("dark-mode", true);
        }
    }

    @FXML
    private void mostrarAcercaDe() {
        mostrarAlerta("Acerca de TaskEasy",
                "Versión 1.0\nDesarrollado por Aitor Benito Heras\nProyecto Final CFGS DAM - Ilerna Online");
    }

    /* ----------------------------------------------------
           Métodos Utilitarios (Leyenda y Alerta)
       ---------------------------------------------------- */

    // Construye dinámicamente la leyenda de colores y la añade al contenedor HBox.
    private void crearLeyendaColores() {
        if (contenedorLeyenda == null) return;

        contenedorLeyenda.setSpacing(10);
        contenedorLeyenda.getChildren().setAll(
                crearItemLeyenda("Completada", "#b6f7b0"),
                crearItemLeyenda("En curso", "#cfe3ff"),
                crearItemLeyenda("Pendiente", "#fff4a3"),
                crearItemLeyenda("Sin estado", "#ffd4a3")
        );
    }

    // Crea un componente HBox simple con un bloque de color y una etiqueta de texto.
    private HBox crearItemLeyenda(String texto, String colorHex) {
        HBox box = new HBox(5);
        Region color = new Region();
        color.setPrefSize(16, 16);
        color.setStyle("-fx-background-color:" + colorHex + "; -fx-border-color:#888;");
        box.getChildren().addAll(color, new Label(texto));
        return box;
    }

    @FXML
    private void nuevaTareaFlotante() {
        // Asume que este metodo está vinculado a un Floating Action Button (FAB) o similar.
        abrirFormularioTarea(null);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}