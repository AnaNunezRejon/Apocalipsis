
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

public class VistaGuia extends AppCompatActivity {

    private SharedPreferences prefs;
    private RecyclerView recyclerGuias;
    private List<Mensaje> listaGuias = new ArrayList<>();

    // ============================================================
    // CICLO DE VIDA
    // ============================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guia);

        prefs = getSharedPreferences("configuracion", MODE_PRIVATE);
        recyclerGuias = findViewById(R.id.recyclerGuia);
        recyclerGuias.setLayoutManager(new LinearLayoutManager(this));

        // Configuración general
        Controlador.configurarModoDesarrolladorComun(this);
        ManejadorVistas.configurarElementosComunes(this);

        // Cargar guías
        cargarGuias();
    }

    // ============================================================
    // CARGA DE GUÍAS
    // ============================================================
    public void cargarGuias() {
        listaGuias.clear();

        try {
            InputStream is = getAssets().open("guias.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);

            int diaActual = prefs.getInt("diaActual", 1);

            // Guía inicial fija (día 0)
            listaGuias.add(new Mensaje(
                    0,
                    Controlador.obtenerFechaSimulada(prefs, 0),
                    "Consejo inicial: Mantén la calma y sigue las instrucciones del Gobierno.",
                    "false",
                    "guia"
            ));

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int dia = obj.getInt("dia");
                String texto = obj.getString("mensaje");
                String sonido = obj.optString("sonido", "false");

                if (dia <= diaActual) {
                    listaGuias.add(new Mensaje(
                            dia,
                            Controlador.obtenerFechaSimulada(prefs, dia),
                            texto,
                            sonido,
                            "guia"
                    ));
                }
            }

            // Ordenar de más recientes a más antiguos
            Collections.reverse(listaGuias);

            recyclerGuias.setAdapter(new AdaptadorMensajes(listaGuias, this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // ACTUALIZAR GUÍAS (AL AVANZAR DÍA)
    // ============================================================
    public void actualizarGuias() {
        cargarGuias();
    }
}


/**
 * ============================================================
 * 📘 Clase: VistaGuia.java
 * ============================================================
 *
 * Presenta la **colección de guías y consejos** emitidos por el Gobierno de España
 * durante la simulación del apocalipsis.
 *
 * Es una vista complementaria a la principal, centrada únicamente en las guías.
 *
 * ------------------------------------------------------------
 * ⚙️ Funciones principales
 * ------------------------------------------------------------
 *
 * 1️⃣ Carga el layout `activity_guia.xml`.
 * 2️⃣ Configura cabecera, menú inferior y modo desarrollador.
 * 3️⃣ Obtiene las guías del día actual desde `Controlador` y las muestra.
 * 4️⃣ Permite actualizar el contenido al avanzar de día.
 *
 * ------------------------------------------------------------
 * 🗂️ Elementos visuales destacados
 * ------------------------------------------------------------
 *
 * - `@id/recyclerGuia` → lista de recomendaciones y protocolos diarios.
 * - `@id/textoModo` → indicador de modo desarrollador.
 * - `@layout/cabecera` → zona superior con saludo, fecha y escudo.
 *
 * ------------------------------------------------------------
 * 🔁 Flujo de funcionamiento
 * ------------------------------------------------------------
 *
 * 1️⃣ Al iniciar, se cargan las guías correspondientes al `diaActual`.
 * 2️⃣ Cada guía se muestra en el RecyclerView usando el `AdaptadorMensajes`.
 * 3️⃣ Si el modo desarrollador está activo, se actualiza visualmente la cabecera y colores.
 *
 * ------------------------------------------------------------
 * 💡 En resumen:
 * ------------------------------------------------------------
 *
 * `VistaGuia.java` muestra los mensajes de **orientación y ayuda oficial**
 * del día actual o días previos, reforzando la narrativa de supervivencia.
 *
 * Su implementación modular la mantiene sincronizada con `VistaPrincipal`
 * sin duplicar código de lógica o interfaz.
 *
 * ============================================================
 */
