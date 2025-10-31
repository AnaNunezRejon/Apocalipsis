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
 * ============================================================
 * ⚙️ Clase: Preferencias.java
 * ============================================================
 *
 * Gestiona el almacenamiento y recuperación de datos persistentes
 * mediante `SharedPreferences`.
 *
 * Actúa como un **módulo de utilidades** dentro del patrón MVC,
 * separando la lógica de guardado, reinicio y mantenimiento
 * de sesión del resto de las clases (VistaPrincipal, Controlador, etc.).
 *
 * ------------------------------------------------------------
 * ⚙️ Funciones principales (explicadas en detalle)
 * ------------------------------------------------------------
 *
 * 1️⃣ **guardarSesion(Context context, String usuario)**
 * ------------------------------------------------------------
 *   ➤ Objetivo:
 *     Se llama cuando el usuario inicia sesión desde `VistaLogin`.
 *     Inicializa todos los valores básicos del juego y guarda
 *     el nombre del jugador y la fecha de inicio de la simulación.
 *
 *   ➤ Qué guarda exactamente:
 *     - `nombreUsuario`: el nombre introducido en el login.
 *     - `fechaInicio`: la hora actual del sistema (marca el día 1).
 *     - `diaActual`: el primer día de la simulación (valor 1).
 *     - `indiceMensajeDia`: el primer mensaje del día (valor 0).
 *     - `modoDesarrollador`: desactivado por defecto (false).
 *
 *   ➤ Por qué se usa `apply()`:
 *     El método `apply()` guarda los datos de forma asíncrona
 *     (sin bloquear la interfaz) y es más eficiente que `commit()`.
 *
 *   ➤ Interacción:
 *     - Se llama una vez en `VistaLogin` al pulsar “Iniciar sesión”.
 *     - Permite que al abrir `VistaPrincipal` ya haya datos válidos.
 *
 * ------------------------------------------------------------
 *
 * 2️⃣ **cerrarSesion(Context context)**
 * ------------------------------------------------------------
 *   ➤ Objetivo:
 *     Elimina todas las preferencias guardadas del usuario actual.
 *     Se usa cuando se pulsa el texto **"Cerrar sesión"** en la cabecera.
 *
 *   ➤ Qué hace:
 *     - Abre el archivo de preferencias “configuracion”.
 *     - Borra todas las claves guardadas (`clear()`).
 *     - Guarda el cambio inmediatamente con `apply()`.
 *
 *   ➤ Resultado:
 *     - El usuario pierde el progreso y el modo desarrollador.
 *     - La aplicación queda lista para volver al login.
 *
 *   ➤ Interacción:
 *     - Se llama desde `ManejadorVistas` cuando el usuario toca “Cerrar sesión”.
 *     - Después se lanza `VistaLogin` para iniciar una nueva sesión.
 *
 * ------------------------------------------------------------
 *
 * 3️⃣ **reiniciarSimulacion(Context context, boolean modoDesarrollador)**
 * ------------------------------------------------------------
 *   ➤ Objetivo:
 *     Reinicia la simulación al día 1 pero **sin cerrar sesión**.
 *     Mantiene el nombre del usuario y el estado del modo desarrollador.
 *     Se usa en el modo de prueba (cuando se activa el escudo 5 veces).
 *
 *   ➤ Qué hace:
 *     - Recupera el nombre del usuario actual.
 *     - Restablece:
 *          · `diaActual = 1`  (reinicia el progreso)
 *          · `indiceMensajeDia = 0` (primer mensaje del día)
 *          · `fechaInicio = System.currentTimeMillis()` (nuevo inicio)
 *     - Vuelve a guardar `modoDesarrollador` según el valor recibido.
 *
 *   ➤ Por qué se mantiene `modoDesarrollador`:
 *     Si el usuario está probando el juego (modo dev),
 *     puede reiniciar la simulación sin perder el acceso a los botones ocultos.
 *
 *   ➤ Interacción:
 *     - Llamado desde `Controlador.reiniciarSimulacionComun()`.
 *     - Este método se ejecuta cuando el usuario pulsa “REINICIAR DÍAS”.
 *     - Luego `VistaPrincipal` o `VistaGuia` se actualizan al día 1.
 *
 * ------------------------------------------------------------
 * 🗂️ Claves utilizadas en SharedPreferences
 * ------------------------------------------------------------
 *
 *   • `nombreUsuario` → String
 *       → Guarda el nombre actual del jugador.
 *
 *   • `diaActual` → int
 *       → Día de simulación actual (1–14).
 *
 *   • `indiceMensajeDia` → int
 *       → Indica qué mensaje del día se está mostrando.
 *
 *   • `fechaInicio` → long
 *       → Fecha de inicio en milisegundos (System.currentTimeMillis()).
 *         Se usa para calcular la “fecha simulada” mostrada en la cabecera.
 *
 *   • `modoDesarrollador` → boolean
 *       → Indica si el modo desarrollador está activo o no.
 *
 * ------------------------------------------------------------
 * 💡 En resumen:
 * ------------------------------------------------------------
 *
 * `Preferencias.java` centraliza toda la **gestión de persistencia**.
 *
 * ✔️ Evita duplicar código en otras clases.
 * ✔️ Mantiene la sesión activa aunque se cierre la app.
 * ✔️ Permite reiniciar o limpiar la simulación fácilmente.
 * ✔️ Integra con `Controlador` y `ManejadorVistas` para actualizar el estado global.
 *
 * ============================================================
 */
