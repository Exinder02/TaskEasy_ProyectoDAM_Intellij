package com.aitorbenito.taskeasy;

public class Session {
    private static int usuarioActual = 0;

    public static void setUsuarioActual(int id) {
        usuarioActual = id;
    }

    public static int getUsuarioActual() {
        return usuarioActual;
    }
}
