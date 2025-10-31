package com.example.apocalipsisgranada.controlador;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.modelo.Mensaje;
import com.example.apocalipsisgranada.vista.ManejadorVistas;
import com.example.apocalipsisgranada.vista.VistaGuia;
import com.example.apocalipsisgranada.vista.VistaHistorial;
import com.example.apocalipsisgranada.vista.VistaPrincipal;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Controlador {

    private static final int TOQUES_DESARROLLADOR = 5;
    private static boolean enReinicio = false;

    // ============================================================
    // CONFIGURAR MODO DESARROLLADOR
    // ============================================================
    public static void configurarModoDesarrolladorComun(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        boolean modoDev = prefs.getBoolean("modoDesarrollador", false);

        activity.findViewById(R.id.escudo).setOnClickListener(v -> {
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

        // Botones de avance y reinicio
        if (activity.findViewById(R.id.botonAvanzar) != null)
            activity.findViewById(R.id.botonAvanzar)
                    .setOnClickListener(v -> avanzarDiaComun(activity));

        if (activity.findViewById(R.id.botonReiniciar) != null)
            activity.findViewById(R.id.botonReiniciar)
                    .setOnClickListener(v -> reiniciarSimulacionComun(activity));
    }

    // ============================================================
    // FECHAS Y DÍAS
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

        // Comprobamos cuántos pares hay en el día actual
        int totalPares = contarMensajesDelDia(activity, diaActual);

        if (indice < totalPares - 1) {
            // Quedan más mensajes del mismo día
            indice++;
            prefs.edit().putInt("indiceMensajeDia", indice).apply();
        } else {
            // Ya se mostraron todos los mensajes de ese día → pasar al siguiente
            diaActual++;
            indice = 0;
            prefs.edit()
                    .putInt("diaActual", diaActual)
                    .putInt("indiceMensajeDia", 0)
                    .apply();
        }

        Toast.makeText(activity, "Avanzaste al día " + diaActual, Toast.LENGTH_SHORT).show();

        procesarAlertasDelDia(activity, diaActual);

        // Actualizar la vista correspondiente
        if (activity instanceof VistaPrincipal)
            ((VistaPrincipal) activity).mostrarMensajesIniciales();
        else if (activity instanceof VistaGuia)
            ((VistaGuia) activity).actualizarGuias();
        else if (activity instanceof VistaHistorial)
            ((VistaHistorial) activity).actualizarHistorial();

        // Actualizar visual
        ManejadorVistas.actualizarCabecera(activity, obtenerFechaSimulada(prefs, diaActual));
        ManejadorVistas.mostrarTextoModoDesarrollador(activity, diaActual);
        ManejadorVistas.actualizarColoresModoDesarrollador(activity);
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

            return contador;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    // ============================================================
    // REINICIAR SIMULACIÓN
    // ============================================================
    public static void reiniciarSimulacionComun(Activity activity) {
        if (enReinicio) return;
        enReinicio = true;

        SharedPreferences prefs = activity.getSharedPreferences("configuracion", Context.MODE_PRIVATE);
        boolean modoDev = prefs.getBoolean("modoDesarrollador", false);
        Preferencias.reiniciarSimulacion(activity, modoDev);

        Toast.makeText(activity, "Reiniciado al día 1", Toast.LENGTH_SHORT).show();

        if (activity instanceof VistaPrincipal)
            ((VistaPrincipal) activity).mostrarMensajesIniciales();
        else if (activity instanceof VistaGuia)
            ((VistaGuia) activity).actualizarGuias();
        else if (activity instanceof VistaHistorial)
            ((VistaHistorial) activity).actualizarHistorial();

        int diaActual = prefs.getInt("diaActual", 1);
        ManejadorVistas.actualizarCabecera(activity, obtenerFechaSimulada(prefs, diaActual));
        ManejadorVistas.mostrarTextoModoDesarrollador(activity, diaActual);

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

            // Recuperamos el índice actual
            int indice = prefs.getInt("indiceMensajeDia", 0);

            // Creamos una lista con las alertas del día actual
            List<JSONObject> alertasDelDia = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getInt("dia") == diaActual) {
                    alertasDelDia.add(obj);
                }
            }

            // Si hay alertas y el índice es válido, mostramos solo la que toca
            if (indice < alertasDelDia.size()) {
                JSONObject obj = alertasDelDia.get(indice);

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

            // Linterna SOS solo si es el final del día 14
            if (diaActual == 14 && indice == alertasDelDia.size() - 1) {
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
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(context, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE);

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
    // LINTERNA SOS
    // ============================================================
    public static void activarLinternaSOS(Context context) {
        try {
            CameraManager camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = camManager.getCameraIdList()[0];

            int corto = 200;
            int largo = 600;
            int pausa = 150;

            Runnable parpadear = () -> {
                try {
                    for (int i = 0; i < 3; i++) {
                        camManager.setTorchMode(cameraId, true);
                        Thread.sleep(corto);
                        camManager.setTorchMode(cameraId, false);
                        Thread.sleep(pausa);
                    }

                    Thread.sleep(400);

                    for (int i = 0; i < 3; i++) {
                        camManager.setTorchMode(cameraId, true);
                        Thread.sleep(largo);
                        camManager.setTorchMode(cameraId, false);
                        Thread.sleep(pausa);
                    }

                    Thread.sleep(400);

                    for (int i = 0; i < 3; i++) {
                        camManager.setTorchMode(cameraId, true);
                        Thread.sleep(corto);
                        camManager.setTorchMode(cameraId, false);
                        Thread.sleep(pausa);
                    }

                    camManager.setTorchMode(cameraId, false);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            new Thread(parpadear).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al activar linterna SOS", Toast.LENGTH_SHORT).show();
        }
    }
}

/**
 * ============================================================
 * 🧠 Clase: Controlador.java
 * ============================================================
 *
 * Esta clase centraliza toda la **lógica funcional y de coordinación**
 * de la aplicación “Apocalipsis Granada”.
 *
 * Es el **núcleo del patrón MVC**, actuando como puente entre el modelo
 * (datos, mensajes, preferencias) y las vistas (interfaces gráficas).
 *
 * Gestiona eventos, notificaciones, sonidos, el modo desarrollador,
 * la linterna SOS y el avance del juego día a día.
 *
 * ------------------------------------------------------------
 * ⚙️ Funciones principales
 * ------------------------------------------------------------
 *
 * 1️⃣ **Modo desarrollador**
 *    - Detecta 5 toques en el escudo del Gobierno.
 *    - Activa o desactiva el modo desarrollador en SharedPreferences.
 *    - Recarga la actividad actual para aplicar cambios visuales.
 *    - Muestra mensajes de confirmación (“Modo desarrollador ACTIVADO/DESACTIVADO”).
 *
 * 2️⃣ **Avance de simulación**
 *    - Controla el día actual (`diaActual`) y el índice del mensaje (`indiceMensajeDia`).
 *    - Si aún quedan mensajes del día → muestra el siguiente.
 *    - Si no → pasa al siguiente día.
 *    - Reproduce sonido, notificación y actualiza vistas.
 *
 * 3️⃣ **Reinicio de simulación**
 *    - Restablece el progreso (día 1, índice 0, fecha inicial).
 *    - Mantiene el estado del modo desarrollador activo o no.
 *    - Notifica con un mensaje “Reiniciado al día 1”.
 *
 * 4️⃣ **Procesamiento de alertas**
 *    - Lee `alertas.json` desde /assets.
 *    - Busca las alertas correspondientes al día actual.
 *    - Muestra solo la alerta o guía que toca según el índice.
 *    - Reproduce el sonido y lanza la notificación del Gobierno.
 *
 * 5️⃣ **Notificaciones y sonido**
 *    - Usa `NotificationCompat` para mostrar avisos del “Gobierno de España”.
 *    - Cada mensaje puede incluir vibración y texto expandido.
 *    - Los sonidos se gestionan con `MediaPlayer` y se asocian al tipo de alerta.
 *
 * 6️⃣ **Evento especial — Día 14 (23:00h)**
 *    - Activa la linterna del dispositivo en patrón **SOS (... --- ...)**
 *    - Simula una alerta máxima del Gobierno.
 *
 * ------------------------------------------------------------
 * 🗂️ Datos gestionados (SharedPreferences)
 * ------------------------------------------------------------
 *  - nombreUsuario → Nombre introducido en el login
 *  - diaActual → Día simulado actual
 *  - indiceMensajeDia → Índice del mensaje dentro del día
 *  - fechaInicio → Fecha base desde la que se simula el paso de días
 *  - modoDesarrollador → Estado del modo oculto (true/false)
 *
 * ------------------------------------------------------------
 * 🎨 Integración visual
 * ------------------------------------------------------------
 *  - ManejadorVistas.java → Se encarga de los cambios visuales (cabecera, colores, menú).
 *  - VistaPrincipal.java → Recibe los mensajes y alertas del día.
 *  - VistaGuia.java → Muestra las guías diarias.
 *  - VistaHistorial.java → Lista todas las alertas pasadas.
 *
 * ------------------------------------------------------------
 * 🔁 Flujo resumido
 * ------------------------------------------------------------
 *  🧭 Usuario abre app → Login (guarda nombre y arranque día 1)
 *  📅 VistaPrincipal → Carga mensajes del día actual
 *  ⚙️ Controlador → Comprueba modo desarrollador, muestra alertas
 *  🔔 Procesa notificaciones y sonidos según el día
 *  🚨 Día 14 a las 23:00 → Linterna SOS
 *
 * ------------------------------------------------------------
 * 💡 En resumen:
 * ------------------------------------------------------------
 *
 * `Controlador.java` es el **centro lógico del proyecto**,
 * responsable de toda la interacción entre la historia, los eventos y las vistas.
 *
 * Gestiona el avance, la persistencia, las alertas y las funciones especiales,
 * manteniendo el equilibrio entre jugabilidad e inmersión narrativa.
 *
 * Su diseño modular permite mantener las vistas simples y enfocadas
 * únicamente en la interfaz gráfica, mientras la lógica queda
 * completamente encapsulada aquí.
 *
 * ============================================================
 */

