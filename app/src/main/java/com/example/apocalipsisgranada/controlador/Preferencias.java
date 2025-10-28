package com.example.apocalipsisgranada.controlador;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;

public class Preferencias{

    private static final String NOMBRE_PREFS = "configuracion";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE);
    }

    // 🟩 GUARDAR NOMBRE DE USUARIO
    public static void guardarNombreUsuario(Context context, String nombre) {
        synchronized (Preferencias.class) {
            getPrefs(context).edit().putString("nombreUsuario", nombre).commit(); // commit para asegurar escritura inmediata
        }
    }

    // 🟩 OBTENER NOMBRE DE USUARIO
    public static String obtenerNombreUsuario(Context context) {
        return getPrefs(context).getString("nombreUsuario", "");
    }

    // 🟩 COMPROBAR SI HAY USUARIO
    public static boolean hayUsuario(Context context) {
        String nombre = obtenerNombreUsuario(context);
        return nombre != null && !nombre.isEmpty();
    }

    // 🟩 CERRAR SESIÓN (borra usuario + notificaciones)
    public static void cerrarSesion(Context context) {
        synchronized (Preferencias.class) {
            SharedPreferences prefs = getPrefs(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("nombreUsuario");
            for (String clave : prefs.getAll().keySet()) {
                if (clave.startsWith("notificado_dia_")) {
                    editor.remove(clave);
                }
            }
            editor.commit();
        }
    }

    // 🟩 REINICIAR SIMULACIÓN (mantiene usuario y modo desarrollador)
    public static void reiniciarSimulacion(Context context, boolean modoDev) {
        synchronized (Preferencias.class) {
            SharedPreferences prefs = getPrefs(context);
            SharedPreferences.Editor editor = prefs.edit();

            String nombre = prefs.getString("nombreUsuario", "");
            editor.clear();
            editor.putString("nombreUsuario", nombre);
            editor.putBoolean("modoDesarrollador", modoDev);
            editor.putInt("diaActual", 1);
            editor.putLong("fechaInicio", System.currentTimeMillis());
            editor.commit();
        }
    }
}
