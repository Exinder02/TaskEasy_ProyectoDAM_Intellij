package com.aitorbenito.taskeasy;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Tarea {

    // El uso de SimpleXxxProperty es la clave de JavaFX para el Data Binding.
    // Permite que la vista (TableView) observe estos campos.
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty titulo;
    private final SimpleStringProperty descripcion;
    private final SimpleStringProperty fecha;
    private final SimpleStringProperty estado;

    /**
     * Constructor principal, usado al mapear datos de la BD.
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
     * Métodos property(): Devolviendo el objeto `Property`, permiten la
     * conexión directa (binding) entre los datos y los componentes de JavaFX.
     */
    public SimpleStringProperty tituloProperty() { return titulo; }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
    public SimpleStringProperty fechaProperty() { return fecha; }
    public SimpleStringProperty estadoProperty() { return estado; }
}