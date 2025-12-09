package com.aitorbenito.taskeasy;

public class Categoria {
    private int id;
    private String nombre;
    private String color;

    public Categoria(int id, String nombre, String color) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getColor() { return color; }

    @Override
    public String toString() {
        return nombre;  // Esto hace que en un ComboBox aparezca el nombre
    }
}
