package com.example.apocalipsisgranada.vista;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.Controlador;

public class ManejadorVistas {

    // ============================================================
    // CONFIGURAR ELEMENTOS COMUNES DE TODAS LAS VISTAS
    // (se llama en onCreate() de cada Activity de la app salvo Login)
    // ============================================================
    public static void configurarElementosComunes(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        boolean modoDev = prefs.getBoolean("modoDesarrollador", false);
        int diaActual = prefs.getInt("diaActual", 1);
        String nombreUsuario = prefs.getString("nombreUsuario", "Usuario");

        // Saludo ("Hola ANA") y fecha simulada ("Hoy es ...")
        TextView textoSaludo = activity.findViewById(R.id.textoSaludo);
        TextView textoFecha = activity.findViewById(R.id.textoFecha);

        if (textoSaludo != null) {
            textoSaludo.setText("Hola " + nombreUsuario.toUpperCase());
        }

        if (textoFecha != null) {
            textoFecha.setText("Hoy es " + Controlador.obtenerFechaSimulada(prefs, diaActual));
        }

        // Mostrar / ocultar barra "🧪 Modo desarrollador — Día X"
        mostrarTextoModoDesarrollador(activity, diaActual);

        // Mostrar / ocultar botones de avanzar / reiniciar según modoDev
        LinearLayout contenedorBotones = activity.findViewById(R.id.contenedorBotonesDev);
        if (contenedorBotones != null) {
            contenedorBotones.setVisibility(modoDev ? View.VISIBLE : View.GONE);
        }

        // Botones propios de cabecera (Avanzar, Reiniciar, Cerrar sesión)
        configurarBotonesCabecera(activity);

        // Menú inferior (Inicio | Guía | Historial | Servicios)
        configurarMenuInferior(activity);

        // Colores según modo desarrollador
        actualizarColoresModoDesarrollador(activity);
    }


    // ============================================================
    // ACTUALIZAR SOLO LA FECHA DE LA CABECERA
    // ============================================================
    public static void actualizarCabecera(Activity activity, String fecha) {
        TextView textoFecha = activity.findViewById(R.id.textoFecha);
        if (textoFecha != null) {
            textoFecha.setText("Hoy es " + fecha);
        }
    }


    // ============================================================
    // MOSTRAR TEXTO DEL MODO DESARROLLADOR
    // (la barra amarilla/verde que dice "🧪 Modo desarrollador — Día X")
    // ============================================================
    public static void mostrarTextoModoDesarrollador(Activity activity, int diaActual) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        boolean modoDev = prefs.getBoolean("modoDesarrollador", false);

        TextView textoModo = activity.findViewById(R.id.textoModo);
        if (textoModo != null) {
            if (modoDev) {
                textoModo.setVisibility(View.VISIBLE);
                textoModo.setText("🧪 Modo desarrollador — Día " + diaActual);
            } else {
                textoModo.setVisibility(View.GONE);
            }
        }

        LinearLayout contenedorBotones = activity.findViewById(R.id.contenedorBotonesDev);
        if (contenedorBotones != null) {
            contenedorBotones.setVisibility(modoDev ? View.VISIBLE : View.GONE);
        }
    }


    // ============================================================
    // COLORES DEL FONDO SEGÚN EL MODO
    // - modo normal: azulGobierno
    // - modo dev: rosaDesarrollador
    // ============================================================
    public static void actualizarColoresModoDesarrollador(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        boolean modoDev = prefs.getBoolean("modoDesarrollador", false);

        // Cabecera
        LinearLayout cabecera = activity.findViewById(R.id.cabecera);
        if (cabecera != null) {
            int colorCabecera = activity.getResources().getColor(
                    modoDev ? R.color.verdeDev : R.color.amarilloGobierno);
            cabecera.setBackgroundColor(colorCabecera);
        }

        // Botones modo desarrollador
        Button botonAvanzar = activity.findViewById(R.id.botonAvanzar);
        Button botonReiniciar = activity.findViewById(R.id.botonReiniciar);
        if (botonAvanzar != null) {
            botonAvanzar.setBackgroundTintList(activity.getResources().getColorStateList(
                    modoDev ? R.color.rosaDev : R.color.rojoBandera));
        }
        if (botonReiniciar != null) {
            botonReiniciar.setBackgroundTintList(activity.getResources().getColorStateList(
                    modoDev ? R.color.rosaDev : R.color.rojoBandera));
        }

        // ESCUDO: cambia entre el normal y el negro
        ImageView escudo = activity.findViewById(R.id.escudo);
        if (escudo != null) {
            escudo.setImageResource(modoDev ? R.drawable.escudo_espania_negro : R.drawable.escudo_espania);
        }
    }


    // ============================================================
    // CABECERA: BOTONES AVANZAR / REINICIAR / CERRAR SESIÓN
    // ============================================================
    public static void configurarBotonesCabecera(Activity activity) {
        Button botonAvanzar = activity.findViewById(R.id.botonAvanzar);
        Button botonReiniciar = activity.findViewById(R.id.botonReiniciar);
        TextView textoCerrarSesion = activity.findViewById(R.id.textoCerrarSesion);

        if (botonAvanzar != null) {
            botonAvanzar.setOnClickListener(v -> Controlador.avanzarDiaComun(activity));
        }

        if (botonReiniciar != null) {
            botonReiniciar.setOnClickListener(v -> Controlador.reiniciarSimulacionComun(activity));
        }

        if (textoCerrarSesion != null) {
            textoCerrarSesion.setOnClickListener(v -> cerrarSesion(activity));
        }
    }


    // ============================================================
    // CERRAR SESIÓN (vuelve al login y limpia preferencias)
    // ============================================================
    public static void cerrarSesion(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }


    // ============================================================
    // MENÚ INFERIOR (NAVEGACIÓN ENTRE PANTALLAS)
    // ============================================================
    public static void configurarMenuInferior(Activity activity) {

        // cada item del menú inferior es un LinearLayout clicable
        View botonInicio     = activity.findViewById(R.id.botonInicio);
        View botonGuia       = activity.findViewById(R.id.botonGuia);
        View botonHistorial  = activity.findViewById(R.id.botonHistorial);
        View botonServicios  = activity.findViewById(R.id.botonServicios);

        // Inicio
        if (botonInicio != null) {
            botonInicio.setOnClickListener(v -> {
                // evitar reabrir la misma Activity
                if (!(activity instanceof VistaPrincipal)) {
                    Intent i = new Intent(activity, VistaPrincipal.class);
                    activity.startActivity(i);
                    activity.finish();
                }
            });
        }

        // Guía
        if (botonGuia != null) {
            botonGuia.setOnClickListener(v -> {
                if (!(activity instanceof VistaGuia)) {
                    Intent i = new Intent(activity, VistaGuia.class);
                    activity.startActivity(i);
                    activity.finish();
                }
            });
        }

        // Historial
        if (botonHistorial != null) {
            botonHistorial.setOnClickListener(v -> {
                if (!(activity instanceof VistaHistorial)) {
                    Intent i = new Intent(activity, VistaHistorial.class);
                    activity.startActivity(i);
                    activity.finish();
                }
            });
        }

        // Servicios
        if (botonServicios != null) {
            botonServicios.setOnClickListener(v -> {
                if (!(activity instanceof VistaServicios)) {
                    Intent i = new Intent(activity, VistaServicios.class);
                    activity.startActivity(i);
                    activity.finish();
                }
            });
        }

        // marcar el botón activo con menos opacidad en la Activity actual
        marcarSeccionActual(activity, botonInicio, botonGuia, botonHistorial, botonServicios);
    }


    // ============================================================
    // VISUAL: MARCAR QUÉ PANTALLA ESTÁ ACTIVA EN EL MENÚ INFERIOR
    // baja la opacidad del botón activo para dar feedback visual
    // ============================================================
    private static void marcarSeccionActual(
            Activity activity,
            View botonInicio,
            View botonGuia,
            View botonHistorial,
            View botonServicios
    ) {
        if (botonInicio == null || botonGuia == null || botonHistorial == null || botonServicios == null) {
            // si alguna pantalla no tiene menú completo, salimos
            return;
        }

        // valores por defecto (todos visibles al 100%)
        float alphaInicio = 1f;
        float alphaGuia = 1f;
        float alphaHistorial = 1f;
        float alphaServicios = 1f;

        // bajamos opacidad SOLO del que corresponde a la Activity actual
        if (activity instanceof VistaPrincipal) {
            alphaInicio = 0.4f;
        } else if (activity instanceof VistaGuia) {
            alphaGuia = 0.4f;
        } else if (activity instanceof VistaHistorial) {
            alphaHistorial = 0.4f;
        } else if (activity instanceof VistaServicios) {
            alphaServicios = 0.4f;
        }

        botonInicio.setAlpha(alphaInicio);
        botonGuia.setAlpha(alphaGuia);
        botonHistorial.setAlpha(alphaHistorial);
        botonServicios.setAlpha(alphaServicios);
    }
}
/**
 * ============================================================
 * 🧩 Clase: ManejadorVistas.java
 * ============================================================
 *
 * Se encarga de **gestionar y actualizar los elementos visuales comunes**
 * de todas las pantallas (vistas) de la aplicación:
 *
 * - Cabecera (nombre, fecha, escudo, botón cerrar sesión)
 * - Modo desarrollador (color, texto, botones especiales)
 * - Colores dinámicos según el estado del juego o modo dev
 *
 * Actúa como el “cerebro visual” del proyecto, coordinando la parte
 * gráfica mientras `Controlador` maneja la lógica y `Preferencias` la persistencia.
 *
 * ------------------------------------------------------------
 * ⚙️ Funciones principales
 * ------------------------------------------------------------
 *
 * 1️⃣ **configurarElementosComunes(Activity activity)**
 * ------------------------------------------------------------
 *   ➤ Objetivo:
 *     Inicializa todos los elementos compartidos de la interfaz
 *     que deben estar presentes en cualquier vista:
 *       - Cabecera con saludo, fecha y escudo.
 *       - Indicador del modo desarrollador.
 *       - Botones de avance / reinicio (solo visibles en modo dev).
 *
 *   ➤ Qué hace:
 *     - Llama a `mostrarSaludoUsuario()` para mostrar el nombre actual.
 *     - Usa `Controlador.obtenerFechaSimulada()` para calcular la fecha.
 *     - Configura el texto del modo desarrollador si está activo.
 *     - Cambia los colores visuales según el modo actual.
 *
 *   ➤ Interacción:
 *     - Se invoca desde cada Activity (VistaPrincipal, VistaGuia, VistaHistorial...).
 *     - Asegura coherencia visual entre todas las pantallas.
 *
 * ------------------------------------------------------------
 *
 * 2️⃣ **mostrarSaludoUsuario(Activity activity)**
 * ------------------------------------------------------------
 *   ➤ Objetivo:
 *     Muestra el texto de bienvenida en la cabecera,
 *     recuperando el nombre del usuario almacenado en `SharedPreferences`.
 *
 *   ➤ Qué hace:
 *     - Lee `nombreUsuario` del archivo de preferencias.
 *     - Escribe “Hola [NOMBRE]” en el `TextView` correspondiente.
 *     - Asocia un listener al texto “Cerrar sesión” para
 *       ejecutar `Preferencias.cerrarSesion(context)` y volver al login.
 *
 *   ➤ Interacción:
 *     - Llamada automática al abrir cualquier vista.
 *     - Facilita la desconexión rápida del usuario desde la cabecera.
 *
 * ------------------------------------------------------------
 *
 * 3️⃣ **actualizarCabecera(Activity activity, String fechaFormateada)**
 * ------------------------------------------------------------
 *   ➤ Objetivo:
 *     Actualiza dinámicamente el texto que muestra la fecha simulada
 *     en la cabecera (por ejemplo: “Hoy es martes, 14 de octubre de 2025”).
 *
 *   ➤ Qué hace:
 *     - Localiza el `TextView` de la fecha.
 *     - Inserta el texto recibido formateado desde `Controlador.obtenerFechaSimulada()`.
 *
 *   ➤ Interacción:
 *     - Llamada desde `Controlador.avanzarDiaComun()` o `reiniciarSimulacionComun()`
 *       cada vez que se avanza de día o se reinicia la historia.
 *
 * ------------------------------------------------------------
 *
 * 4️⃣ **mostrarTextoModoDesarrollador(Activity activity, int diaActual)**
 * ------------------------------------------------------------
 *   ➤ Objetivo:
 *     Muestra o esconde la franja amarilla/verde con el texto
 *     “🧪 Modo desarrollador — Día X” dependiendo del estado del modo.
 *
 *   ➤ Qué hace:
 *     - Lee `modoDesarrollador` de las preferencias.
 *     - Si está activo → muestra el `TextView` y actualiza el texto con el día actual.
 *     - Si no está activo → oculta el componente (`setVisibility(GONE)`).
 *
 *   ➤ Interacción:
 *     - Llamada automáticamente al iniciar la vista.
 *     - También se llama desde `Controlador` cuando el usuario activa el modo dev.
 *
 * ------------------------------------------------------------
 *
 * 5️⃣ **actualizarColoresModoDesarrollador(Activity activity)**
 * ------------------------------------------------------------
 *   ➤ Objetivo:
 *     Cambia los colores visuales principales de la interfaz
 *     según si el modo desarrollador está activado o no.
 *
 *   ➤ Qué hace:
 *     - Cambia el color de fondo de la cabecera.
 *     - Modifica el color de los botones “Avanzar” y “Reiniciar”.
 *     - Sustituye el escudo de España normal por la versión negra en modo dev.
 *     - Si el modo dev está desactivado, restaura los colores originales.
 *
 *   ➤ Interacción:
 *     - Llamado desde `configurarElementosComunes()` y `Controlador.avanzarDiaComun()`.
 *     - Se ejecuta cada vez que se activa o desactiva el modo desarrollador.
 *
 * ------------------------------------------------------------
 * 🧠 Flujo de uso en conjunto con otras clases
 * ------------------------------------------------------------
 *
 * - `VistaPrincipal`, `VistaGuia`, `VistaHistorial` → llaman a `configurarElementosComunes()`
 *     para que todo se pinte igual en cada pantalla.
 *
 * - `Controlador` → usa `actualizarCabecera()` y `mostrarTextoModoDesarrollador()`
 *     tras avanzar de día o reiniciar.
 *
 * - `Preferencias` → provee la información (usuario, modo dev, día actual)
 *     que `ManejadorVistas` muestra visualmente.
 *
 * ------------------------------------------------------------
 * 🎨 Elementos visuales gestionados
 * ------------------------------------------------------------
 *
 *   🟨 Cabecera:
 *      - Nombre del usuario.
 *      - Fecha simulada.
 *      - Escudo (normal o negro).
 *      - Texto “Cerrar sesión”.
 *
 *   🟢 Modo desarrollador:
 *      - Franja amarilla o verde.
 *      - Botones “AVANZAR DÍA” y “REINICIAR DÍAS”.
 *
 * ------------------------------------------------------------
 * 💡 En resumen:
 * ------------------------------------------------------------
 *
 * `ManejadorVistas.java` coordina todos los **elementos visuales comunes**
 * para que cada pantalla mantenga coherencia con el resto.
 *
 * ✔️ Unifica el aspecto visual de toda la app.
 * ✔️ Reacciona automáticamente al estado del modo desarrollador.
 * ✔️ Facilita la interacción con `Controlador` y `Preferencias`.
 * ✔️ Reduce código duplicado en las vistas individuales.
 *
 * ============================================================
 */

