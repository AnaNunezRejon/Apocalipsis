package com.example.apocalipsisgranada.controlador;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;

/**
 * Es como un “archivo de configuración” donde la app guarda datos sencillos (texto, números, booleanos...)
 * para que sigan ahí aunque cierres o apagues la app.
 *
 /**
 * Resumen rápido del flujo — Preferencias.java
 *
 * Esta clase maneja **las configuraciones guardadas del usuario**.
 * Usa un sistema llamado SharedPreferences para guardar datos sencillos
 * (como el nombre del usuario, el día actual o si está activado el modo desarrollador).
 *
 * En resumen:
 * Es como una pequeña libreta de notas donde la app guarda datos
 * que deben mantenerse aunque cierres o apagues el móvil.
 *
 */
public class Preferencias{

    private static final String NOMBRE_PREFS = "configuracion";
    //NOMBRE_PREFS - Es el nombre del archivo donde Android guardará todas las preferencias.

    //SharedPreferences es un archivo de configuración interna de Android.
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE);

        //Este metodo abre el archivo de preferencias llamado "configuracion"
        //y te devuelve un objeto SharedPreferences para poder leer o escribir.
        //Context.MODE_PRIVATE significa que solo tu aplicación puede acceder a este archivo
        //(nadie más desde fuera).
    }

    // GUARDAR NOMBRE DE USUARIO
    public static void guardarNombreUsuario(Context context, String nombre) {
        synchronized (Preferencias.class) {
            getPrefs(context).edit().putString("nombreUsuario", nombre).commit(); // commit para asegurar escritura inmediata
        }
    }

    // OBTENER NOMBRE DE USUARIO
    public static String obtenerNombreUsuario(Context context) {
        return getPrefs(context).getString("nombreUsuario", "");
    }

    // COMPROBAR SI HAY USUARIO
    public static boolean hayUsuario(Context context) {
        String nombre = obtenerNombreUsuario(context);  //Después, aunque cierres la app, puedes recuperarlo:
        return nombre != null && !nombre.isEmpty();
    }

    // CERRAR SESIÓN (borra usuario + notificaciones)
    public static void cerrarSesion(Context context) {
        synchronized (Preferencias.class) {
            SharedPreferences prefs = getPrefs(context);
            SharedPreferences.Editor editor = prefs.edit(); //abrimos el editor
            editor.remove("nombreUsuario");//Borra el usuario guardado.
            for (String clave : prefs.getAll().keySet()) {
                if (clave.startsWith("notificado_dia_")) { //Limpia las notificaciones que empiecen por "notificado_dia_".
                    editor.remove(clave);
                }
            }
            editor.commit();//guardamos los cambios
        }
    }

    // REINICIAR SIMULACIÓN (mantiene usuario y modo desarrollador)
    public static void reiniciarSimulacion(Context context, boolean modoDev) {
        synchronized (Preferencias.class) {

            /**
             * Sirve para que dos procesos no escriban a la vez en las preferencias.
             * Ejemplo:
             * Si guardas el nombre mientras otra parte borra el usuario → puede corromper los datos.
             * Con synchronized, solo una acción puede ejecutarse al mismo tiempo. 🔒
             */


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

/**
 * 💾 Resumen rápido del flujo — Preferencias.java
 *
 * Esta clase maneja **las configuraciones guardadas del usuario**.
 * Usa un sistema llamado SharedPreferences para guardar datos sencillos
 * (como el nombre del usuario, el día actual o si está activado el modo desarrollador).
 *
 * 🧠 En resumen:
 * Es como una pequeña libreta de notas donde la app guarda datos
 * que deben mantenerse aunque cierres o apagues el móvil.
 *
 * ============================================================
 * 🗂️ Estructura general
 * ============================================================
 * NOMBRE_PREFS → “configuracion”
 * Es el nombre del archivo donde se guardan las preferencias.
 *
 * getPrefs(Context context)
 * ├─ Devuelve el acceso al archivo de preferencias de la app.
 * └─ Lo usan todos los demás métodos para leer o escribir datos.
 *
 * ============================================================
 * 🟢 guardarNombreUsuario(Context, String)
 * ============================================================
 * ├─ Guarda el nombre del usuario en las preferencias.
 * ├─ Usa .edit() para abrir el modo de edición.
 * ├─ Usa .putString("nombreUsuario", nombre) para escribir el valor.
 * └─ Usa .commit() para guardar inmediatamente los cambios.
 *    (commit guarda al instante, apply lo hace en segundo plano)
 *
 * ============================================================
 * 🔍 obtenerNombreUsuario(Context)
 * ============================================================
 * ├─ Devuelve el nombre del usuario guardado.
 * └─ Si no existe, devuelve una cadena vacía "".
 *
 * ============================================================
 * ✅ hayUsuario(Context)
 * ============================================================
 * ├─ Comprueba si ya hay un usuario guardado.
 * ├─ Llama a obtenerNombreUsuario().
 * └─ Devuelve true si el nombre no está vacío o null.
 *
 * ============================================================
 * 🚪 cerrarSesion(Context)
 * ============================================================
 * ├─ Borra el nombre del usuario y las alertas guardadas.
 * ├─ Usa editor.remove("nombreUsuario").
 * ├─ También borra todas las claves que empiecen por "notificado_dia_".
 * ├─ Llama a commit() para guardar los cambios inmediatamente.
 * └─ Así, cuando el usuario cierre sesión, empieza desde cero.
 *
 * ============================================================
 * 🔁 reiniciarSimulacion(Context, boolean modoDev)
 * ============================================================
 * ├─ Reinicia la simulación al día 1.
 * ├─ Limpia todas las preferencias, pero mantiene:
 * │     - El nombre de usuario actual.
 * │     - El estado del modo desarrollador.
 * ├─ Guarda de nuevo:
 * │     - nombreUsuario
 * │     - modoDesarrollador
 * │     - diaActual = 1
 * │     - fechaInicio = hora actual del sistema
 * └─ Usa commit() para asegurarse de que se guarde todo inmediatamente.
 *
 * ============================================================
 * 💡 En resumen:
 *  Preferencias.java = “memoria persistente” de la app.
 *  Controlador y las vistas la usan para recordar:
 *   - quién es el usuario
 *   - en qué día está la simulación
 *   - si está en modo desarrollador
 *   - si es el primer arranque o no
 *
 * 🔁 Relación con otras clases:
 *  ├─ LoginActivity → guardarNombreUsuario(), reiniciarSimulacion()
 *  ├─ Controlador → obtenerNombreUsuario(), reiniciarSimulacion()
 *  ├─ VistaPrincipal → usa las preferencias para obtener el día actual
 *  └─ Cerrar sesión → usa cerrarSesion() + reiniciarSimulacion()
 *
 * 📘 Concepto clave:
 *  SharedPreferences = un “archivo XML” interno del sistema Android
 *  donde se guardan pares clave–valor de tipo texto, número o booleano.
 */

