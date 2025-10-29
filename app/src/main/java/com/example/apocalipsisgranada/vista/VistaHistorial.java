package com.example.apocalipsisgranada.vista;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.AdaptadorMensajes;
import com.example.apocalipsisgranada.modelo.Mensaje;
import com.example.apocalipsisgranada.controlador.Controlador;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 🕰️ Resumen rápido del flujo — VistaHistorial
 *
 * Esta pantalla muestra el **historial completo de alertas** que se han emitido
 * en días anteriores o en el día actual.
 * Sirve como registro de todas las notificaciones y mensajes del Gobierno de España
 * que el usuario ha recibido desde que empezó la simulación.
 *
 */

public class VistaHistorial extends AppCompatActivity {

    private RecyclerView recyclerView;
    private final List<Mensaje> listaAlertas = new ArrayList<>();
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // 🟡 Cargar metodos comunes
        Controlador.configurarModoDesarrolladorComun(this);
        Controlador.configurarMenuInferior(this);
        Controlador.actualizarCabecera(this);
        Controlador.mostrarSaludoUsuario(this);
        Controlador.actualizarColoresModoDesarrollador(this);
        Controlador.mostrarTextoModoDesarrollador(this);

        prefs = getSharedPreferences("configuracion", MODE_PRIVATE);

        // Recycler de alertas
        recyclerView = findViewById(R.id.recyclerHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarAlertas();
    }

    // ============================================================
    // CARGAR ALERTAS (solo las de días pasados o antiguos)
    // ============================================================
    public void cargarAlertas() {
        try {
            InputStream is = getAssets().open("alertas.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            boolean modoDev = prefs.getBoolean("modoDesarrollador", false);
            int diaActual;

            if (modoDev) {
                diaActual = prefs.getInt("diaActual", 1);
            } else {
                // Calculamos los días transcurridos desde el inicio real
                long fechaInicio = prefs.getLong("fechaInicio", 0);
                if (fechaInicio == 0) {
                    fechaInicio = System.currentTimeMillis();
                    prefs.edit().putLong("fechaInicio", fechaInicio).apply();
                }
                long diasPasados = (System.currentTimeMillis() - fechaInicio) / (1000 * 60 * 60 * 24);
                diaActual = (int) diasPasados + 1;
            }

            listaAlertas.clear();

            // Mensaje inicial “antiguo”
            listaAlertas.add(new Mensaje(
                    0,
                    "23/09/2025",
                    "🌧️ Alerta por lluvias en la provincia de Granada.",
                    "false",
                    "alerta"
            ));

            SharedPreferences preferencias = getSharedPreferences("configuracion", MODE_PRIVATE);

            // Añadir solo las alertas hasta el día actual
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int dia = obj.getInt("dia");
                if (dia <= diaActual) {

                    // Calcular fecha simulada del mensaje (según día y fechaInicio)
                    String fecha = Controlador.obtenerFechaSimulada(preferencias, dia);

                    Mensaje mensaje = new Mensaje(
                            dia,
                            fecha, // 👈 ahora guardamos la fecha real simulada
                            obj.getString("mensaje"),
                            obj.optString("sonido", "false"),
                            "alerta"
                    );
                    listaAlertas.add(mensaje);
                }
            }

            // ORDENAR DE MÁS RECIENTE A MÁS ANTIGUO
            listaAlertas.sort((m1, m2) -> {
                if (m1.getDia() == m2.getDia()) {
                    // Si tienen el mismo día, ordena por hora (si la hay)
                    return m2.getHora().compareTo(m1.getHora());
                }
                return Integer.compare(m2.getDia(), m1.getDia());
            });

            // ACTUALIZAR ADAPTADOR
            recyclerView.setAdapter(new AdaptadorMensajes(listaAlertas, this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarHistorial() {
        cargarAlertas();
    }
}

/**
 * 🕰️ Resumen rápido del flujo — VistaHistorial
 *
 * Esta pantalla muestra el **historial completo de alertas** que se han emitido
 * en días anteriores o en el día actual.
 * Sirve como registro de todas las notificaciones y mensajes del Gobierno de España
 * que el usuario ha recibido desde que empezó la simulación.
 *
 * 📲 Flujo general:
 * La app abre esta pantalla → entra en onCreate()
 * Dentro de onCreate() se configuran los elementos comunes (modo desarrollador, menú, cabecera…)
 * Luego se prepara el RecyclerView que mostrará todas las alertas
 * Finalmente se llama a cargarAlertas(), que lee las alertas del archivo “alertas.json”
 * y muestra solo las que correspondan a días pasados o al actual.
 *
 * 🟩 onCreate()
 *  ├─ setContentView(R.layout.activity_historial)
 *  ├─ Controlador.configurarModoDesarrolladorComun(this)
 *  ├─ Controlador.configurarMenuInferior(this)
 *  ├─ Controlador.actualizarCabecera(this)
 *  ├─ Controlador.mostrarSaludoUsuario(this)
 *  ├─ Controlador.actualizarColoresModoDesarrollador(this)
 *  ├─ Controlador.mostrarTextoModoDesarrollador(this)
 *  ├─ prefs = getSharedPreferences("configuracion", MODE_PRIVATE)
 *  ├─ recyclerView = findViewById(R.id.recyclerHistorial)
 *  ├─ recyclerView.setLayoutManager(new LinearLayoutManager(this))
 *  └─ cargarAlertas()
 *
 * 🟨 cargarAlertas()
 *  ├─ Abre el archivo “alertas.json” desde la carpeta assets
 *  ├─ Convierte su contenido en texto y luego en JSONArray
 *  ├─ Comprueba si el usuario está en modo desarrollador:
 *  │     ├─ Si está en modo desarrollador → usa el día guardado en prefs (“diaActual”)
 *  │     └─ Si NO está en modo desarrollador → calcula días reales desde “fechaInicio”
 *  │            usando la diferencia entre la hora actual y la guardada en milisegundos
 *  ├─ Limpia la lista anterior (listaAlertas.clear())
 *  ├─ Añade un mensaje inicial “antiguo” (día 0)
 *  ├─ Recorre el JSON con un bucle for:
 *  │     ├─ Lee cada alerta
 *  │     ├─ Comprueba si su día ≤ día actual
 *  │     ├─ Calcula su fecha simulada con Controlador.obtenerFechaSimulada()
 *  │     └─ Crea un objeto Mensaje y lo añade a listaAlertas
 *  ├─ Ordena todas las alertas:
 *  │     ├─ Primero por día (de más reciente a más antiguo)
 *  │     └─ Si tienen el mismo día, por hora (si está disponible)
 *  └─ Asigna el adaptador al RecyclerView:
 *        recyclerView.setAdapter(new AdaptadorMensajes(listaAlertas, this))
 *
 * 🟦 actualizarHistorial()
 *  └─ Llama a cargarAlertas() para refrescar la lista de alertas
 *     (por ejemplo, cuando se avanza de día o se vuelve a esta pantalla)
 *
 * 🔁 Relación entre métodos:
 * onCreate() → cargarAlertas()
 * cargarAlertas() → Controlador.obtenerFechaSimulada()
 * actualizarHistorial() → cargarAlertas()
 *
 * 💡 En resumen:
 * - Lee las alertas del archivo JSON
 * - Calcula qué día del juego estamos
 * - Muestra solo las alertas de los días ya pasados
 * - Las ordena de más nuevas a más viejas
 * - Las enseña en el RecyclerView con su diseño (item_mensajes.xml)
 */

