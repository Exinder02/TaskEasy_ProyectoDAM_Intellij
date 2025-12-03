package com.aitorbenito.taskeasy;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;


import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private TableView<Tarea> tablaTareas;
    @FXML private TableColumn<Tarea, String> colTitulo;
    @FXML private TableColumn<Tarea, String> colDescripcion;
    @FXML private TableColumn<Tarea, String> colFecha;
    @FXML private TableColumn<Tarea, String> colEstado;
    @FXML private TextField textoTitulo;
    @FXML private TextArea textoDescripcion;
    @FXML private DatePicker dpFecha;
    @FXML private ChoiceBox<String> cbEstado;
    @FXML private HBox contenedorLeyenda;

    private boolean modoOscuro = false;
    private final ObservableList<Tarea> listaTareas = FXCollections.observableArrayList();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {

        //Mostrrar columna del titulo de la tarea
        //Se muestran los datos del titulo introducido en la tarea con data.getValue()
        colTitulo.setCellValueFactory(data -> data.getValue().tituloProperty());

        //Mostrar columan descripcion de la tarea.
        //Se muestran los datos de la descripcion introducida en la tareacon data.getValue()
        colDescripcion.setCellValueFactory(data -> data.getValue().descripcionProperty());


        //Mostrar columan fecha de la tarea.
        //Se muestran los datos de la fecha introducida en la tarea con data.getValue()
        colFecha.setCellValueFactory(data -> data.getValue().fechaProperty());


        //Mostrar columan estado de la tarea.
        //Se muestran los datos del estado introducida en la tarea con data.getValue().estadoProperty()
        colEstado.setCellValueFactory(data -> data.getValue().estadoProperty());

        // Mostrar "Sin fecha establecida" solo en tareas reales (no en filas vacías),
        // Pasrá siempre que la tarea haya sido creada sin fecha de fin establecida
        colFecha.setCellFactory(column -> new TableCell<>() {
            @Override
            //Creamos la funcion updateItem con la fecha en texto (ya que tenemos numero y caracereres como "/") y un boolean para verificar si está vacio vacio.
            protected void updateItem(String fecha, boolean empty) {

                //Hacemos un update de la fecha, si se encuentra vacia le decimos que el texto es null, que ponga tipo "" que es de texto.
                super.updateItem(fecha, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");

                    //Hacemos otro condicional para indicar que no hay fecha establecida
                } else if (fecha == null || fecha.trim().isEmpty()) {
                    setText("Sin fecha establecida");
                    setStyle("-fx-text-fill: gray; -fx-font-style: italic;");

                //Hacemos otro condicional, si se ha introducido fecha, poner el texto introducido.
                } else {
                    setText(fecha);
                    setStyle("");
                }
            }
        });

        // --- CONFIGURAR EL DATEPICKER ---
        dpFecha.setPromptText("dd/MM/yyyy");

        dpFecha.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? fmt.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.trim().isEmpty()) return null;
                try {
                    return LocalDate.parse(string, fmt);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        // Bloquear letras o caracteres inválidos (solo números y '/')
        dpFecha.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.matches("[0-9/]*")) {
                dpFecha.getEditor().setText(oldText);
            }
        });

        // --- CUANDO SE SELECCIONA UNA TAREA ---
        tablaTareas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                textoTitulo.setText(newSel.getTitulo());
                textoDescripcion.setText(newSel.getDescripcion());

                // Intentar convertir la fecha correctamente (String → LocalDate)
                try {
                    if (newSel.getFecha() != null && !newSel.getFecha().equals("Sin fecha establecida")) {
                        LocalDate fecha = LocalDate.parse(newSel.getFecha(), fmt);
                        dpFecha.setValue(fecha);
                    } else {
                        dpFecha.setValue(null);
                    }
                } catch (Exception e) {
                    dpFecha.setValue(null);
                }

                cbEstado.setValue(newSel.getEstado());
            }
        });



        //Habilitamos el modo de seleccion multiple de tareas en la tabla
        tablaTareas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //Mensaje para avisar que no hay tareas creadas y se debe crear una nueva.
        tablaTareas.setPlaceholder(new Label("No hay tareas disponibles. Usa el botón Agregar para crear una nueva."));


        // Estados del selector
        cbEstado.setItems(FXCollections.observableArrayList("Pendiente", "En curso", "Completada"));
        cbEstado.setValue("Pendiente");

        // Inicializar base y cargar tareas
        Database.ensureInitialized();
        tablaTareas.setItems(listaTareas);
        cargarTareas();

        // Colorear filas según estado
        tablaTareas.setRowFactory(tv -> new TableRow<Tarea>() {
            @Override
            protected void updateItem(Tarea tarea, boolean empty) {
                super.updateItem(tarea, empty);

                if (empty || tarea == null) {
                    setStyle("");
                    return;
                }

                String estado = tarea.getEstado();

                if (estado == null || estado.trim().isEmpty()) {
                    // Estado sin definir → pintar fila de naranja suave
                    setStyle("-fx-background-color: #ffd4a3;");
                    return;
                }

                switch (estado.toLowerCase()) {
                    case "completada":
                        setStyle("-fx-background-color: #b6f7b0;");  // pintar fila en verde suave si la tarea está completada
                        break;

                    case "pendiente":
                        setStyle("-fx-background-color: #fff4a3;");  // pintar fila en amarillo suave si la tarea está pendiente
                        break;

                    case "en curso":
                        setStyle("-fx-background-color: #cfe3ff;");  // pintar fila en azul claro suave si la tarea está en curso
                        break;

                    default:
                        setStyle("");
                }
            }
        });

        //Crear la leyenda al final del initialize
        crearLeyendaColores();

    }

    private void crearLeyendaColores() {
        if (contenedorLeyenda == null) return;

        contenedorLeyenda.setSpacing(10);

        contenedorLeyenda.getChildren().setAll(
                crearItemLeyenda("Completada", "#b6f7b0"),
                crearItemLeyenda("En curso",   "#cfe3ff"),
                crearItemLeyenda("Pendiente",  "#fff4a3"),
                crearItemLeyenda("Sin estado", "#ffd4a3")
        );
    }

    private HBox crearItemLeyenda(String texto, String colorHex) {
        HBox box = new HBox(5);

        Region color = new Region();
        color.setPrefSize(16, 16);
        color.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-border-color: #888;" +
                        "-fx-border-radius: 3;" +
                        "-fx-background-radius: 3;"
        );

        Label label = new Label(texto);

        box.getChildren().addAll(color, label);
        return box;

    }

    // --- AGREGAR TAREA ---
    @FXML
    private void agregarTarea() {
        String titulo = textoTitulo.getText();
        String descripcion = textoDescripcion.getText();
        String estado = cbEstado.getValue();
        LocalDate fecha = dpFecha.getValue();

        if (titulo == null || titulo.isBlank()) {
            mostrarAlerta("Datos incompletos", "Una tarea debe tener un título.");
            return;
        }

        String fechaTexto = (fecha == null) ? "Sin fecha establecida" : fecha.format(fmt);

        if ("Completada".equalsIgnoreCase(estado)) {
            Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
            confirmar.setTitle("Confirmar creación");
            confirmar.setHeaderText("Confirmación TaskEasy");
            confirmar.setContentText("¿Estás seguro de que quieres crear una tarea nueva con estado 'Completada'?");
            if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }

        // Insertar los datos proporcionados por pantalla en la base de datos SQLite
        Database.ejecutar(
                "INSERT INTO tareas (titulo, descripcion, fecha, estado, usuario_id) VALUES (?, ?, ?, ?, ?)",
                titulo, descripcion, fecha, estado, Session.getUsuarioActual()
                );


        cargarTareas();
        limpiarCampos();
    }

    // --- ELIMINAR TAREA ---
    @FXML
    private void eliminarTarea() {
        ObservableList<Tarea> seleccionadas = tablaTareas.getSelectionModel().getSelectedItems();

        if (seleccionadas.isEmpty()) {
            mostrarAlerta("Aviso", "Selecciona una o varias tareas para eliminar.");
            return;
        }

        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Confirmar eliminación");
        confirmar.setHeaderText("Eliminar tareas seleccionadas");
        confirmar.setContentText("¿Seguro que quieres eliminar las tareas seleccionadas?");
        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        for (Tarea tarea : seleccionadas) {
            Database.ejecutar("DELETE FROM tareas WHERE id = ?", tarea.getId());
        }

        cargarTareas();
    }


    // --- CARGAR TAREAS ---
    @FXML
    private void cargarTareas() {
        listaTareas.clear();

        int usuarioId = Session.getUsuarioActual();

        // Si por algún motivo no hay usuario logueado, no cargamos nada
        if (usuarioId <= 0) {
            System.out.println("⚠️ No hay usuario activo. No se cargan tareas.");
            return;
        }

        try (ResultSet rs = Database.consultar(
                "SELECT * FROM tareas WHERE usuario_id = ? ORDER BY fecha ASC", usuarioId)) {

            while (rs.next()) {
                listaTareas.add(new Tarea(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getString("fecha"),
                        rs.getString("estado")
                ));
            }

            // Actualizar la vista de la tabla
            tablaTareas.setItems(listaTareas);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // --- MODIFICAR TAREA ---
    @FXML
    private void modificarTarea() {
        Tarea seleccionada = tablaTareas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Aviso", "Selecciona una tarea para modificar.");
            return;
        }

        String nuevoTitulo = textoTitulo.getText().trim();
        String nuevaDescripcion = textoDescripcion.getText().trim();
        LocalDate nuevaFecha = dpFecha.getValue();
        String nuevoEstado = cbEstado.getValue();

        if (nuevoTitulo.isEmpty()) {
            mostrarAlerta("Error", "El título no puede estar vacío.");
            return;
        }

        // Mantener fecha anterior si no se cambia
        String fechaTexto = (nuevaFecha == null)
                ? seleccionada.getFecha()
                : nuevaFecha.format(fmt);

        // Mantener estado anterior si no se cambia
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            nuevoEstado = seleccionada.getEstado();
        }

        Database.ejecutar(
                "UPDATE tareas SET titulo = ?, descripcion = ?, fecha = ?, estado = ? WHERE id = ?",
                nuevoTitulo, nuevaDescripcion, fechaTexto, nuevoEstado, seleccionada.getId()
        );

        mostrarAlerta("Éxito", "La tarea ha sido modificada correctamente.");
        cargarTareas();
        limpiarCampos();
    }

    @FXML
    private void cerrarSesion() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar sesión");
        confirm.setHeaderText("¿Deseas cerrar tu sesión actual?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return; // el usuario canceló
        }

        try {
            // Limpia el usuario activo en la sesión
            Session.setUsuarioActual(0);

            // Cierra la ventana actual
            Stage currentStage = (Stage) tablaTareas.getScene().getWindow();
            currentStage.close();

            // Carga la ventana de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("TaskEasy — Iniciar sesión");
            loginStage.setScene(new Scene(loader.load()));
            loginStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("No se pudo cerrar sesión");
            error.setContentText("Ha ocurrido un error al intentar volver al login.");

            error.showAndWait();
        }
    }

    @FXML
    private void nuevaTarea() {
        limpiarCampos();
        mostrarAlerta("Nueva tarea", "Puedes introducir los datos de una nueva tarea.");
    }

    @FXML
    private void guardarCambios() {
        mostrarAlerta("Guardar cambios", "Tareas guardadas.\n\n Las tareas se guardan automáticamente en la base de datos:\n Al crear, modificar o eliminar una tarea.");
    }

    @FXML
    private void salirAplicacion() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Salir de TaskEasy");
        confirm.setHeaderText("¿Seguro que deseas cerrar la aplicación?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Platform.exit();
        }
    }

    @FXML
    private void cambiarTema() {

        Scene scene = tablaTareas.getScene();
        if (scene == null) return;

        // limpiar estilos anteriores
        scene.getStylesheets().clear();

        // saber si ya estamos en modo oscuro
        boolean modoOscuro = scene.getProperties().getOrDefault("dark-mode", false).equals(true);

        if (modoOscuro) {
            // aplicar tema claro
            scene.getStylesheets().add(
                    getClass().getResource("/css/temaClaro.css").toExternalForm()
            );
            scene.getProperties().put("dark-mode", false);

        } else {
            // aplicar tema oscuro
            scene.getStylesheets().add(
                    getClass().getResource("/css/temaOscuro.css").toExternalForm()
            );
            scene.getProperties().put("dark-mode", true);
        }
    }


    @FXML
    private void mostrarAcercaDe() {
        Alert acercaDe = new Alert(Alert.AlertType.INFORMATION);
        acercaDe.setTitle("Acerca de TaskEasy");
        acercaDe.setHeaderText("Gestor de tareas - TaskEasy");
        acercaDe.setContentText("Versión 1.0\nDesarrollado por Aitor Benito Heras\nProyecto Final CFGS DAM - Ilerna Online");
        acercaDe.showAndWait();
    }


    // --- LIMPIAR CAMPOS ---
    private void limpiarCampos() {
        textoTitulo.clear();
        textoDescripcion.clear();
        dpFecha.setValue(null);
        cbEstado.setValue(null);
    }

    // --- ALERTAS ---
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
