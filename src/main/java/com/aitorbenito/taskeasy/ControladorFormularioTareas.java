/*Creado por Aitor Benito Heras "ExInDer"*/
package com.aitorbenito.taskeasy;

/*
Imports javafx
*/
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;


/*
Imports java.time
*/


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/*
Imports SQL
*/
import java.sql.SQLException;
import com.aitorbenito.taskeasy.Categoria;


public class ControladorFormularioTareas {

    /* Boton guardar del formulario de las tareas*/
    @FXML Button btnGuardar;
    /* Area introduccion Titulo*/
    @FXML private TextField txtTitulo;
    /* Area introduccion Descripcion*/
    @FXML private TextArea txtDescripcion;
    /* Selector Fecha*/
    @FXML private DatePicker dpFecha;
    /* Selector del Estado de la tarea*/
    @FXML private ChoiceBox<String> cbEstado;
    /* Título de la ventana que nos muestra*/
    @FXML private Label tituloVentana;
    /* Botón eliminar*/
    @FXML private Button btnEliminar;
    /* Selector de categorías*/
    @FXML
    private ComboBox<Categoria> comboCategoria;



    private Tarea tareaActual = null; // Almacena la tarea si estamos en modo edición (null en modo creación).
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");



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

        // ---------------------------------------------
        // Cargar categorías desde la base de datos
        // ---------------------------------------------
        comboCategoria.getItems().setAll(BaseDeDatos.obtenerCategorias());
        comboCategoria.getSelectionModel().selectFirst();
        // Evita que se pueda escribir una fecha directamente, asi forzamos el uso del selector
        dpFecha.setEditable(false);

        // Opciones disponibles para el estado de la tarea que estamos creando
        cbEstado.getItems().setAll("Sin estado definido", "Pendiente", "En curso", "Completada");

        if (tarea == null) {
            // CREAR NUEVA TAREA
            tituloVentana.setText("Nueva tarea");
            //Ocultamos el botón de eliminar al crear una nueva tarea porque no tiene sentido tenerlo.
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
                    dpFecha.setValue(LocalDate.parse(tarea.getFecha(), formatoFecha));
                    /*Captura de excepcion con la fecha*/
                } catch (Exception e) {
                    System.err.println("Advertencia: No se pudo parsear la fecha de la tarea ID: " + tarea.getId());
                    // El DatePicker queda vacío.
                }
            }

            /*
                Validamos el estado de la tarea
            */
            String estadoBD = tarea.getEstado();
            if (estadoBD != null && cbEstado.getItems().contains(estadoBD)) {
                cbEstado.setValue(estadoBD);
            } else {
                /*
                Se define como estado por defecto "Sin estado definido" para evitar problemas
                en la selección del estado y por si el usuario no quiere que tenga estado la tarea.*/
                cbEstado.setValue("Sin estado definido");
            }

            /*
                  Seleccionar categoría correcta
            */
            for (Categoria c : comboCategoria.getItems()) {
                if (c.getId() == tarea.getIdCategoria()) {
                    comboCategoria.getSelectionModel().select(c);
                    break;
                }
            }

        }
    }



    /* ----------------------------------------------------

                       Metodo guardar

       ----------------------------------------------------
       Maneja la acción de guardar,
       Decide si hacer un INSERT o un UPDATE en la base de datos.
    */
    @FXML
    private void guardar() {
        String titulo = txtTitulo.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        LocalDate fecha = dpFecha.getValue();
        String estado = cbEstado.getValue();
        // Obtener la categoría seleccionada
        Categoria categoria = comboCategoria.getValue();
        Integer idCategoria = (categoria != null) ? categoria.getId() : null;

        // **Validación 1: Título obligatorio.
        if (titulo.isEmpty()) {
            alert("Error", "El título es obligatorio.");
            return;
        }

        // **Validación 2: Estado obligatorio (aunque el choicebox por defecto ya ayuda).
        if (estado == null || estado.isEmpty()) {
            alert("Error", "Debes seleccionar un estado para la tarea.");
            return;
        }

        // Mapeo: LocalDate (UI) → String (BD)
        String fechaTexto = (fecha == null) ? "Sin fecha establecida" : fecha.format(formatoFecha);

        try {
            if (tareaActual == null) {
                // Insercion de nueva tarea
                BaseDeDatos.ejecutar(
                        "INSERT INTO tareas (titulo, descripcion, fecha, estado, usuario_id) VALUES (?, ?, ?, ?, ?)",
                        titulo, descripcion, fechaTexto, estado, SesionUsuario.getUsuarioActual()
                );

            } else {
                // UPDATE EXISTENTE
                // Se utiliza el ID de la tarea para saber qué registro actualizar.
                BaseDeDatos.ejecutar(
                        "UPDATE tareas SET titulo=?, descripcion=?, fecha=?, estado=?, id_categoria=? WHERE id=?",
                        titulo, descripcion, fechaTexto, estado, idCategoria, tareaActual.getId()
                );
            }

            // **Callback**: Ejecuta el metodo de retorno (ej. `cargarTareas` del MainController)
            if (onSaveCallback != null) onSaveCallback.run();
            cerrar();

        } catch (SQLException excepcion) {
            alert("Error", "No se pudo guardar la tarea.");
            excepcion.printStackTrace();
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
        Alert aviso = new Alert(Alert.AlertType.CONFIRMATION);
        aviso.setTitle("Confirmar eliminación");
        aviso.setHeaderText("Eliminar tarea: " + tareaActual.getTitulo());
        aviso.setContentText("¿Estás seguro de que deseas eliminar esta tarea de forma permanente?");

        // Si el usuario no pulsa OK, aborta la eliminación.
        if (aviso.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        } try {
            // **DELETE**
            BaseDeDatos.ejecutar("DELETE FROM tareas WHERE id=?", tareaActual.getId());

            // Ejecuta el callback y cierra.
            if (onSaveCallback != null) onSaveCallback.run();
            cerrar();

        } catch (SQLException excepcion) {
            alert("Error", "No se pudo eliminar la tarea.");
            excepcion.printStackTrace();
        }
    }

    /* ----------------------------------------------------
                        Metodo cerrar
       ----------------------------------------------------
    Lo usamos para cerrar los escenarios y las escenas generadas*/
    @FXML
    private void cerrar() {
        Stage escenario = (Stage) txtTitulo.getScene().getWindow();
        escenario.close();
    }


    /* ----------------------------------------------------
                     Metodo alert
       ----------------------------------------------------
        Lo usamos para lanzar avisos informativos, cargando el titulo del mensaje y el texto de este*/
    private void alert(String titulo, String msg) {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        aviso.setContentText(msg);
        aviso.showAndWait();
    }
}