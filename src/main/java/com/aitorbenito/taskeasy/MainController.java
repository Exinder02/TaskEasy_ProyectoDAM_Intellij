package com.aitorbenito.taskeasy;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.*;
import java.time.LocalDate;
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

    @FXML
    public void initialize() {
        colTitulo.setCellValueFactory(data -> data.getValue().tituloProperty());
        colDescripcion.setCellValueFactory(data -> data.getValue().descripcionProperty());
        colFecha.setCellValueFactory(data -> data.getValue().fechaProperty());
        colEstado.setCellValueFactory(data -> data.getValue().estadoProperty());


        // Mostrar "Sin fecha establecida" solo en tareas reales (no en filas vacías)
        colFecha.setCellFactory(column -> new TableCell<Tarea, String>() {
            @Override
            protected void updateItem(String fecha, boolean empty) {
                super.updateItem(fecha, empty);

                // Si la fila está vacía, no mostrar nada
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                // Si no hay fecha en la tarea
                if (fecha == null || fecha.trim().isEmpty()) {
                    setText("Sin fecha establecida");
                    setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                } else {
                    setText(fecha);
                    setStyle("");
                }
            }
        });


        // Forzar formato de fecha dia-Mes-año
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpFecha.setPromptText("Dia/Mes/Año");


// ✅ Formatear automáticamente la fecha mientras se escribe (dd/MM/yyyy)
        dpFecha.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null) return;

            // Solo permitir números y '/'
            if (!newText.matches("[0-9/]*")) {
                dpFecha.getEditor().setText(oldText);
                return;
            }

            // Eliminar cualquier separador previo para procesar
            String clean = newText.replaceAll("[^0-9]", "");

            // Máximo 8 dígitos (ddMMyyyy)
            if (clean.length() > 8) clean = clean.substring(0, 8);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < clean.length(); i++) {
                sb.append(clean.charAt(i));
                // Añadir "/" en posiciones adecuadas: 2 y 4 (para dd/MM/yyyy)
                if ((i == 1 || i == 3) && i != clean.length() - 1) sb.append("/");
            }

            String formatted = sb.toString();

            // Si el año tiene solo 2 dígitos (ddMMyy → dd/MM/20yy)
            if (clean.length() == 6) {
                String dia = clean.substring(0, 2);
                String mes = clean.substring(2, 4);
                String anio = "20" + clean.substring(4); // completar con 20xx
                formatted = dia + "/" + mes + "/" + anio;
            }

            // Actualizar texto solo si cambia
            if (!formatted.equals(newText)) {
                dpFecha.getEditor().setText(formatted);
                dpFecha.getEditor().positionCaret(formatted.length());
            }
        });

        // ✅ Bloquear letras o caracteres inválidos en el campo de fecha (solo números y el simbolo '/')
        dpFecha.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            // Solo permitir dígitos y barras (ejemplo válido: 25/10/2025)
            if (!newText.matches("[0-9/]*")) {
                dpFecha.getEditor().setText(oldText);
            }
        });




        // Selector de estado.
        cbEstado.setItems(FXCollections.observableArrayList("Pendiente", "En curso", "Completada"));
        // Establece el valor inicial del selector de estado como pendiente
        cbEstado.setValue("Pendiente");



        // Inicializar y cargar tareas
        tablaTareas.setItems(listaTareas);
        Database.ensureInitialized();
        cargarTareas();
    }



    @FXML
    private void agregarTarea() {
        String titulo = txtTitulo.getText();
        String descripcion = txtDescripcion.getText();
        String estado = cbEstado.getValue();
        String fecha = "";



        // 1️⃣ Validar campos obligatorios
        if (titulo == null || titulo.isBlank() || estado == null) {
            mostrarAlerta("Datos incompletos", "Título y estado son obligatorios.");
            return;
        }



        // 2️⃣ Validar formato de fecha
        if (dpFecha.getEditor().getText() != null && !dpFecha.getEditor().getText().isBlank()) {

            // Intentamos obtener la fecha como LocalDate
            try {
                fecha = dpFecha.getValue() != null ? dpFecha.getValue().toString() : "";
                } catch (Exception e) {
                    mostrarAlerta("Fecha inválida", "Introduce una fecha válida (por ejemplo: 2025-08-25).");
                return;
                }
        } else {
            // 3️⃣ Si no hay fecha, preguntar si continuar sin ella
            Alert avisoSinFecha = new Alert(Alert.AlertType.CONFIRMATION);
            avisoSinFecha.setTitle("Tarea sin fecha");
            avisoSinFecha.setHeaderText(null);
            avisoSinFecha.setContentText("La tarea no tiene fecha. ¿Quieres crearla igualmente?");
            if (avisoSinFecha.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }



        // 4️⃣ Confirmar si el estado es “Completada”
        if ("Completada".equalsIgnoreCase(estado)) {
            Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
            confirmar.setTitle("Confirmar creación");
            confirmar.setHeaderText(null);
            confirmar.setContentText("¿Estás seguro de que quieres crear una tarea nueva con estado 'Completada'?");
            if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }



        // 5️⃣ Insertar en la base de datos
        Database.ejecutar(
                "INSERT INTO tareas (titulo, descripcion, fecha, estado) VALUES (?, ?, ?, ?)",
                titulo, descripcion, fecha, estado
        );



        cargarTareas();
        limpiarCampos();
    }




    @FXML
    private void eliminarTarea() {
        Tarea seleccionada = tablaTareas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Aviso", "Selecciona una tarea para eliminar.");
            return;
        }

        Database.ejecutar("DELETE FROM tareas WHERE id = ?", seleccionada.getId());
        cargarTareas();
    }



    @FXML
    private void cargarTareas() {
        listaTareas.clear();
        try (ResultSet rs = Database.consultar("SELECT * FROM tareas")) {
            while (rs.next()) {
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
        }
    }



    private void limpiarCampos() {
        txtTitulo.clear();
        txtDescripcion.clear();
        dpFecha.setValue(null);
        cbEstado.setValue(null);
    }



    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}



/*Test de subida de cambios en git hub*/