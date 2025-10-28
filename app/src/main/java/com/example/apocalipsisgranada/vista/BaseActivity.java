package com.example.apocalipsisgranada.vista;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.Preferencias;
import com.example.apocalipsisgranada.modelo.Mensaje;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    protected SharedPreferences preferencias;
    private static final int TOQUES_DESARROLLADOR = 5;
    private int contadorToques = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencias = getSharedPreferences("configuracion", MODE_PRIVATE);
    }

    // ============================================================
    // 🧩 MODO DESARROLLADOR COMÚN
    // ============================================================
    protected void configurarModoDesarrolladorComun() {
        LinearLayout contenedorDev = findViewById(R.id.contenedorBotonesDev);
        ImageView escudo = findViewById(R.id.escudo);

        if (contenedorDev == null || escudo == null) return;

        boolean modoDev = preferencias.getBoolean("modoDesarrollador", false);

        // Mostrar u ocultar los botones del modo desarrollador
        if (modoDev) {
            contenedorDev.setVisibility(View.VISIBLE);
            escudo.setImageResource(R.drawable.escudo_espania_negro);
        } else {
            contenedorDev.setVisibility(View.GONE);
            escudo.setImageResource(R.drawable.escudo_espania);
        }

        // Detectar toques en el escudo para activar o desactivar modo desarrollador
        escudo.setOnClickListener(v -> {
            contadorToques++;
            if (contadorToques >= TOQUES_DESARROLLADOR) {
                boolean nuevoModo = !preferencias.getBoolean("modoDesarrollador", false);
                preferencias.edit().putBoolean("modoDesarrollador", nuevoModo).apply();

                if (nuevoModo) {
                    contenedorDev.setVisibility(View.VISIBLE);
                    escudo.setImageResource(R.drawable.escudo_espania_negro);
                    Toast.makeText(this, "🔧 Modo desarrollador ACTIVADO", Toast.LENGTH_SHORT).show();
                } else {
                    contenedorDev.setVisibility(View.GONE);
                    escudo.setImageResource(R.drawable.escudo_espania);
                    Toast.makeText(this, "Modo desarrollador DESACTIVADO", Toast.LENGTH_SHORT).show();
                }

                actualizarColoresModoDesarrollador();
                contadorToques = 0;
            }
        });

        View botonAvanzar = findViewById(R.id.botonAvanzar);
        View botonReiniciar = findViewById(R.id.botonReiniciar);

        if (botonAvanzar != null)
            botonAvanzar.setOnClickListener(v -> avanzarDiaComun());

        if (botonReiniciar != null)
            botonReiniciar.setOnClickListener(v -> reiniciarSimulacionComun());
    }

    // ============================================================
    // 🧭 MENÚ INFERIOR COMÚN
    // ============================================================
    protected void configurarMenuInferior() {
        LinearLayout menuInferior = findViewById(R.id.menuInferior);
        if (menuInferior == null) return;

        View botonInicio = menuInferior.findViewById(R.id.botonInicio);
        View botonGuia = menuInferior.findViewById(R.id.botonGuia);
        View botonHistorial = menuInferior.findViewById(R.id.botonHistorial);
        View botonServicios = menuInferior.findViewById(R.id.botonServicios);

        // Restablecer opacidad
        botonInicio.setAlpha(1f);
        botonGuia.setAlpha(1f);
        botonHistorial.setAlpha(1f);
        botonServicios.setAlpha(1f);

        // Marcar la pestaña actual
        if (this instanceof VistaPrincipal) botonInicio.setAlpha(0.5f);
        else if (this instanceof VistaGuia) botonGuia.setAlpha(0.5f);
        else if (this instanceof VistaHistorial) botonHistorial.setAlpha(0.5f);
        else if (this instanceof VistaServicios) botonServicios.setAlpha(0.5f);

        // Navegación
        botonInicio.setOnClickListener(v -> {
            if (!(this instanceof VistaPrincipal))
                startActivity(new Intent(this, VistaPrincipal.class));
        });
        botonGuia.setOnClickListener(v -> {
            if (!(this instanceof VistaGuia))
                startActivity(new Intent(this, VistaGuia.class));
        });
        botonHistorial.setOnClickListener(v -> {
            if (!(this instanceof VistaHistorial))
                startActivity(new Intent(this, VistaHistorial.class));
        });
        botonServicios.setOnClickListener(v -> {
            if (!(this instanceof VistaServicios))
                startActivity(new Intent(this, VistaServicios.class));
        });
    }

    // ============================================================
    // 📅 FECHA SIMULADA SEGÚN DÍA DEL JUEGO
    // ============================================================
    protected String obtenerFechaSimulada(int dia) {
        long fechaInicio = preferencias.getLong("fechaInicio", 0);
        if (fechaInicio == 0) {
            fechaInicio = System.currentTimeMillis();
            preferencias.edit().putLong("fechaInicio", fechaInicio).apply();
        }

        // Calcular fecha simulada en milisegundos
        long fechaSimulada = fechaInicio + (long) (dia - 1) * 24 * 60 * 60 * 1000;

        // 🟡 Formato con día de la semana y mes completo (como en la pantalla principal)
        java.text.SimpleDateFormat formato =
                new java.text.SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", new java.util.Locale("es", "ES"));

        return formato.format(new java.util.Date(fechaSimulada));
    }

    // ============================================================
    // 📅 OBTENER DÍA ACTUAL SIMULADO
    // ============================================================
    protected int obtenerDiaActualSimulado() {
        long fechaInicio = preferencias.getLong("fechaInicio", 0);
        boolean modoDev = preferencias.getBoolean("modoDesarrollador", false);

        // Si es la primera vez, guardar la fecha actual como inicio
        if (fechaInicio == 0) {
            fechaInicio = System.currentTimeMillis();
            preferencias.edit().putLong("fechaInicio", fechaInicio).apply();
        }

        int diaDev = preferencias.getInt("diaActual", 1);

        if (modoDev) {
            return diaDev; // En modo desarrollador, usamos el contador manual
        } else {
            long diasPasados = (System.currentTimeMillis() - fechaInicio) / (1000 * 60 * 60 * 24);
            return (int) diasPasados + 1; // Día 1 = primer día de uso
        }
    }

    // ============================================================
    // 🔔 NOTIFICACIONES Y SONIDOS
    // ============================================================
    protected void mostrarNotificacion(Mensaje mensaje) {
        if (!mensaje.debeMostrarNotificacion()) return;

        String canalId = "alertas_gremlins";
        NotificationManager gestor = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    canalId,
                    "Alertas Apocalipsis Gremlins",
                    NotificationManager.IMPORTANCE_HIGH
            );
            gestor.createNotificationChannel(canal);
        }

        Intent intent = new Intent(this, VistaPrincipal.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, canalId)
                .setSmallIcon(R.drawable.ic_alerta)
                .setContentTitle("⚠️ Alerta del Gobierno de España")
                .setContentText(mensaje.getTexto())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje.getTexto()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 400, 200, 400});

        gestor.notify(mensaje.getDia(), builder.build());
    }

    protected void reproducirSonido(Mensaje mensaje) {
        int sonidoId = mensaje.obtenerRecursoSonido();
        if (sonidoId != 0) {
            MediaPlayer mp = MediaPlayer.create(this, sonidoId);
            mp.start();
        }
    }

    // ============================================================
    // 🔁 CONTROL DE NOTIFICACIONES
    // ============================================================
    protected boolean yaNotificada(int dia) {
        return preferencias.getBoolean("notificado_dia_" + dia, false);
    }

    // ============================================================
// 🔔 MOSTRAR ALERTAS Y SONIDOS DEL NUEVO DÍA
// ============================================================
    protected void procesarAlertasDelDia(int diaActual) {
        try {
            InputStream is = getAssets().open("alertas.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int dia = obj.getInt("dia");

                if (dia == diaActual) {
                    Mensaje mensaje = new Mensaje(
                            dia,
                            obtenerFechaSimulada(dia),
                            obj.getString("mensaje"),
                            obj.optString("sonido", "false"),
                            "alerta"
                    );

                    // Solo notificar si no se ha hecho ya
                    if (!yaNotificada(diaActual * 10 + i)) {
                        reproducirSonido(mensaje);
                        mostrarNotificacion(mensaje);
                        marcarComoNotificada(diaActual * 10 + i);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void marcarComoNotificada(int dia) {
        preferencias.edit().putBoolean("notificado_dia_" + dia, true).apply();
    }

    // ============================================================
    // 🟡 SIMULACIÓN DE DÍAS
    // ============================================================
    protected void avanzarDiaComun() {
        int diaActual = preferencias.getInt("diaActual", 1);
        diaActual++;
        preferencias.edit().putInt("diaActual", diaActual).apply();

        Toast.makeText(this, "Avanzaste al día " + diaActual, Toast.LENGTH_SHORT).show();

        // 🔊 Nueva línea: reproducir alertas del nuevo día
        procesarAlertasDelDia(diaActual);

        // Actualizar vistas según la pantalla actual
        if (this instanceof VistaPrincipal) {
            ((VistaPrincipal) this).mostrarMensajesIniciales();
        } else if (this instanceof VistaHistorial) {
            ((VistaHistorial) this).actualizarHistorial();
        } else if (this instanceof VistaGuia) {
            ((VistaGuia) this).actualizarGuias();
        }

        actualizarCabecera();
        actualizarTextoModoDesarrollador();
    }

    // ============================================================
// 🔁 REINICIAR SIMULACIÓN SIN BORRAR USUARIO NI MODO DEV
// ============================================================
    private boolean enReinicio = false;

    protected void reiniciarSimulacionComun() {
        if (enReinicio) return; // evita doble clic accidental
        enReinicio = true;

        boolean modoDev = preferencias.getBoolean("modoDesarrollador", false);

        // 🔒 Reinicia solo los datos de simulación (díaActual, fechaInicio, etc.)
        Preferencias.reiniciarSimulacion(this, modoDev);

        Toast.makeText(this, "🔄 Reiniciado al día 1", Toast.LENGTH_SHORT).show();

        if (this instanceof VistaPrincipal) {
            ((VistaPrincipal) this).mostrarMensajesIniciales();
        } else if (this instanceof VistaHistorial) {
            ((VistaHistorial) this).actualizarHistorial();
        } else if (this instanceof VistaGuia) {
            ((VistaGuia) this).actualizarGuias();
        }

        actualizarCabecera();
        actualizarTextoModoDesarrollador();

        enReinicio = false;
    }

    // ============================================================
    // 🗓️ ACTUALIZAR FECHA DE CABECERA EN CUALQUIER PANTALLA
    // ============================================================
    protected void actualizarCabecera() {
        TextView textoFecha = findViewById(R.id.textoFecha);
        if (textoFecha != null) {
            int diaActual = obtenerDiaActualSimulado();
            textoFecha.setText("Hoy es " + obtenerFechaSimulada(diaActual));
        }
    }
    // ============================================================
// 🧩 ACTUALIZAR TEXTO Y COLORES DEL MODO DESARROLLADOR
// ============================================================
    protected void actualizarTextoModoDesarrollador() {
        boolean modoDev = preferencias.getBoolean("modoDesarrollador", false);

        // 🌈 Aplicar los colores inmediatamente
        actualizarColoresModoDesarrollador();

        // 🔁 Refrescar la visibilidad de los botones dev
        LinearLayout contenedorDev = findViewById(R.id.contenedorBotonesDev);
        if (contenedorDev != null) {
            contenedorDev.setVisibility(modoDev ? View.VISIBLE : View.GONE);
        }
    }
    // ============================================================
// 👋 MOSTRAR SALUDO PERSONALIZADO + CERRAR SESIÓN
// ============================================================
    protected void mostrarSaludoUsuario() {
        TextView textoSaludo = findViewById(R.id.textoSaludo);
        TextView textoCerrarSesion = findViewById(R.id.textoCerrarSesion);

        if (textoSaludo != null) {
            String nombre = Preferencias.obtenerNombreUsuario(this);
            if (nombre != null && !nombre.isEmpty()) {
                String nombreCapitalizado =
                        nombre.substring(0, 1).toUpperCase() + nombre.substring(1).toLowerCase();
                textoSaludo.setText("Hola " + nombreCapitalizado);
            } else {
                textoSaludo.setText("Hola usuario");
            }
        }

        // 🟦 Configurar botón de cerrar sesión
        if (textoCerrarSesion != null) {
            textoCerrarSesion.setOnClickListener(v -> {
                // 🧹 1️⃣ Cerrar sesión (borra usuario y notificaciones)
                Preferencias.cerrarSesion(this);

                // 🔁 2️⃣ Reiniciar simulación
                Preferencias.reiniciarSimulacion(this, false);

                // 🟡 3️⃣ Volver a marcar primer arranque
                getSharedPreferences("configuracion", MODE_PRIVATE)
                        .edit()
                        .putBoolean("primer_arranque", true)
                        .commit();

                Toast.makeText(this, "Sesión cerrada. Reiniciando aplicación...", Toast.LENGTH_SHORT).show();

                // 🚀 4️⃣ Redirigir al login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }

        }
    // ============================================================
// 🎨 APLICAR COLORES SEGÚN MODO DESARROLLADOR
// ============================================================
    protected void actualizarColoresModoDesarrollador() {
        boolean modoDev = preferencias.getBoolean("modoDesarrollador", false);

        View cabecera = findViewById(R.id.cabecera);
        View fondoMensajes = findViewById(R.id.fondoMensajes);
        LinearLayout menuInferior = findViewById(R.id.menuInferior);
        Button botonAvanzar = findViewById(R.id.botonAvanzar);
        Button botonReiniciar = findViewById(R.id.botonReiniciar);

        if (modoDev) {
            // 💚 Verde para cabecera y menú
            if (cabecera != null) cabecera.setBackgroundColor(getColor(R.color.verdeDev));
            if (menuInferior != null) menuInferior.setBackgroundColor(getColor(R.color.verdeDev));

            // 💗 Rosa para fondo de mensajes y botones
            if (fondoMensajes != null) fondoMensajes.setBackgroundColor(getColor(R.color.rosaDev));
            if (botonAvanzar != null) botonAvanzar.setBackgroundTintList(getColorStateList(R.color.rosaDev));
            if (botonReiniciar != null) botonReiniciar.setBackgroundTintList(getColorStateList(R.color.rosaDev));
        } else {
            // 🔙 Volver a los colores normales del tema
            if (cabecera != null) cabecera.setBackgroundColor(getColor(R.color.amarilloGobierno));
            if (menuInferior != null) menuInferior.setBackgroundColor(getColor(R.color.azulGobierno));
            if (fondoMensajes != null) fondoMensajes.setBackgroundColor(getColor(R.color.azulGobierno));
            if (botonAvanzar != null) botonAvanzar.setBackgroundTintList(getColorStateList(R.color.rojoBandera));
            if (botonReiniciar != null) botonReiniciar.setBackgroundTintList(getColorStateList(R.color.rojoBandera));
        }
    }
        }


