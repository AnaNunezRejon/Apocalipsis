package com.example.apocalipsisgranada.vista;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.Controlador;
import com.example.apocalipsisgranada.controlador.AdaptadorMensajes;
import com.example.apocalipsisgranada.modelo.Mensaje;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resumen rápido del flujo
 *
 * La app abre esta pantalla → entra en onCreate()
 * Dentro de onCreate() se configuran los ajustes básicos
 * Se cargan las alertas y guías desde los archivos
 * Se llama a mostrarMensajesIniciales()
 * Esa función junta todas las alertas y guías que deben mostrarse
 * El resultado se enseña en el RecyclerView (la lista de mensajes en pantalla)
 *
 */

public class VistaPrincipal extends AppCompatActivity {

    private RecyclerView recyclerMensajes; //Donde se van a mostrar todos los mensajes
    private SharedPreferences preferencias; //Donde se guardan las preferencias (modo desarrollador, día actual, etc.)
    private final List<Mensaje> alertas = new ArrayList<>();
    private final List<Mensaje> guias = new ArrayList<>();
    private final List<Mensaje> mostrados = new ArrayList<>(); //Lista final que contiene los mensajes que se muestran en pantalla

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        preferencias = getSharedPreferences("configuracion", MODE_PRIVATE);
        recyclerMensajes = findViewById(R.id.recyclerPrincipal);
        recyclerMensajes.setLayoutManager(new LinearLayoutManager(this));

        // ⚙Configuración inicial
        configurarPrimerArranque();
        Controlador.configurarModoDesarrolladorComun(this);
        Controlador.configurarMenuInferior(this);
        Controlador.mostrarSaludoUsuario(this);
        Controlador.mostrarTextoModoDesarrollador(this);

        // Permiso de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setTitle("Permiso necesario")
                        .setMessage("Esta aplicación necesita enviar notificaciones para avisarte de alertas importantes del Gobierno de España. ¿Deseas activarlas?")
                        .setPositiveButton("Sí, activar", (dialog, which) -> {
                            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
                        })
                        .setNegativeButton("No, más tarde", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }

        // Cargar datos desde assets
        cargarArchivo("alertas.json", "alerta", alertas);
        cargarArchivo("guias.json", "guia", guias);

        // Mostrar estado inicial
        Controlador.actualizarColoresModoDesarrollador(this);
        Controlador.actualizarCabecera(this);
        mostrarMensajesIniciales();
    }

    private void configurarPrimerArranque() {
        boolean primeraVez = preferencias.getBoolean("primer_arranque", true);
        if (primeraVez) {
            SharedPreferences.Editor editor = preferencias.edit();
            editor.putBoolean("primer_arranque", false);
            editor.putLong("fechaInicio", System.currentTimeMillis());// Guarda la fecha en que se inicia el “día 1”
            editor.putInt("diaActual", 1);// Empieza en el día 1
            editor.putInt("indiceMensajeDia", 0); // Empieza mostrando el primer mensaje del día
            editor.apply();
        }
    }

    // ============================================================
    // MUESTRA LOS MENSAJES CORRESPONDIENTES SEGÚN EL DÍA
    // ============================================================

    public void mostrarMensajesIniciales() {

        mostrados.clear();
        mostrados.add(new Mensaje(0, "23/09/2025", "🌧️ Alerta por lluvias en la provincia de Granada.", "false", "alerta"));

        //Recupera el día actual y el índice de mensaje (por si hay varios en el mismo día)
        int diaActual = preferencias.getInt("diaActual", 1);
        int indice = preferencias.getInt("indiceMensajeDia", 0);

        //Añade los mensajes de los dias anteriores, si vas por el dia 5, carga los mensajes del dia 1 al 4
        for (int d = 1; d < diaActual; d++) {
            List<List<Mensaje>> mensajesAlertayGuiaDia = obtenerAmbosMensajesDelDia(d); //Obiene todas las parejas
            for (List<Mensaje> par : mensajesAlertayGuiaDia) mostrados.addAll(par); //Mete esas parejas en mostrados
        }

        //Añade mensajes del dia actual
        List<List<Mensaje>> mensajesAlertayGuiaHoy = obtenerAmbosMensajesDelDia(diaActual);
        for (int i = 0; i <= indice && i < mensajesAlertayGuiaHoy.size(); i++) mostrados.addAll(mensajesAlertayGuiaHoy.get(i));

        //Muestra los mensajes mas recientes primero
        Collections.sort(mostrados, (m1, m2) -> Integer.compare(m2.getDia(), m1.getDia()));
        recyclerMensajes.setAdapter(new AdaptadorMensajes(mostrados, this));//Muestra los mensajes en pantalla

        Controlador.actualizarCabecera(this);//Actualiza la fecha de cabecera (“Hoy es…”)

        //Si hay una añerta nueva - sonido + notificación
        if (!mensajesAlertayGuiaHoy.isEmpty() && indice < mensajesAlertayGuiaHoy.size()) {
            Mensaje nuevaAlerta = mensajesAlertayGuiaHoy.get(indice).get(0);
            if ("alerta".equals(nuevaAlerta.getTipo())) {
                Controlador.reproducirSonido(this, nuevaAlerta);
                Controlador.mostrarNotificacion(this, nuevaAlerta);
            }
        }
    }

    // ============================================================
    // UNE ALERTAS Y GUÍAS DE UN MISMO DÍA EN “PAREJAS”
    // ============================================================
    public List<List<Mensaje>> obtenerAmbosMensajesDelDia(int dia) {
        List<Mensaje> alertasDia = new ArrayList<>();
        List<Mensaje> guiasDia = new ArrayList<>();

        //Busca todas las alertas y mensajes que pertenecen a ese día
        for (Mensaje alerta : alertas)
            if (alerta.getDia() == dia) alertasDia.add(alerta);
        for (Mensaje guia : guias)
            if (guia.getDia() == dia) guiasDia.add(guia);

        List<List<Mensaje>> pares = new ArrayList<>();//Combina ambos tipos (alerta + guía) en pares
        int total = Math.max(alertasDia.size(), guiasDia.size());

        for (int i = 0; i < total; i++) {
            List<Mensaje> par = new ArrayList<>();
            if (i < alertasDia.size()) par.add(alertasDia.get(i));// añade alerta si existe
            if (i < guiasDia.size()) par.add(guiasDia.get(i));// añade guía si existe
            pares.add(par);
        }
        return pares; // devuelve lista de “parejas” de mensajes de ese día
    }


    // ============================================================
    // CARGA LOS MENSAJES DESDE UN ARCHIVO JSON
    // ============================================================
    public void cargarArchivo(String archivo, String tipo, List<Mensaje> destino) {
        try {

            //Abre el archivo
            InputStream is = getAssets().open(archivo);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            //Convierte el archivo en texto y luego en JSON
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            //Recorre cada objeto dentro del JSON y crea un “Mensaje”
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Mensaje mensaje = new Mensaje(
                        obj.getInt("dia"),
                        Controlador.obtenerFechaSimulada(preferencias, obj.getInt("dia")),
                        obj.getString("mensaje"),
                        obj.optString("sonido", "false"),
                        tipo
                );
                destino.add(mensaje); // lo añade a la lista correspondiente
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar " + archivo, Toast.LENGTH_SHORT).show();
        }
    }
}

/**
 * 🏛️ Resumen rápido del flujo — VistaPrincipal
 *
 * Esta es la pantalla principal del juego/aplicación.
 * Aquí se muestran todas las alertas y guías del Gobierno de España
 * que corresponden a los días simulados (pasados y el actual).
 * También se reproducen los sonidos y notificaciones cuando hay alertas nuevas.
 *
 * 📲 Flujo general:
 * La app abre esta pantalla → entra en onCreate()
 * Dentro de onCreate() se configuran los elementos comunes (menú, cabecera, colores, modo desarrollador)
 * Se cargan los archivos "alertas.json" y "guias.json"
 * Se prepara el RecyclerView que mostrará los mensajes
 * Finalmente se llama a mostrarMensajesIniciales(), que decide qué mensajes enseñar en pantalla
 *
 * 🟩 onCreate()
 *  ├─ setContentView(R.layout.activity_principal)
 *  ├─ preferencias = getSharedPreferences("configuracion", MODE_PRIVATE)
 *  ├─ recyclerMensajes = findViewById(R.id.recyclerPrincipal)
 *  ├─ recyclerMensajes.setLayoutManager(new LinearLayoutManager(this))
 *  ├─ configurarPrimerArranque()
 *  ├─ Controlador.configurarModoDesarrolladorComun(this)
 *  ├─ Controlador.configurarMenuInferior(this)
 *  ├─ Controlador.mostrarSaludoUsuario(this)
 *  ├─ Controlador.mostrarTextoModoDesarrollador(this)
 *  ├─ (Comprueba permiso de notificaciones)
 *  ├─ cargarArchivo("alertas.json", "alerta", alertas)
 *  ├─ cargarArchivo("guias.json", "guia", guias)
 *  ├─ Controlador.actualizarColoresModoDesarrollador(this)
 *  ├─ Controlador.actualizarCabecera(this)
 *  └─ mostrarMensajesIniciales()
 *
 * 🟨 configurarPrimerArranque()
 *  ├─ Comprueba si es la primera vez que se abre la app (clave "primer_arranque")
 *  ├─ Si es la primera vez → guarda la fecha actual como inicio y pone el díaActual = 1
 *  └─ También guarda el índice de mensaje en 0
 *
 * 🟦 mostrarMensajesIniciales()
 *  ├─ Limpia la lista de mensajes mostrados (mostrados.clear())
 *  ├─ Añade un mensaje inicial por defecto (día 0)
 *  ├─ Obtiene el día actual desde preferencias
 *  ├─ Obtiene el índice de mensaje actual (para no repetir)
 *  ├─ Recorre los días anteriores (for d = 1 hasta díaActual-1)
 *  │     ├─ Llama a obtenerAmbosMensajesDelDia(d)
 *  │     └─ Añade todos los mensajes de esos días a la lista "mostrados"
 *  ├─ Obtiene los mensajes del día actual (obtenerAmbosMensajesDelDia(diaActual))
 *  ├─ Añade solo los que tocan según el índice guardado
 *  ├─ Ordena todos los mensajes de más nuevo a más antiguo
 *  ├─ Crea un AdaptadorMensajes con esa lista y lo pone en el RecyclerView
 *  ├─ Actualiza la cabecera con la fecha simulada
 *  └─ Si hay una alerta nueva, reproduce sonido y muestra notificación
 *
 * 🟩 obtenerAmbosMensajesDelDia(int dia)
 *  ├─ Busca los mensajes (alerta + guía) que correspondan a ese día
 *  ├─ Los junta en pares para mostrarlos ordenados (alerta + guía)
 *  └─ Devuelve una lista con esos pares
 *
 * 🟪 cargarArchivo(String archivo, String tipo, List<Mensaje> destino)
 *  ├─ Abre el archivo dentro de /assets (alertas.json o guias.json)
 *  ├─ Convierte el contenido a texto y luego a JSONArray
 *  ├─ Recorre el array y crea objetos Mensaje con los datos
 *  ├─ Calcula la fecha simulada con Controlador.obtenerFechaSimulada()
 *  └─ Añade cada Mensaje a la lista destino (alertas o guias)
 *
 * 🔁 Relación entre métodos:
 * onCreate() → mostrarMensajesIniciales()
 * mostrarMensajesIniciales() → obtenerAmbosMensajesDelDia()
 * mostrarMensajesIniciales() → Controlador.mostrarNotificacion() / reproducirSonido()
 * cargarArchivo() → Controlador.obtenerFechaSimulada()
 */
