package com.aitorbenito.taskeasy;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Tarea {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty titulo;
    private final SimpleStringProperty descripcion;
    private final SimpleStringProperty fecha;
    private final SimpleStringProperty estado;

    public Tarea(int id, String titulo, String descripcion, String fecha, String estado) {
        this.id = new SimpleIntegerProperty(id);
        this.titulo = new SimpleStringProperty(titulo);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.fecha = new SimpleStringProperty(fecha);
        this.estado = new SimpleStringProperty(estado);
    }

    public int getId() { return id.get(); }
    public String getTitulo() { return titulo.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public String getFecha() { return fecha.get(); }
    public String getEstado() { return estado.get(); }

    public SimpleStringProperty tituloProperty() { return titulo; }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
    public SimpleStringProperty fechaProperty() { return fecha; }
    public SimpleStringProperty estadoProperty() { return estado; }
}
