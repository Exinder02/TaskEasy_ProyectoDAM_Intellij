/*Creado por Aitor Benito Heras "ExInDer"*/
package com.aitorbenito.taskeasy;

/*Imports javafx*/
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
/*Imports java.time*/
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/*Imnports SQL*/
import java.sql.SQLException;


public class TaskFormController {

    /* Declaracion de variasbles*/

    /*Variable Titulo*/
    @FXML private TextField txtTitulo;
    /*Variable Descripcion*/
    @FXML private TextArea txtDescripcion;
    /*Variable Fecha*/
    @FXML private DatePicker dpFecha;
    /*Variable estado*/
    @FXML private ChoiceBox<String> cbEstado;
    /*Variable titulo de la ventana que nos muestra*/
    @FXML private Label tituloVentana;
    /*Variable Boton eliminar*/
    @FXML private Button btnEliminar;

    private Tarea tareaActual = null; // Almacena la tarea si estamos en modo edición (null en modo creación).
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ----------------------------------------------------
       Callback para la actualización de la tabla principal
       ----------------------------------------------------
       MainController inyecta aquí su metodo cargarTareas(); para que el formulario lo ejecute al finalizar*/
    private Runnable onSaveCallback;

    /* ----------------------------------------------------
                    Metodo configurar
       ----------------------------------------------------
       Es el metodo de inicialización llamado por el MainController y que define el modo de trabajo: Crear (tarea == null) o Editar*/
    public void configurar(Tarea tarea, Runnable callback) {
        this.tareaActual = tarea;
        this.onSaveCallback = callback;

        // Evita que el usuario escriba una fecha directamente, forzando el uso del selector.
        dpFecha.setEditable(false);

        // Opciones disponibles para el estado de la tarea que estamos creando,
        cbEstado.getItems().setAll("Sin estado definido", "Pendiente", "En curso", "Completada");

        if (tarea == null) {
            // CREAR NUEVA TAREA
            tituloVentana.setText("Nueva tarea");
            //Ocultamos el boton de eliminar al crear una nueva tarea porque no tiene sentido tenerlo.
            btnEliminar.setVisible(false);
            /*El estado por defecto de una tarea es sin estado definido, puesto que puede ser solo una nota...*/
            cbEstado.setValue("Sin estado definido");

        } else {
            /*EDITAR TAREA*/
            tituloVentana.setText("Editar tarea");
            /*CoGemos los datos introduicdos en el titulo de la tarea y en la descripcion*/
            txtTitulo.setText(tarea.getTitulo());
            txtDescripcion.setText(tarea.getDescripcion());

            /*Manejo y parseo de la fecha (si existe)*/
            if (tarea.getFecha() != null && !tarea.getFecha().equals("Sin fecha establecida")) {
                try {
                    // Mapeo inverso: String (BD) → LocalDate (UI)
                    dpFecha.setValue(LocalDate.parse(tarea.getFecha(), fmt));
                    /*Captura de excepcion con la fecha*/
                } catch (Exception e) {
                    System.err.println("Advertencia: No se pudo parsear la fecha de la tarea ID: " + tarea.getId());
                    // El DatePicker queda vacío.
                }
            }

            /*Validación y asignación del estado*/
            String estadoBD = tarea.getEstado();
            if (estadoBD != null && cbEstado.getItems().contains(estadoBD)) {
                cbEstado.setValue(estadoBD);
            } else {
                /*Se define como estado por defecto "Sin estado definido" para evitar problemas en la seleccion del estado, y por si el usuario no quiere que tenga estado la tarea.*/
                cbEstado.setValue("Sin estado definido");
            }
        }
    }

    /* ----------------------------------------------------
       Metodo guardar
       ----------------------------------------------------
       Maneja la acción de guardar, decidiendo si hacer un INSERT o un UPDATE.
    */
    @FXML
    private void guardar() {
        String titulo = txtTitulo.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        LocalDate fecha = dpFecha.getValue();
        String estado = cbEstado.getValue();

        // **Validación 1**: Título obligatorio.
        if (titulo.isEmpty()) {
            alert("Error", "El título es obligatorio.");
            return;
        }

        // **Validación 2**: Estado obligatorio (aunque el choicebox por defecto ya ayuda).
        if (estado == null || estado.isEmpty()) {
            alert("Error", "Debes seleccionar un estado para la tarea.");
            return;
        }

        // Mapeo: LocalDate (UI) → String (BD)
        String fechaTexto = (fecha == null) ? "Sin fecha establecida" : fecha.format(fmt);

        try {
            if (tareaActual == null) {
                // Insercion de nueva tarea
                Database.ejecutar(
                        "INSERT INTO tareas (titulo, descripcion, fecha, estado, usuario_id) VALUES (?, ?, ?, ?, ?)",
                        titulo, descripcion, fechaTexto, estado, Session.getUsuarioActual() // Usa el ID de la sesión
                );

            } else {
                // UPDATE EXISTENTE
                // Se utiliza el ID de la tarea para saber qué registro actualizar.
                Database.ejecutar(
                        "UPDATE tareas SET titulo=?, descripcion=?, fecha=?, estado=? WHERE id=?",
                        titulo, descripcion, fechaTexto, estado, tareaActual.getId()
                );
            }

            // **Callback**: Ejecuta el metodo de retorno (ej. `cargarTareas` del MainController)
            if (onSaveCallback != null) onSaveCallback.run();
            cerrar();

        } catch (SQLException e) {
            alert("Error", "No se pudo guardar la tarea.");
            e.printStackTrace();
        }
    }

    /* ----------------------------------------------------
                    Metodo eliminarTarea
       ----------------------------------------------------
       Maneja la acción de eliminar, solo disponible en modo edición.
    */
    @FXML
    private void eliminarTarea() {
        if (tareaActual == null) return;

        // Confirma la acción destructiva con el usuario.
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Confirmar eliminación");
        confirmar.setHeaderText("Eliminar tarea: " + tareaActual.getTitulo());
        confirmar.setContentText("¿Estás seguro de que deseas eliminar esta tarea de forma permanente?");

        // Si el usuario no pulsa OK, aborta la eliminación.
        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        } try {
            // **DELETE**
            Database.ejecutar("DELETE FROM tareas WHERE id=?", tareaActual.getId());

            // Ejecuta el callback y cierra.
            if (onSaveCallback != null) onSaveCallback.run();
            cerrar();

        } catch (SQLException e) {
            alert("Error", "No se pudo eliminar la tarea.");
            e.printStackTrace();
        }
    }

    /* ----------------------------------------------------
                        Metodo cerrar
       ----------------------------------------------------
    Lo usamos para cerrar los escenarios y las escenas generadas*/
    @FXML
    private void cerrar() {
        Stage stage = (Stage) txtTitulo.getScene().getWindow();
        stage.close();
    }


    /* ----------------------------------------------------
                     Metodo alert
       ----------------------------------------------------
        Lo usamos para lanzar avisos informativos, cargando el titulo del mensaje y el texto de este*/
    private void alert(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}