package com.aitorbenito.taskeasy;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Clase Tarea
 * --------------------------------------------
 * Representa una tarea individual dentro de TaskEasy.
 *
 * Esta clase funciona como un "modelo de datos" dentro de la arquitectura MVC:
 *  - Guarda la información que proviene de la base de datos.
 *  - Proporciona propiedades (Property) para que la tabla JavaFX (TableView)
 *    pueda actualizarse automáticamente cuando cambian los valores.
 *
 * ¿Por qué usamos SimpleXxxProperty en lugar de variables normales?
 * -----------------------------------------------------------------
 *  - TableView y otros controles de JavaFX funcionan con propiedades observables.
 *  - Cuando una propiedad cambia, la interfaz puede actualizarse sola.
 *  - Esto permite, por ejemplo, editar tareas en un futuro directamente desde la tabla.
 *
 * Campos incluidos en esta clase:
 *  - id → ID único en la base de datos (INTEGER AUTOINCREMENT)
 *  - titulo → Texto principal de la tarea
 *  - descripcion → Contenido o explicación ampliada
 *  - fecha → Fecha formateada en dd/MM/yyyy o “Sin fecha establecida”
 *  - estado → Pendiente / En curso / Completada
 */
public class Tarea {

    /** ID de la tarea en la base de datos (PRIMARY KEY AUTOINCREMENT) */
    private final SimpleIntegerProperty id;

    /** Título breve y obligatorio de la tarea */
    private final SimpleStringProperty titulo;

    /** Texto detallado o comentarios de la tarea */
    private final SimpleStringProperty descripcion;

    /** Fecha límite o asignada (texto, no LocalDate) */
    private final SimpleStringProperty fecha;

    /** Estado de la tarea: Pendiente, En curso o Completada */
    private final SimpleStringProperty estado;

    /**
     * Constructor principal.
     *
     * Se usa en MainController al cargar tareas desde SQLite.
     *
     * @param id ID único de la tarea
     * @param titulo título de la tarea
     * @param descripcion descripción o contenido
     * @param fecha fecha formateada en texto
     * @param estado estado de la tarea
     */
    public Tarea(int id, String titulo, String descripcion, String fecha, String estado) {
        this.id = new SimpleIntegerProperty(id);
        this.titulo = new SimpleStringProperty(titulo);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.fecha = new SimpleStringProperty(fecha);
        this.estado = new SimpleStringProperty(estado);
    }

    /** Métodos GETTERS: devuelven el valor primitivo (String o int) */
    public int getId() { return id.get(); }
    public String getTitulo() { return titulo.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public String getFecha() { return fecha.get(); }
    public String getEstado() { return estado.get(); }

    /**
     * Métodos property(): permiten que la tabla observe los cambios.
     * JavaFX usa estas propiedades para enlazar columnas con los datos.
     */
    public SimpleStringProperty tituloProperty() { return titulo; }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
    public SimpleStringProperty fechaProperty() { return fecha; }
    public SimpleStringProperty estadoProperty() { return estado; }
}
