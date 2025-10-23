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

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private TableView<Tarea> tablaTareas;
    @FXML private TableColumn<Tarea, String> colTitulo;
    @FXML private TableColumn<Tarea, String> colDescripcion;
    @FXML private TableColumn<Tarea, String> colFecha;
    @FXML private TableColumn<Tarea, String> colEstado;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescripcion;
    @FXML private DatePicker dpFecha;
    @FXML private ChoiceBox<String> cbEstado;

    private final ObservableList<Tarea> listaTareas = FXCollections.observableArrayList();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        colTitulo.setCellValueFactory(data -> data.getValue().tituloProperty());
        colDescripcion.setCellValueFactory(data -> data.getValue().descripcionProperty());
        colFecha.setCellValueFactory(data -> data.getValue().fechaProperty());
        colEstado.setCellValueFactory(data -> data.getValue().estadoProperty());

        // Mostrar "Sin fecha establecida" solo en tareas reales (no en filas vacías),
        // Pasrá siempre que la tarea haya sido creada sin fecha de fin establecida
        colFecha.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String fecha, boolean empty) {
                super.updateItem(fecha, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else if (fecha == null || fecha.trim().isEmpty()) {
                    setText("Sin fecha establecida");
                    setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
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
                txtTitulo.setText(newSel.getTitulo());
                txtDescripcion.setText(newSel.getDescripcion());

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
    }

    // --- AGREGAR TAREA ---
    @FXML
    private void agregarTarea() {
        String titulo = txtTitulo.getText();
        String descripcion = txtDescripcion.getText();
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

        String nuevoTitulo = txtTitulo.getText().trim();
        String nuevaDescripcion = txtDescripcion.getText().trim();
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
        confirm.setContentText("Si cierras sesion, no podras acceder a tus tareas hasta volver a iniciar sesión.");

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
        mostrarAlerta("Guardar cambios", "Las tareas se guardan automáticamente en la base de datos.");
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
        // Aquí podríamos implementar un cambio de estilo entre claro/oscuro
        mostrarAlerta("Tema", "Esta función estará disponible próximamente.");
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
        txtTitulo.clear();
        txtDescripcion.clear();
        dpFecha.setValue(null);
        cbEstado.setValue("Pendiente");
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
