package com.example.apocalipsisgranada.vista;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.Controlador;
import com.example.apocalipsisgranada.modelo.Mensaje;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VistaPrincipal extends AppCompatActivity {

    private SharedPreferences preferencias;
    private RecyclerView recyclerMensajes;
    private List<Mensaje> alertas = new ArrayList<>();
    private List<Mensaje> guias = new ArrayList<>();
    private List<Mensaje> mostrados = new ArrayList<>();

    private static final int PERMISO_NOTIFICACION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        preferencias = getSharedPreferences("configuracion", MODE_PRIVATE);
        recyclerMensajes = findViewById(R.id.recyclerPrincipal);
        recyclerMensajes.setLayoutManager(new LinearLayoutManager(this));

        configurarPrimerArranque();
        comprobarPermisoNotificaciones();

        // Configuración común
        Controlador.configurarModoDesarrolladorComun(this);
        ManejadorVistas.configurarElementosComunes(this);

        // Carga inicial de archivos
        cargarArchivo("alertas.json", "alerta", alertas);
        cargarArchivo("guias.json", "guia", guias);

        // Mostrar los mensajes del día actual
        mostrarMensajesIniciales();
    }

    // ============================================================
    // CONFIGURACIÓN INICIAL
    // ============================================================
    private void configurarPrimerArranque() {
        boolean primeraVez = preferencias.getBoolean("primer_arranque", true);
        if (primeraVez) {
            SharedPreferences.Editor editor = preferencias.edit();
            editor.putBoolean("primer_arranque", false);
            editor.putLong("fechaInicio", System.currentTimeMillis());
            editor.putInt("diaActual", 1);
            editor.putInt("indiceMensajeDia", 0);
            editor.apply();
        }
    }

    private void comprobarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISO_NOTIFICACION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_NOTIFICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ============================================================
    // MOSTRAR MENSAJES
    // ============================================================
    public void mostrarMensajesIniciales() {
        mostrados.clear();

        int diaActual = preferencias.getInt("diaActual", 1);
        int indice = preferencias.getInt("indiceMensajeDia", 0);

        // Mensaje inicial fijo (día 0)
        mostrados.add(new Mensaje(0,
                Controlador.obtenerFechaSimulada(preferencias, 0),
                "Sistema de Alertas del Gobierno de España — Modo activo",
                "false",
                "alerta"));

        // Cargar todos los días anteriores
        for (int d = 1; d < diaActual; d++) {
            List<List<Mensaje>> paresAnteriores = obtenerAmbosMensajesDelDia(d);
            for (int i = 0; i < paresAnteriores.size(); i++) {
                List<Mensaje> par = paresAnteriores.get(i);
                mostrados.addAll(par);
            }

        }

        // Cargar mensajes del día actual (según índice)
        List<List<Mensaje>> paresHoy = obtenerAmbosMensajesDelDia(diaActual);
        for (int i = 0; i <= indice && i < paresHoy.size(); i++) {
            mostrados.addAll(paresHoy.get(i));
        }

        // Ordenar de más nuevo a más antiguo
        Collections.reverse(mostrados);

        recyclerMensajes.setAdapter(new AdaptadorMensajes(mostrados, this));
        ManejadorVistas.actualizarCabecera(this, Controlador.obtenerFechaSimulada(preferencias, diaActual));
    }

    // ============================================================
    // OBTENER PARES ALERTA + GUÍA DE UN DÍA
    // ============================================================
    private List<List<Mensaje>> obtenerAmbosMensajesDelDia(int dia) {
        List<Mensaje> alertasDia = new ArrayList<>();
        List<Mensaje> guiasDia = new ArrayList<>();
        List<List<Mensaje>> pares = new ArrayList<>();

        // Buscar alertas del día
        for (int i = 0; i < alertas.size(); i++) {
            Mensaje a = alertas.get(i);
            if (a.getDia() == dia) alertasDia.add(a);
        }

        // Buscar guías del día
        for (int i = 0; i < guias.size(); i++) {
            Mensaje g = guias.get(i);
            if (g.getDia() == dia) guiasDia.add(g);
        }

        int total = Math.max(alertasDia.size(), guiasDia.size());
        for (int i = 0; i < total; i++) {
            List<Mensaje> par = new ArrayList<>();
            if (i < alertasDia.size()) par.add(alertasDia.get(i));
            if (i < guiasDia.size()) par.add(guiasDia.get(i));
            pares.add(par);
        }
        return pares;
    }

    // ============================================================
    // CARGAR ARCHIVOS JSON
    // ============================================================
    private void cargarArchivo(String archivo, String tipo, List<Mensaje> destino) {
        try {
            InputStream is = getAssets().open(archivo);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            destino.clear();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int dia = obj.getInt("dia");
                String texto = obj.getString("mensaje");
                String sonido = obj.optString("sonido", "false");
                String fecha = Controlador.obtenerFechaSimulada(preferencias, dia);
                destino.add(new Mensaje(dia, fecha, texto, sonido, tipo));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * ============================================================
 * 🏛️ Clase: VistaPrincipal.java
 * ============================================================
 *
 * Representa la **pantalla central** de la aplicación “Apocalipsis Granada”.
 *
 * Es la vista principal del usuario, donde se muestra la **historia diaria** del juego:
 * las alertas del Gobierno y las guías de actuación correspondientes al día actual.
 *
 * Forma parte del patrón MVC como la **VISTA principal**,
 * encargada exclusivamente de mostrar los datos que le proporciona el Controlador.
 *
 * ------------------------------------------------------------
 * ⚙️ Funciones principales
 * ------------------------------------------------------------
 *
 * 1️⃣ **Carga inicial de la vista**
 *     - Infla el layout `activity_principal.xml`.
 *     - Configura los componentes visuales comunes (cabecera, menú inferior, modo dev).
 *     - Obtiene el día actual desde SharedPreferences.
 *     - Muestra los mensajes correspondientes (alerta + guía) del día.
 *
 * 2️⃣ **Gestión de mensajes**
 *     - Utiliza un `RecyclerView` con un `AdaptadorMensajes` personalizado.
 *     - Carga los datos desde los archivos JSON (`alertas.json` y `guias.json`).
 *     - Muestra los mensajes combinados (alerta + guía) de cada día.
 *     - Reacciona al avance de día actualizando el contenido mostrado.
 *
 * 3️⃣ **Integración con el Controlador**
 *     - Llama a `Controlador.configurarModoDesarrolladorComun(this)` para:
 *         - Detectar toques en el escudo.
 *         - Habilitar los botones de avanzar y reiniciar en modo desarrollador.
 *     - Usa `Controlador.procesarAlertasDelDia()` para generar sonidos y notificaciones.
 *     - Actualiza la fecha simulada mediante `Controlador.obtenerFechaSimulada()`.
 *
 * 4️⃣ **Integración con ManejadorVistas**
 *     - `ManejadorVistas.configurarElementosComunes(this)` → carga cabecera y menú.
 *     - `ManejadorVistas.mostrarTextoModoDesarrollador()` → muestra barra “🧪 Modo desarrollador”.
 *     - `ManejadorVistas.actualizarColoresModoDesarrollador()` → aplica los colores dev (verde/rosa).
 *
 * ------------------------------------------------------------
 * 🗂️ Elementos visuales destacados
 * ------------------------------------------------------------
 *
 *  Layout: `activity_principal.xml`
 *
 *  - 🟨 **Cabecera reutilizada:** `@layout/cabecera`
 *      - Escudo (activa modo desarrollador)
 *      - TextView saludo (“Hola, [usuario]”)
 *      - Fecha simulada (“Hoy es lunes, 27 de octubre de 2025”)
 *
 *  - 🧪 **Indicador modo desarrollador:**
 *      - `@id/textoModo` → barra amarilla o verde según el modo.
 *
 *  - 💬 **Centro de mensajes (RecyclerView):**
 *      - `@id/recyclerPrincipal` → lista los mensajes del día actual.
 *
 *  - ⚫ **Colores dinámicos según el modo:**
 *      - Modo normal → Amarillo + Azul Gobierno.
 *      - Modo desarrollador → Verde + Rosa.
 *
 * ------------------------------------------------------------
 * 🔁 Flujo de funcionamiento
 * ------------------------------------------------------------
 *
 *  1️⃣ Al iniciar la app, se carga VistaPrincipal.
 *  2️⃣ Se lee el usuario y día actual desde SharedPreferences.
 *  3️⃣ Se configuran cabecera, menú inferior y modo desarrollador.
 *  4️⃣ Se obtienen las alertas y guías del día mediante el Controlador.
 *  5️⃣ Se muestran en el RecyclerView.
 *  6️⃣ Al pulsar “Avanzar día”:
 *      - El Controlador incrementa el día o el índice.
 *      - Se actualizan mensajes, sonidos y notificaciones.
 *  7️⃣ Si se alcanza el día 14 a las 23:00 → se activa la linterna SOS.
 *
 * ------------------------------------------------------------
 * 🧩 Integración con otras vistas
 * ------------------------------------------------------------
 *
 *  - **VistaGuia.java** → lista todas las guías pasadas.
 *  - **VistaHistorial.java** → muestra alertas anteriores.
 *  - **VistaServicios.java** → enlaces rápidos a servicios oficiales.
 *  - **ManejadorVistas.java** → gestiona la interfaz visual común.
 *  - **Controlador.java** → controla la lógica y el avance de días.
 *
 * ------------------------------------------------------------
 * 💡 En resumen:
 * ------------------------------------------------------------
 *
 * `VistaPrincipal.java` es el **centro narrativo y visual** del proyecto.
 *
 * Su misión es mostrar al jugador la evolución de la historia día a día,
 * conectando la interfaz (RecyclerView, cabecera, menú) con la lógica del
 * Controlador.
 *
 * Gracias a su integración con `ManejadorVistas`, mantiene coherencia visual
 * con el resto de pantallas, adaptando automáticamente colores, menús y
 * elementos del modo desarrollador.
 *
 * Es la pantalla que define la experiencia principal del usuario.
 *
 * ============================================================
 */


