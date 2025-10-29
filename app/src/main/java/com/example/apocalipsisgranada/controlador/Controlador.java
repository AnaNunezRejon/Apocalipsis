package com.example.apocalipsisgranada.controlador;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.modelo.Mensaje;
import com.example.apocalipsisgranada.vista.LoginActivity;
import com.example.apocalipsisgranada.vista.VistaGuia;
import com.example.apocalipsisgranada.vista.VistaHistorial;
import com.example.apocalipsisgranada.vista.VistaPrincipal;
import com.example.apocalipsisgranada.vista.VistaServicios;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * Resumen rápido del flujo — Controlador.java
 *
 * Esta clase agrupa **toda la lógica común** que antes estaba repartida
 * en las distintas pantallas (BaseActivity).
 * Aquí se concentran los métodos que todas las vistas comparten:
 * - Configurar el modo desarrollador (escudo, colores y botones)
 * - Configurar el menú inferior de navegación
 * - Avanzar o reiniciar los días de simulación
 * - Mostrar notificaciones, reproducir sonidos
 * - Actualizar colores y cabeceras
 * - Mostrar el saludo del usuario y cerrar sesión
 *
 * En resumen: es el “centro de control” de toda la app.
 * Las demás actividades simplemente llaman a estos métodos.
 */


public class Controlador {

    private static final int TOQUES_DESARROLLADOR = 5;
    private static boolean enReinicio = false;

    // ============================================================
    // CONFIGURAR MODO DESARROLLADOR (ESCUDO + BOTONES)
    // ============================================================
    public static void configurarModoDesarrolladorComun(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        LinearLayout contenedorDev = activity.findViewById(R.id.contenedorBotonesDev);
        ImageView escudo = activity.findViewById(R.id.escudo);
        Button botonAvanzar = activity.findViewById(R.id.botonAvanzar);
        Button botonReiniciar = activity.findViewById(R.id.botonReiniciar);

        if (escudo == null) return;

        boolean modoDev = prefs.getBoolean("modoDesarrollador", false);
        if (contenedorDev != null)
            contenedorDev.setVisibility(modoDev ? View.VISIBLE : View.GONE);

        escudo.setImageResource(modoDev ? R.drawable.escudo_espania_negro : R.drawable.escudo_espania);

        escudo.setOnClickListener(v -> {
            int toques = prefs.getInt("contadorToques", 0) + 1;
            if (toques >= TOQUES_DESARROLLADOR) {
                boolean nuevoModo = !modoDev;
                prefs.edit()
                        .putBoolean("modoDesarrollador", nuevoModo)
                        .putInt("contadorToques", 0)
                        .apply();

                Toast.makeText(activity,
                        nuevoModo ? "🔧 Modo desarrollador ACTIVADO" : "Modo desarrollador DESACTIVADO",
                        Toast.LENGTH_SHORT).show();

                Intent intent = activity.getIntent();
                activity.finish();
                activity.overridePendingTransition(0, 0);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            } else {
                prefs.edit().putInt("contadorToques", toques).apply();
            }
        });

        if (botonAvanzar != null)
            botonAvanzar.setOnClickListener(v -> avanzarDiaComun(activity));

        if (botonReiniciar != null)
            botonReiniciar.setOnClickListener(v -> reiniciarSimulacionComun(activity));
    }

    // ============================================================
    // MENÚ INFERIOR COMÚN
    // ============================================================
    public static void configurarMenuInferior(Activity activity) {
        LinearLayout menuInferior = activity.findViewById(R.id.menuInferior);
        if (menuInferior == null) return;

        View botonInicio = menuInferior.findViewById(R.id.botonInicio);
        View botonGuia = menuInferior.findViewById(R.id.botonGuia);
        View botonHistorial = menuInferior.findViewById(R.id.botonHistorial);
        View botonServicios = menuInferior.findViewById(R.id.botonServicios);

        if (botonInicio != null) botonInicio.setAlpha(activity instanceof VistaPrincipal ? 0.5f : 1f);
        if (botonGuia != null) botonGuia.setAlpha(activity instanceof VistaGuia ? 0.5f : 1f);
        if (botonHistorial != null) botonHistorial.setAlpha(activity instanceof VistaHistorial ? 0.5f : 1f);
        if (botonServicios != null) botonServicios.setAlpha(activity instanceof VistaServicios ? 0.5f : 1f);

        if (botonInicio != null)
            botonInicio.setOnClickListener(v -> {
                if (!(activity instanceof VistaPrincipal))
                    activity.startActivity(new Intent(activity, VistaPrincipal.class));
            });
        if (botonGuia != null)
            botonGuia.setOnClickListener(v -> {
                if (!(activity instanceof VistaGuia))
                    activity.startActivity(new Intent(activity, VistaGuia.class));
            });
        if (botonHistorial != null)
            botonHistorial.setOnClickListener(v -> {
                if (!(activity instanceof VistaHistorial))
                    activity.startActivity(new Intent(activity, VistaHistorial.class));
            });
        if (botonServicios != null)
            botonServicios.setOnClickListener(v -> {
                if (!(activity instanceof VistaServicios))
                    activity.startActivity(new Intent(activity, VistaServicios.class));
            });
    }

    // ============================================================
    // DÍAS Y FECHAS
    // ============================================================
    public static String obtenerFechaSimulada(SharedPreferences prefs, int dia) {
        long fechaInicio = prefs.getLong("fechaInicio", 0);
        if (fechaInicio == 0) {
            fechaInicio = System.currentTimeMillis();
            prefs.edit().putLong("fechaInicio", fechaInicio).apply();
        }
        long fechaSimulada = fechaInicio + (long) (dia - 1) * 24 * 60 * 60 * 1000;
        java.text.SimpleDateFormat formato =
                new java.text.SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new java.util.Locale("es", "ES"));
        return formato.format(new java.util.Date(fechaSimulada));
    }

    // ============================================================
    // AVANZAR DÍA
    // ============================================================
    public static void avanzarDiaComun(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);

        int diaActual = prefs.getInt("diaActual", 1);
        int indice = prefs.getInt("indiceMensajeDia", 0);

        // 🔹 Comprobamos cuántos pares hay en el día actual
        int totalPares = contarMensajesDelDia(activity, diaActual);

        if (indice < totalPares - 1) {
            // Todavía quedan mensajes en el día actual
            indice++;
            prefs.edit().putInt("indiceMensajeDia", indice).apply();
        } else {
            // Se han mostrado todos los pares del día → pasar al siguiente día
            diaActual++;
            indice = 0;
            prefs.edit()
                    .putInt("diaActual", diaActual)
                    .putInt("indiceMensajeDia", 0)
                    .apply();
        }

        Toast.makeText(activity, "Avanzaste al día " + diaActual, Toast.LENGTH_SHORT).show();

        // 🔊 Reproducir notificación solo del mensaje nuevo
        procesarAlertasDelDia(activity, diaActual);

        // 🔁 Actualizar la vista actual
        if (activity instanceof VistaPrincipal)
            ((VistaPrincipal) activity).mostrarMensajesIniciales();
        else if (activity instanceof VistaGuia)
            ((VistaGuia) activity).actualizarGuias();
        else if (activity instanceof VistaHistorial)
            ((VistaHistorial) activity).actualizarHistorial();

        actualizarCabecera(activity);
        mostrarTextoModoDesarrollador(activity);
        actualizarColoresModoDesarrollador(activity);
    }

    private static int contarMensajesDelDia(Context context, int diaBuscado) {
        try {
            InputStream is = context.getAssets().open("alertas.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            int contador = 0;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getInt("dia") == diaBuscado)
                    contador++;
            }

            return contador; // número de alertas (pares alerta+guía)
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }


    public static void reiniciarSimulacionComun(Activity activity) {
        if (enReinicio) return;
        enReinicio = true;

        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        boolean modoDev = prefs.getBoolean("modoDesarrollador", false);
        Preferencias.reiniciarSimulacion(activity, modoDev);

        Toast.makeText(activity, "🔄 Reiniciado al día 1", Toast.LENGTH_SHORT).show();

        if (activity instanceof VistaPrincipal)
            ((VistaPrincipal) activity).mostrarMensajesIniciales();
        else if (activity instanceof VistaGuia)
            ((VistaGuia) activity).actualizarGuias();
        else if (activity instanceof VistaHistorial)
            ((VistaHistorial) activity).actualizarHistorial();

        actualizarCabecera(activity);
        mostrarTextoModoDesarrollador(activity);

        enReinicio = false;
    }

    // ============================================================
    // NOTIFICACIONES Y SONIDOS
    // ============================================================
    public static void procesarAlertasDelDia(Context context, int diaActual) {
        SharedPreferences prefs = context.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        try {
            InputStream is = context.getAssets().open("alertas.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            // 🔁 Recorremos las alertas del archivo
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getInt("dia") == diaActual) {
                    Mensaje mensaje = new Mensaje(
                            diaActual,
                            obtenerFechaSimulada(prefs, diaActual),
                            obj.getString("mensaje"),
                            obj.optString("sonido", "false"),
                            "alerta"
                    );
                    reproducirSonido(context, mensaje);
                    mostrarNotificacion(context, mensaje);
                }
            }

            // 🔦 Día 14 a las 23:00 → Linterna SOS
            if (diaActual == 14) {
                Calendar calendario = Calendar.getInstance();
                int hora = calendario.get(Calendar.HOUR_OF_DAY);
                if (hora == 23) {
                    activarLinternaSOS(context);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void mostrarNotificacion(Context context, Mensaje mensaje) {
        String canalId = "alertas_gremlins";
        NotificationManager gestor = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    canalId, "Alertas Apocalipsis Gremlins", NotificationManager.IMPORTANCE_HIGH);
            gestor.createNotificationChannel(canal);
        }

        Intent intent = new Intent(context, VistaPrincipal.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(R.drawable.ic_alerta)
                .setContentTitle("⚠️ Alerta del Gobierno de España")
                .setContentText(mensaje.getTexto())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje.getTexto()))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 400, 200, 400});

        gestor.notify(mensaje.getDia(), builder.build());
    }

    public static void reproducirSonido(Context context, Mensaje mensaje) {
        int sonidoId = mensaje.obtenerRecursoSonido();
        if (sonidoId != 0) {
            MediaPlayer mp = MediaPlayer.create(context, sonidoId);
            mp.start();
        }
    }
    // ============================================================
    // 🔦 EFECTO DE LINTERNA SOS (día 14 a las 23:00)
    // ============================================================
    public static void activarLinternaSOS(Context context) {
        try {
            CameraManager camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = camManager.getCameraIdList()[0];

            // 🧩 Definimos las duraciones
            int corto = 200; // milisegundos
            int largo = 600;
            int pausa = 150;

            // Función interna para parpadear
            Runnable parpadear = () -> {
                try {
                    // S (· · ·)
                    for (int i = 0; i < 3; i++) {
                        camManager.setTorchMode(cameraId, true);
                        Thread.sleep(corto);
                        camManager.setTorchMode(cameraId, false);
                        Thread.sleep(pausa);
                    }

                    Thread.sleep(400); // pequeña pausa entre letras

                    // O (– – –)
                    for (int i = 0; i < 3; i++) {
                        camManager.setTorchMode(cameraId, true);
                        Thread.sleep(largo);
                        camManager.setTorchMode(cameraId, false);
                        Thread.sleep(pausa);
                    }

                    Thread.sleep(400); // pequeña pausa entre letras

                    // S (· · ·)
                    for (int i = 0; i < 3; i++) {
                        camManager.setTorchMode(cameraId, true);
                        Thread.sleep(corto);
                        camManager.setTorchMode(cameraId, false);
                        Thread.sleep(pausa);
                    }

                    // Apagar linterna por seguridad
                    camManager.setTorchMode(cameraId, false);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            // Ejecutar en un hilo separado (para no bloquear la interfaz)
            new Thread(parpadear).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al activar linterna SOS", Toast.LENGTH_SHORT).show();
        }
    }


    // ============================================================
    // COLORES Y CABECERA
    // ============================================================
    public static void actualizarCabecera(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        TextView textoFecha = activity.findViewById(R.id.textoFecha);
        if (textoFecha != null) {
            int diaActual = prefs.getInt("diaActual", 1);
            textoFecha.setText("Hoy es " + obtenerFechaSimulada(prefs, diaActual));
        }
    }

    public static void actualizarColoresModoDesarrollador(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        boolean modoDev = prefs.getBoolean("modoDesarrollador", false);

        View cabecera = activity.findViewById(R.id.cabecera);
        View fondoMensajes = activity.findViewById(R.id.fondoMensajes);
        LinearLayout menuInferior = activity.findViewById(R.id.menuInferior);
        Button botonAvanzar = activity.findViewById(R.id.botonAvanzar);
        Button botonReiniciar = activity.findViewById(R.id.botonReiniciar);

        if (modoDev) {
            if (cabecera != null) cabecera.setBackgroundColor(activity.getColor(R.color.verdeDev));
            //if (menuInferior != null) menuInferior.setBackgroundColor(activity.getColor(R.color.verdeDev));
            //if (fondoMensajes != null) fondoMensajes.setBackgroundColor(activity.getColor(R.color.rosaDev));
            if (botonAvanzar != null)
                botonAvanzar.setBackgroundTintList(activity.getColorStateList(R.color.rosaDev));
            if (botonReiniciar != null)
                botonReiniciar.setBackgroundTintList(activity.getColorStateList(R.color.rosaDev));
        } else {
            if (cabecera != null) cabecera.setBackgroundColor(activity.getColor(R.color.amarilloGobierno));
            if (menuInferior != null) menuInferior.setBackgroundColor(activity.getColor(R.color.azulGobierno));
            if (fondoMensajes != null) fondoMensajes.setBackgroundColor(activity.getColor(R.color.azulGobierno));
            if (botonAvanzar != null)
                botonAvanzar.setBackgroundTintList(activity.getColorStateList(R.color.rojoBandera));
            if (botonReiniciar != null)
                botonReiniciar.setBackgroundTintList(activity.getColorStateList(R.color.rojoBandera));
        }
    }

    public static void mostrarTextoModoDesarrollador(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        boolean modoDev = prefs.getBoolean("modoDesarrollador", false);
        int diaActual = prefs.getInt("diaActual", 1);

        TextView textoModo = activity.findViewById(R.id.textoModo);
        if (textoModo != null) {
            if (modoDev) {
                textoModo.setVisibility(View.VISIBLE);
                textoModo.setText("🧪 Modo desarrollador — Día " + diaActual);
            } else {
                textoModo.setVisibility(View.GONE);
            }
        }
    }

    // ============================================================
    // MOSTRAR SALUDO PERSONALIZADO + CERRAR SESIÓN
    // ============================================================
    public static void mostrarSaludoUsuario(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        TextView textoSaludo = activity.findViewById(R.id.textoSaludo);
        TextView textoCerrarSesion = activity.findViewById(R.id.textoCerrarSesion);

        if (textoSaludo != null) {
            String nombre = Preferencias.obtenerNombreUsuario(activity);
            if (nombre != null && !nombre.isEmpty()) {
                String nombreCapitalizado =
                        nombre.substring(0, 1).toUpperCase() + nombre.substring(1).toLowerCase();
                textoSaludo.setText("Hola " + nombreCapitalizado);
            } else {
                textoSaludo.setText("Hola usuario");
            }
        }

        if (textoCerrarSesion != null) {
            textoCerrarSesion.setOnClickListener(v -> {
                // 🧹 Cerrar sesión y reiniciar
                Preferencias.cerrarSesion(activity);
                Preferencias.reiniciarSimulacion(activity, false);

                prefs.edit().putBoolean("primer_arranque", true).apply();

                Toast.makeText(activity, "Sesión cerrada. Reiniciando aplicación...", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(activity, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                activity.finish();
            });
        }
    }
}

/**
 * ⚙️ Resumen rápido del flujo — Controlador.java
 *
 * Esta clase agrupa **toda la lógica común** que antes estaba repartida
 * en las distintas pantallas (BaseActivity).
 * Aquí se concentran los métodos que todas las vistas comparten:
 * - Configurar el modo desarrollador (escudo, colores y botones)
 * - Configurar el menú inferior de navegación
 * - Avanzar o reiniciar los días de simulación
 * - Mostrar notificaciones, reproducir sonidos
 * - Actualizar colores y cabeceras
 * - Mostrar el saludo del usuario y cerrar sesión
 *
 * En resumen: es el “centro de control” de toda la app.
 * Las demás actividades simplemente llaman a estos métodos.
 *
 * 🧭 Flujo general de uso
 *  ├─ Cada pantalla (VistaPrincipal, VistaGuia, etc.) llama en onCreate():
 *  │     ├─ Controlador.configurarModoDesarrolladorComun(this)
 *  │     ├─ Controlador.configurarMenuInferior(this)
 *  │     ├─ Controlador.actualizarCabecera(this)
 *  │     ├─ Controlador.mostrarSaludoUsuario(this)
 *  │     ├─ Controlador.actualizarColoresModoDesarrollador(this)
 *  │     └─ Controlador.mostrarTextoModoDesarrollador(this)
 *  └─ Así se mantiene un comportamiento idéntico en todas las vistas.
 *
 * ============================================================
 * 🧩 configurarModoDesarrolladorComun(Activity activity)
 * ============================================================
 *  ├─ Activa o desactiva el modo desarrollador tocando el escudo 5 veces.
 *  ├─ Si se activa → muestra botones de avanzar y reiniciar días.
 *  ├─ Cambia el color del escudo (normal o negro).
 *  ├─ Guarda el estado del modo en SharedPreferences ("modoDesarrollador").
 *  ├─ Reinicia la pantalla actual para aplicar los nuevos colores.
 *  ├─ Botón “Avanzar día” llama a avanzarDiaComun(activity)
 *  └─ Botón “Reiniciar” llama a reiniciarSimulacionComun(activity)
 *
 * ============================================================
 * 🧭 configurarMenuInferior(Activity activity)
 * ============================================================
 *  ├─ Activa los botones de navegación del menú inferior:
 *  │     - Inicio
 *  │     - Guía
 *  │     - Historial
 *  │     - Servicios
 *  ├─ Resalta con opacidad la pantalla en la que estás.
 *  └─ Al pulsar en un botón, abre la actividad correspondiente con un Intent.
 *
 * ============================================================
 * 📅 obtenerFechaSimulada(SharedPreferences prefs, int dia)
 * ============================================================
 *  ├─ Calcula la “fecha simulada” de cada día del juego.
 *  ├─ Usa la fecha de inicio guardada (“fechaInicio”) y suma días.
 *  └─ Devuelve una fecha en formato: “lunes, 28 de octubre de 2025”.
 *
 * ============================================================
 * 🟡 avanzarDiaComun(Activity activity)
 * ============================================================
 *  ├─ Avanza el juego un día o un mensaje más dentro del mismo día.
 *  ├─ Lee “diaActual” y “indiceMensajeDia” desde las preferencias.
 *  ├─ Si aún quedan mensajes en el mismo día → incrementa el índice.
 *  ├─ Si ya no quedan más → pasa al siguiente día y reinicia el índice.
 *  ├─ Guarda los cambios en SharedPreferences.
 *  ├─ Muestra un Toast “Avanzaste al día X”.
 *  ├─ Llama a procesarAlertasDelDia(activity, diaActual) para mostrar notificaciones.
 *  ├─ Actualiza la pantalla actual:
 *  │     ├─ VistaPrincipal → mostrarMensajesIniciales()
 *  │     ├─ VistaGuia → actualizarGuias()
 *  │     └─ VistaHistorial → actualizarHistorial()
 *  └─ Actualiza cabecera, modo desarrollador y colores.
 *
 * ============================================================
 * contarMensajesDelDia(Context context, int diaBuscado)
 * ============================================================
 *  ├─ Abre el archivo “alertas.json”.
 *  ├─ Cuenta cuántos mensajes hay para el día indicado.
 *  └─ Devuelve ese número (sirve para saber si hay 1 o 2 alertas por día).
 *
 * ============================================================
 * 🔁 reiniciarSimulacionComun(Activity activity)
 * ============================================================
 *  ├─ Reinicia el progreso del juego al día 1.
 *  ├─ Conserva el nombre del usuario y el modo desarrollador.
 *  ├─ Llama a Preferencias.reiniciarSimulacion(activity, modoDev)
 *  ├─ Muestra un Toast “Reiniciado al día 1”.
 *  ├─ Actualiza la pantalla en la que esté el usuario.
 *  └─ Evita doble clics usando la variable enReinicio.
 *
 * ============================================================
 * 🔔 procesarAlertasDelDia(Context context, int diaActual)
 * ============================================================
 *  ├─ Lee “alertas.json” y busca las alertas del día actual.
 *  ├─ Por cada alerta:
 *  │     ├─ Crea un objeto Mensaje con el texto y sonido
 *  │     ├─ Llama a reproducirSonido()
 *  │     └─ Llama a mostrarNotificacion()
 *  └─ Así las notificaciones aparecen aunque no estés en la pantalla principal.
 *
 * ============================================================
 * mostrarNotificacion(Context context, Mensaje mensaje)
 * ============================================================
 *  ├─ Crea un canal de notificaciones “alertas_gremlins”.
 *  ├─ Crea la notificación con icono, texto y vibración.
 *  ├─ Si el usuario pulsa la notificación → abre VistaPrincipal.
 *  └─ Usa NotificationCompat para compatibilidad en Android.
 *
 * ============================================================
 * reproducirSonido(Context context, Mensaje mensaje)
 * ============================================================
 *  ├─ Reproduce el sonido asociado a una alerta.
 *  ├─ Usa MediaPlayer.create(context, idDelSonido).
 *  └─ Solo suena si el mensaje tiene un recurso válido.
 *
 * ============================================================
 * 🎨 actualizarCabecera(Activity activity)
 * ============================================================
 *  ├─ Cambia el texto del TextView “textoFecha”.
 *  ├─ Muestra “Hoy es [fecha simulada del día actual]”.
 *
 * ============================================================
 * 🎨 actualizarColoresModoDesarrollador(Activity activity)
 * ============================================================
 *  ├─ Cambia los colores de cabecera, fondo y botones
 *  ├─ Si modo desarrollador = true:
 *  │     ├─ Cabecera verde, botones rosa.
 *  │     └─ Fondo opcional diferente.
 *  └─ Si modo normal:
 *        ├─ Cabecera amarilla (Gobierno)
 *        ├─ Fondo azul
 *        └─ Botones rojos (bandera).
 *
 * ============================================================
 * 🧪 mostrarTextoModoDesarrollador(Activity activity)
 * ============================================================
 *  ├─ Busca el TextView con id “textoModo”.
 *  ├─ Si modo desarrollador está activado → lo muestra con el día actual.
 *  └─ Si no → lo oculta.
 *
 * ============================================================
 * 👋 mostrarSaludoUsuario(Activity activity)
 * ============================================================
 *  ├─ Busca los TextView “textoSaludo” y “textoCerrarSesion”.
 *  ├─ Muestra “Hola [nombre del usuario]”.
 *  ├─ Si no hay usuario → “Hola usuario”.
 *  ├─ Si se pulsa “Cerrar sesión”:
 *  │     ├─ Borra usuario y notificaciones guardadas.
 *  │     ├─ Reinicia simulación y vuelve al LoginActivity.
 *  │     └─ Muestra Toast “Sesión cerrada. Reiniciando aplicación...”.
 *
 * 🔁 Relación entre métodos:
 *  configurarModoDesarrolladorComun() → avanzarDiaComun(), reiniciarSimulacionComun()
 *  avanzarDiaComun() → procesarAlertasDelDia(), actualizarCabecera(), mostrarTextoModoDesarrollador()
 *  procesarAlertasDelDia() → mostrarNotificacion(), reproducirSonido()
 *  mostrarSaludoUsuario() → Preferencias.cerrarSesion()
 *
 * 💡 En resumen:
 * Controlador.java es el “cerebro común” de toda la app:
 * maneja la simulación de días, los colores, las notificaciones,
 * el modo desarrollador y la navegación entre pantallas.
 */
