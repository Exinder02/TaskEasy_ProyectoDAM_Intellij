/*Creado por Aitor Benito Heras "ExInDer"*/
package com.aitorbenito.taskeasy;

public class SesionUsuario {

    /*
     Variable estática que almacena el ID del usuario que ha iniciado sesión.
     El uso de la palabra clave `static` permite que esta variable sea accesible
     sin necesidad de crear una instancia de la clase SesionUsuario (SesionUsuario.usuarioActual).
     */
    private static int usuarioActual = 0;

    /*
     Establece el ID del usuario que ha iniciado sesión.
     */
    public static void setUsuarioActual(int id) {
        usuarioActual = id;
    }

    /*
     Devuelve el ID del usuario activo como int.
     */
    public static int getUsuarioActual() {
        return usuarioActual;
    }
}