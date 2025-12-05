package com.aitorbenito.taskeasy;

public class Session {

    /**
     Variable estática que almacena el ID del usuario que ha iniciado sesión.
     El uso de la palabra clave `static` permite que esta variable sea accesible
     sin necesidad de crear una instancia de la clase Session (Session.usuarioActual).
     */
    private static int usuarioActual = 0;

    // Se recomienda añadir un constructor privado (público en este caso) para
    // evitar que se creen instancias de esta clase, reforzando el patrón estático.
    private Session() {
        // Constructor vacío y privado.
    }

    /**
     Establece el ID del usuario que ha iniciado sesión.
     */
    public static void setUsuarioActual(int id) {
        usuarioActual = id;
    }

    /**
     Devuelve el ID del usuario activo como int.
     */
    public static int getUsuarioActual() {
        return usuarioActual;
    }
}