/*Creado por Aitor Benito Heras "ExInDer"*/
package com.aitorbenito.taskeasy;

/*
Imports javaFX
*/
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/* ---------------------------------

            Clase Tarea

   ---------------------------------

   Representa las filas de las tareas que tenemos creadas dentro de nuestra base de datos
   Habilita el enlace de datos en javaFX, permite que la tabla de la interfaz, se conecte
   y muestre los datos de la tarea de forma dinámica.
*/
public class Tarea {

    /*
    El uso de SimpleXxxProperty es la clave de JavaFX para el Data Binding.
    Permite que la vista (TableView) observe estos campos.
    */
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty titulo;
    private final SimpleStringProperty descripcion;
    private final SimpleStringProperty fecha;
    private final SimpleStringProperty estado;


    /* -------------------------------------------------

           Constructor principal de la clase Tarea

       -------------------------------------------------
        Usado para poder mapear datos de la BaseDeDatos.
     */
    public Tarea(int id, String titulo, String descripcion, String fecha, String estado) {
        this.id = new SimpleIntegerProperty(id);
        this.titulo = new SimpleStringProperty(titulo);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.fecha = new SimpleStringProperty(fecha);
        this.estado = new SimpleStringProperty(estado);
    }

    /* ----------------------------------

            Métodos GETTERS:

       ---------------------------------
    Devuelven el valor primitivo (String o int) de cada uno de los campos de las columnas de la tabla
    */
    public int getId() { return id.get(); }
    public String getTitulo() { return titulo.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public String getFecha() { return fecha.get(); }
    public String getEstado() { return estado.get(); }

    /* ------------------------------------

                Métodos property()

      ------------------------------------
     Devolviendo el objeto "property":
     Permiten la conexión directa entre los datos y los componentes de JavaFX.
     */
    public SimpleStringProperty tituloProperty() { return titulo; }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
    public SimpleStringProperty fechaProperty() { return fecha; }
    public SimpleStringProperty estadoProperty() { return estado; }
}