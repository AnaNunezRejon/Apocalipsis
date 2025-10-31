package com.example.apocalipsisgranada.vista;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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

public class VistaHistorial extends AppCompatActivity {

    private SharedPreferences prefs;
    private RecyclerView recyclerHistorial;
    private List<Mensaje> listaAlertas = new ArrayList<>();

    // ============================================================
    // CICLO DE VIDA
    // ============================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        prefs = getSharedPreferences("configuracion", MODE_PRIVATE);
        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));

        // Configuración general
        Controlador.configurarModoDesarrolladorComun(this);
        ManejadorVistas.configurarElementosComunes(this);

        // Cargar alertas
        cargarAlertas();
    }

    // ============================================================
    // CARGAR ALERTAS
    // ============================================================
    public void cargarAlertas() {
        listaAlertas.clear();

        try {
            InputStream is = getAssets().open("alertas.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            int diaActual = prefs.getInt("diaActual", 1);

            // Mensaje inicial del sistema
            listaAlertas.add(new Mensaje(
                    0,
                    Controlador.obtenerFechaSimulada(prefs, 0),
                    "Registro del Sistema de Alertas — Gobierno de España",
                    "false",
                    "alerta"
            ));

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int dia = obj.getInt("dia");
                String texto = obj.getString("mensaje");
                String sonido = obj.optString("sonido", "false");

                // Solo mostrar alertas hasta el día actual
                if (dia <= diaActual) {
                    listaAlertas.add(new Mensaje(
                            dia,
                            Controlador.obtenerFechaSimulada(prefs, dia),
                            texto,
                            sonido,
                            "alerta"
                    ));
                }
            }

            // Ordenar de más recientes a más antiguas
            Collections.reverse(listaAlertas);

            recyclerHistorial.setAdapter(new AdaptadorMensajes(listaAlertas, this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // ACTUALIZAR HISTORIAL (AL AVANZAR DÍA)
    // ============================================================
    public void actualizarHistorial() {
        cargarAlertas();
    }
}


/**
 * ============================================================
 * 🕓 Clase: VistaHistorial.java
 * ============================================================
 *
 * Muestra el **historial completo de alertas y guías** que el usuario ha recibido
 * a lo largo de la simulación en “Apocalipsis Granada”.
 *
 * Permite revisar todas las notificaciones pasadas de manera ordenada,
 * mostrando cada mensaje con su formato visual correspondiente.
 *
 * ------------------------------------------------------------
 * ⚙️ Funciones principales
 * ------------------------------------------------------------
 *
 * 1️⃣ Carga el layout `activity_historial.xml`.
 * 2️⃣ Configura los elementos visuales comunes (cabecera, menú inferior, modo dev).
 * 3️⃣ Utiliza un `RecyclerView` con el `AdaptadorMensajes` para mostrar los mensajes.
 * 4️⃣ Llama al `Controlador` para cargar todos los pares de mensajes anteriores
 *     (alertas + guías) desde los JSON del modelo.
 * 5️⃣ Actualiza la lista cuando el usuario avanza o reinicia el día.
 *
 * ------------------------------------------------------------
 * 🗂️ Elementos visuales destacados
 * ------------------------------------------------------------
 *
 * - `@id/recyclerHistorial` → lista cronológica descendente de mensajes antiguos.
 * - `@layout/cabecera` → reutilizada en la parte superior.
 * - `@id/textoModo` → muestra el texto “🧪 Modo desarrollador — Día X” si procede.
 *
 * ------------------------------------------------------------
 * 🔁 Flujo de funcionamiento
 * ------------------------------------------------------------
 *
 * 1️⃣ Al abrir la pantalla, se leen el `diaActual` y los mensajes pasados.
 * 2️⃣ Se construye una lista unificada de todos los mensajes anteriores.
 * 3️⃣ Se muestran en el RecyclerView ordenados por día.
 * 4️⃣ El menú inferior permite volver a la vista principal o navegar a otras secciones.
 *
 * ------------------------------------------------------------
 * 💡 En resumen:
 * ------------------------------------------------------------
 *
 * `VistaHistorial.java` permite al usuario **consultar el progreso completo**
 * de la simulación.
 *
 * Ofrece una representación visual ordenada y coherente con el resto del sistema,
 * utilizando las mismas reglas de estilo y arquitectura MVC.
 *
 * ============================================================
 */
