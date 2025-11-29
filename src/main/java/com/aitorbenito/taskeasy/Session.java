package com.aitorbenito.taskeasy;
/**

 Clase Session

 Esta clase actúa como un contenedor estático para almacenar la información
 del usuario que ha iniciado sesión en TaskEasy.

 NO maneja inicio/cierre de sesión por sí misma: simplemente guarda el ID del usuario
 que obtuvo LoginController al validar las credenciales contra la base de datos.

 ¿Por qué una clase estática?
 ---------------------------------
 Porque permite:
 - Acceder al ID del usuario desde cualquier parte del programa sin tener que pasar variables entre ventanas.

 - Mantener "viva" la información del usuario durante toda la ejecución
 de la aplicación.

 - Evitar crear instancias: la sesión es única y global.

 ¿Por qué guardar solamente el ID?
 ---------------------------------
 - El ID es suficiente para relacionar cualquier registro del sistema (tareas, configuraciones, etc.) con el usuario.
 - Evita almacenar contraseñas o datos sensibles en memoria.

 Esta clase deberá usarse cada vez que:
 - Se cree una tarea nueva (guarda usuario_id).
 - Se carguen tareas filtradas por usuario.
 - Se implemente un "Cerrar sesión".
 */

public class Session {

    /**
     Variable estática que almacena el ID del usuario que ha iniciado sesión.

     Valor inicial:
     0 → indica que no hay usuario autenticado.

     Cuando LoginController valída correctamente el login,
     llama a Session.setUsuarioActual(ID_del_usuario).
     */
    private static int usuarioActual = 0;

    /**
     Establece el ID del usuario que ha iniciado sesión.

     @param id ID numérico recuperado de la base de datos.
     */
    public static void setUsuarioActual(int id) {
        usuarioActual = id;
    }

    /**
     Devuelve el ID del usuario activo como int.

     Se usa para:
     - Filtrar tareas por usuario en "cargarTareas()".
     - Insertar usuario_id en nuevas tareas.

     @return entero con el ID del usuario autenticado.
     */
    public static int getUsuarioActual() {
        return usuarioActual;
    }
}

