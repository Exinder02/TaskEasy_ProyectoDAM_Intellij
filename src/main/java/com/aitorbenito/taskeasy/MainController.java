package com.aitorbenito.taskeasy;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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


        // Forzar formato de fecha dia/Mes/año
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpFecha.setPromptText("Dia/Mes/Año");


        // Formatear automáticamente la fecha mientras se escribe (dd/MM/yyyy)
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

        // Bloquear letras o caracteres inválidos en el campo de fecha (solo números y el simbolo '/')
        dpFecha.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            // Solo permitir dígitos y barras (ejemplo válido: 25/10/2025)
            if (!newText.matches("[0-9/]*")) {
                dpFecha.getEditor().setText(oldText);
            }
        });


        //Modificar tareas, para usar esta funcion que hemos creado, añadimos este bloque en el metodo initialize
        tablaTareas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtTitulo.setText(newSel.getTitulo());
                txtDescripcion.setText(newSel.getDescripcion());

                // Intentar convertir la fecha correctamente
                try {
                    if (newSel.getFecha() != null && !newSel.getFecha().equals("Sin fecha establecida")) {
                        LocalDate fecha = LocalDate.parse(newSel.getFecha(), fmt);
                        dpFecha.setValue(fecha);
                    } else {
                        dpFecha.setValue(null);
                    }
                } catch (Exception e) {
                    dpFecha.setValue(null); // si algo falla, dejar vacío
                }

                cbEstado.setValue(newSel.getEstado());
            }
        });



        // Definimos los estados que hay dentro del Selector de estado.
        cbEstado.setItems(FXCollections.observableArrayList("Pendiente", "En curso", "Completada"));
        // Establece el valor inicial del selector de estado como pendiente, ya que lo habitual es que una tarea se cree antes de ejecutarla o finalizarla.
        cbEstado.setValue("Pendiente");



        // Inicializar y cargar tareas creadas
        tablaTareas.setItems(listaTareas);
        Database.ensureInitialized();
        cargarTareas();
    }


    /*Funcion para agregar tareas, esta funcion tiene varias capturas de errores tipicos de introduccion de datos,
     he intentado controlarlos en cada uno de los campos que conforman la funcion.
     */
    @FXML
    private void agregarTarea() {
        //Inicio las variables de la funcion
        String titulo = txtTitulo.getText();
        String descripcion = txtDescripcion.getText();
        String estado = cbEstado.getValue();
        String fecha = "";



        // Validamos los campos obligatorios
        if (titulo == null || titulo.isBlank() || fecha == null) {
            mostrarAlerta("Datos incompletos", "Una tarea debe tener Título y fecha para poder crearse.");
            return;
        }



        // Validamos el formato de la fecha

        // Abrimos un condicional para decir que si en el editor de fecha los campos son diferentes a vacio,
        // pase al siguiente punto de la funcion, un try catch que nos pedira la fecha en un formato concreto
        if (dpFecha.getEditor().getText() != null && !dpFecha.getEditor().getText().isBlank()) {

            // Intentamos obtener la fecha como LocalDate, o la fecha actual del sitio donde se está ejecutando la aplicacion
            try {
                fecha = dpFecha.getValue() != null ? dpFecha.getValue().toString() : "";
                } catch (Exception e) {
                    //En caso de no cumplir con los criterios de fecha establecidos, mostrar aviso por pantalla
                    mostrarAlerta("Fecha inválida", "Introduce una fecha válida (por ejemplo: 20/08/2025).");
                return;
                }
        } else { //  Si no hay fecha, preguntar si continuamos la creacion de la tarea sin ella.

            //Esto lo he hecho porque es posible que queramos tener una nota y que no dependa de una fecha de fin, como si podría ser el caso de una tarea
            Alert avisoSinFecha = new Alert(Alert.AlertType.CONFIRMATION);
            avisoSinFecha.setTitle("Tarea sin fecha");
            avisoSinFecha.setHeaderText(null);
            avisoSinFecha.setContentText("La tarea no tiene fecha. ¿Quieres crearla igualmente?");
            if (avisoSinFecha.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }



        // Confirmar si el estado de la tarea es “Completada”, ya que crear una tarea como completada es extraño y lo
        // mas normal es crear tareas pendientes de hacer o que están en proceso de finalizacion.

        //Con motivo de lo expuesto en el parrafo anterior,
        if ("Completada".equalsIgnoreCase(estado)) {
            //Tipo de aviso que vamos a tener cuando validemos
            Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
            //Definimos el mensaje
            confirmar.setTitle("Confirmar creación");
            //Ponemos un mensaje en el header
            confirmar.setHeaderText("Confirmacion TaskEasy");
            //Definimos la consulta que le vamos a hacer al usuario sobre si quiere crear la tarea como completada,
            // como hemos visto en el bloque anterior de codigo, esto lo he hecho porque si un usuario solo quiere tener una nota, no necesitaria fecha de fin, pero si un estado.
            confirmar.setContentText("¿Estás seguro de que quieres crear una tarea nueva con estado 'Completada'?");
            //Con este if validamos si creamos la nota o cancelamos la creacion.
            if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }



        // Insertar los datos proporcionados por pantalla en la base de datos SQLite
        Database.ejecutar(
                "INSERT INTO tareas (titulo, descripcion, fecha, estado) VALUES (?, ?, ?, ?)",
                titulo, descripcion, fecha, estado
        );



        cargarTareas();
        limpiarCampos();
    }




    //Funcion para eliminar tareas de la base de datos
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


    //Funcion para cargar las tareas que tenemos en la base de datos
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




    //Funcion para modificar las tareas ya creadas en la base de datos
    @FXML
    private void modificarTarea() {
        Tarea seleccionada = tablaTareas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Aviso", "Selecciona una tarea para modificar.");
            return;
        }

        String nuevoTitulo = txtTitulo.getText().trim();
        String nuevaDescripcion = txtDescripcion.getText().trim();

        // Si el usuario no ha tocado la fecha, conservar la que ya tenía
        LocalDate nuevaFecha = dpFecha.getValue();
        String fechaTexto;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (nuevaFecha == null) {
            // No se seleccionó ninguna nueva fecha → conservar la anterior
            fechaTexto = seleccionada.getFecha();
            if (fechaTexto == null || fechaTexto.isBlank() || fechaTexto.equalsIgnoreCase("Sin fecha establecida")) {
                fechaTexto = "Sin fecha establecida";
            }
        } else {
            // Convertir la fecha nueva al formato que queremos guardar
            fechaTexto = nuevaFecha.format(fmt);
        }

        // Si no se elige nuevo estado, mantener el anterior
        String nuevoEstado = cbEstado.getValue();
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            nuevoEstado = seleccionada.getEstado();
        }

        if (nuevoTitulo.isEmpty()) {
            mostrarAlerta("Error", "El título no puede estar vacío.");
            return;
        }

        // Actualizar en la base de datos
        String sql = "UPDATE tareas SET titulo = ?, descripcion = ?, fecha = ?, estado = ? WHERE id = ?";
        Database.ejecutar(sql, nuevoTitulo, nuevaDescripcion, fechaTexto, nuevoEstado, seleccionada.getId());

        mostrarAlerta("Éxito", "La tarea ha sido modificada correctamente.");

        // Refrescar tabla
        cargarTareas();
        limpiarCampos();
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