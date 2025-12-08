/*Creado por Aitor Benito Heras "ExInDer"*/
package com.aitorbenito.taskeasy;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/* ----------------------------------

      Clase ControladorPrincipal

   ----------------------------------
   Controlador principal de la aplicación, asociado a 'main.fxml'.
   Gestiona la visualización, la manipulación de tareas y las opciones de sesión.
   */
public class ControladorPrincipal {

    /* Inyección de elementos del FXML: La tabla y sus columnas. */
    @FXML private TableView<Tarea> tablaTareas;
    @FXML private TableColumn<Tarea, String> colTitulo;
    @FXML private TableColumn<Tarea, String> colDescripcion;
    @FXML private TableColumn<Tarea, String> colFecha;
    @FXML private TableColumn<Tarea, String> colEstado;
    @FXML private TableColumn<Tarea, String> colCategoria;


    /* Contenedores para elementos de interfaz (Ej. para la leyenda de colores). */

    @FXML private HBox contLeyenda;

    /* Estructura de datos crucial: Lista que se enlaza al TableView (Data Binding).
       ObservableList permite que la tabla se actualice automáticamente al cambiar la lista. */
    private final ObservableList<Tarea> listaTareas = FXCollections.observableArrayList();

    /* Formateador de fecha reutilizable. */
    private final DateTimeFormatter dTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* --------------------------------

           Metodo initialize:

       --------------------------------
       Llamado automáticamente por JavaFX después de cargar el FXML.
       Se usa para:
       - Configurar el 'Data Binding' de las columnas.
       - Aplicar estilos y *callbacks* (doble clic, colores).
       - Cargar los datos iniciales.
    */
    @FXML
    public void initialize() {

        /*
        CONFIGURACIÓN DE LAS CELDAS
        Se define cómo la propiedad de cada objeto Tarea se mapea a la columna.
        */
        colTitulo.setCellValueFactory(data -> data.getValue().tituloProperty());
        colDescripcion.setCellValueFactory(data -> data.getValue().descripcionProperty());
        colFecha.setCellValueFactory(data -> data.getValue().fechaProperty());
        colEstado.setCellValueFactory(data -> data.getValue().estadoProperty());

        // ----------------------------------------------------------
        // NUEVO: Configuración de la columna Categoría
        // ----------------------------------------------------------
        colCategoria.setCellValueFactory(cellData -> {
            int idCat = cellData.getValue().getIdCategoria();

            Categoria categoria = BaseDeDatos.obtenerCategorias()
                    .stream()
                    .filter(c -> c.getId() == idCat)
                    .findFirst()
                    .orElse(null);

            return new SimpleStringProperty(
                    categoria != null ? categoria.getNombre() : "Sin categoría"
            );
        });

        /*
        Configuración de la columna de la fecha
        */
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

        /*
        Habilitar la selección de múltiples filas para la eliminación masiva.
        */
        tablaTareas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        /*
        Mensaje cuando la tabla está vacía.
        */
        tablaTareas.setPlaceholder(new Label("No hay tareas disponibles. Usa el botón + Nueva Tarea"));

        /*
         Se asegura de que la estructura de la DB exista.
        */
        BaseDeDatos.asegurarInicio();


        /* ------------------------------------------------------------

                           CARGAR TAREAS EN LA TABLA

           ------------------------------------------------------------
        */
        cargarTareas();

        tablaTareas.setItems(listaTareas);

        /* ------------------------------------------------------------

           ROWFACTORY: LÓGICA DE COLORES POR ESTADO + EVENTO DOBLE CLIC

           ------------------------------------------------------------
        */
        tablaTareas.setRowFactory(tv -> {
            TableRow<Tarea> fila = new TableRow<>() {
                @Override
                protected void updateItem(Tarea item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        /* Limpiar estilo si no hay datos en la fila*/
                        setStyle("");
                    } else {
                        /* Aplica un color de fondo diferente a la fila según el estado de la tarea*/
                        String estado = item.getEstado();

                        /* Protección contra estados nulos que podrían causar un error*/
                        if (estado == null) {
                            estado = "sin estado";
                        }

                        /*
                        Uso de "switch" para aplicar estilos CSS dependiendo del estado que tenga la tarea
                        */
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

            /*
            ---------------------------------------------

                      Evento de doble clic:

            ---------------------------------------------
            Si se hace doble clic en una fila, abre el formulario para editar.
            */
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirFormularioTarea(fila.getItem());
                }
            });

            return fila;
        });

        /*
          Generar la leyenda de colores debajo de la tabla.
        */
        crearLeyendaColorInferior();
    }

    /* ----------------------------------------------------

                   Metodo abrirFormularioTarea

       ----------------------------------------------------

       Metodo utilizado para abrir la ventana de edicion y creacion de tareas del TaskForm.fxml
       Implementa el patrón de Diseño Modal, bloqueando la ventana principal.
       Pasa el metodo "cargarTareas" para que el TaskFormController lo llame al guardar,
       actualizando la tabla automáticamente.
    */
    private void abrirFormularioTarea(Tarea tarea) {
        try {
            FXMLLoader cargadorFXML = new FXMLLoader(getClass().getResource("/view/formularioTareas.fxml"));
            Parent root = cargadorFXML.load();

            // Obtenemos el controlador del formulario.
            ControladorFormularioTareas controlador = cargadorFXML.getController();

            // Inyectamos la tarea a editar, pero si es null, es una nueva tarea
            controlador.configurar(tarea, this::cargarTareas);

            /*Abre un nuevo escenario*/
            Stage escenario = new Stage();
            /*
            APPLICATION_MODAL bloquea la interacción con la ventana principal,
            para que no podamos abrir otra tarea mientras creamos o editamos una existente
            */
            escenario.initModality(Modality.APPLICATION_MODAL);
            escenario.setResizable(false);
            escenario.setTitle(tarea == null ? "Nueva tarea" : "Editar tarea");
            escenario.setScene(new Scene(root));
            escenario.show();

        } catch (Exception excepcion) {
            excepcion.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de edición.");
        }
    }

    /* ----------------------------------------------------

           Métodos de acción de los Botones inferiores
          (Se usa para los de la barra superior también)

       ----------------------------------------------------
       ----------------------------------------------------

                        agregarTarea

       ---------------------------------------------------- */

    @FXML
    private void agregarTarea() {
        /*
        Pasa "null" para indicar que es una tarea nueva
        */
        abrirFormularioTarea(null);
    }


/* ----------------------------------------------------

                 modificarTarea

   ---------------------------------------------------- */


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


    /* ----------------------------------------------------

                        eliminarTarea

       ---------------------------------------------------- */

    @FXML
    private void eliminarTarea() throws SQLException {
        ObservableList<Tarea> seleccionadas = tablaTareas.getSelectionModel().getSelectedItems();

        if (seleccionadas.isEmpty()) {
            mostrarAlerta("Aviso", "Selecciona una o varias tareas para eliminar.");
            return;
        }

        /*
         Salta un aviso de confirmación antes de eliminar las tareas.
         */
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Confirmar eliminación");
        confirmar.setHeaderText("Eliminar tareas seleccionadas");

        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        /*
        Itera sobre las seleccionadas y llama al metodo DAO "BaseDeDatos.ejecutar" para eliminarlas una a una.
         */
        for (Tarea tarea : seleccionadas) {
            BaseDeDatos.ejecutar("DELETE FROM tareas WHERE id = ?", tarea.getId());
        }

        /*
        Una vez eliminadas, recarga la tabla para reflejar los cambios.
         */
        cargarTareas();
    }

    /* ----------------------------------------------------

                       Metodo cargarTareas

       ----------------------------------------------------
       Este metodo se comunica con la BaseDeDatos
       Carga las tareas del usuario logueado "SesionUsuario.getUsuarioActual()"
    */
    @FXML
    private void cargarTareas() {
        /*
         Limpiar la lista actual para evitar duplicados.
         */
        listaTareas.clear();
        int usuarioId = SesionUsuario.getUsuarioActual();
        /*
         Si no hay sesión iniciada, sale.
         */
        if (usuarioId <= 0) return;

        /*
         Se usa "try-with-resources" para asegurar el cierre del ResultSet y la conexión.
         */
        try (ResultSet resultSet = BaseDeDatos.consultar(
                /*
                 Consulta las tareas asociadas al ID del usuario logueado
                 */
                "SELECT * FROM tareas WHERE usuario_id = ? ORDER BY fecha ASC",
                usuarioId
        )) {
            while (resultSet.next()) {
                /*
                 Mapeo de la fila del ResultSet a un objeto Tarea.
                 */
                listaTareas.add(new Tarea(
                        resultSet.getInt("id"),
                        resultSet.getString("titulo"),
                        resultSet.getString("descripcion"),
                        resultSet.getString("fecha"),
                        resultSet.getString("estado"),
                        resultSet.getObject("id_categoria") != null ? resultSet.getInt("id_categoria") : null
                ));
            }
            /*
            Captura las excepciones adicionales que pudieran surgir,
            pasa un aviso indicando que no se han cargado las tareas
            */
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar las tareas desde la base de datos.");
        }
    }

    /* ----------------------------------------------------

                    Metodo cerrarSesion

       ---------------------------------------------------- */

    @FXML
    private void cerrarSesion() {
        try {
            /*
             Limpia la sesión actual.
             */
            SesionUsuario.setUsuarioActual(0);
            /*
             Cierra la ventana principal actual.
             */
            Stage current = (Stage) tablaTareas.getScene().getWindow();
            current.close();
            /*
            Abre una nueva ventana de login.
             */
            FXMLLoader cargadorFXML = new FXMLLoader(getClass().getResource("/view/logueo.fxml"));
            Stage login = new Stage();
            login.setScene(new Scene(cargadorFXML.load()));
            login.show();

        } catch (Exception excepcion) {
            excepcion.printStackTrace();
        }
    }



        /* ----------------------------------------------------

                    Metodo cerrarSesion

       ---------------------------------------------------- */

    @FXML
    private void cerrarApp() {
        /*
        Cierra la aplicación JavaFX completamente.
         */
        Platform.exit();
    }


    /* ----------------------------------------------------

                     Metodo cambiarTema

       ----------------------------------------------------

      Alterna entre lod CSS para el modo claro y oscuro.
       La lógica se basa en la propiedad "dark-mode" almacenada en la Scene.
     */
    @FXML
    private void cambiarTema() {
        Scene escena = tablaTareas.getScene();
        if (escena == null) return; // Protección

        /*
         Determinar el estado actual del modo oscuro.
         */
        boolean esModoOscuroActual = escena.getProperties().getOrDefault("dark-mode", false).equals(true);

        /*
         Obtener las rutas de los archivos CSS.
         */
        String temaClaroPath = getClass().getResource("/css/temaClaro.css").toExternalForm();
        String temaOscuroPath = getClass().getResource("/css/temaOscuro.css").toExternalForm();

        /*
         LÓGICA DE SWITCH: Quitar uno y poner el otro.
         */
        if (esModoOscuroActual) {
            /*
             Cambiar a CLARO
             */
            escena.getStylesheets().remove(temaOscuroPath);
            escena.getStylesheets().add(temaClaroPath);
            escena.getProperties().put("dark-mode", false);

        } else {
            /*
             Cambiar a OSCURO
             */
            escena.getStylesheets().remove(temaClaroPath);
            escena.getStylesheets().add(temaOscuroPath);
            escena.getProperties().put("dark-mode", true);
        }
    }


    /*  -------------------------------

            Metodo acercaDeTaskeasy

        -------------------------------
    Simplemente muestra un mensaje con la info acerca de quien lo ha desarrollado(Aitor Benito Heras)
    */
    @FXML
    private void acercaDeTaskeasy() {
        mostrarAlerta("Acerca de TaskEasy",
                "Versión 1.0\nDesarrollado por Aitor Benito Heras\nProyecto Final CFGS DAM - Ilerna Online");
    }

    /* ------------------------------------------------------------------------

              Metodo para la Leyenda de colores de la parte inferior

       ------------------------------------------------------------------------ */

    // Construye dinámicamente la leyenda de colores y la añade al contenedor HBox.
    private void crearLeyendaColorInferior() {
        if (contLeyenda == null) return;

        contLeyenda.setSpacing(10);
        contLeyenda.getChildren().setAll(
                componentesLeyenda("Completada", "#b6f7b0"),
                componentesLeyenda("En curso", "#cfe3ff"),
                componentesLeyenda("Pendiente", "#fff4a3"),
                componentesLeyenda("Sin estado", "#ffd4a3")
        );
    }

    /*
    Para la definicion de los componentes de la leyenda, crea un componente HBox simple
    con un bloque de color y una etiqueta de texto indicando que color es
     */
    private HBox componentesLeyenda(String texto, String colorHex) {
        HBox contenedor = new HBox(5);
        Region color = new Region();
        color.setPrefSize(16, 16);
        color.setStyle("-fx-background-color:" + colorHex + "; -fx-border-color:#888;");
        contenedor.getChildren().addAll(color, new Label(texto));
        return contenedor;
    }


    /* ------------------------------------------------------------------------

               Metodo para mostrar alertas informativas al usuario

      ------------------------------------------------------------------------ */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle(titulo);
        aviso.setHeaderText(null);
        aviso.setContentText(mensaje);
        aviso.showAndWait();
    }
}