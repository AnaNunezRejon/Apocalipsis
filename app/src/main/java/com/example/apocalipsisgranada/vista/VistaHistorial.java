package com.example.apocalipsisgranada.vista;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apocalipsisgranada.R;
import com.example.apocalipsisgranada.controlador.AdaptadorMensajes;
import com.example.apocalipsisgranada.modelo.Mensaje;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VistaHistorial extends BaseActivity {

    private RecyclerView recyclerView;
    private final List<Mensaje> listaAlertas = new ArrayList<>();
    private SharedPreferences prefs;
    private TextView textoModo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // 🟡 Cargar metodos comunes
        configurarModoDesarrolladorComun();
        configurarMenuInferior();
        actualizarCabecera();
        mostrarSaludoUsuario();
        actualizarColoresModoDesarrollador();
        mostrarTextoModoDesarrollador();

        prefs = getSharedPreferences("configuracion", MODE_PRIVATE);

        // 🕰 Recycler de alertas
        recyclerView = findViewById(R.id.recyclerHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarAlertas();
    }

    // ============================================================
    // 📜 CARGAR ALERTAS (solo las de días pasados o antiguos)
    // ============================================================
    private void cargarAlertas() {
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
                // 🟢 Calculamos los días transcurridos desde el inicio real
                long fechaInicio = prefs.getLong("fechaInicio", 0);
                if (fechaInicio == 0) {
                    fechaInicio = System.currentTimeMillis();
                    prefs.edit().putLong("fechaInicio", fechaInicio).apply();
                }
                long diasPasados = (System.currentTimeMillis() - fechaInicio) / (1000 * 60 * 60 * 24);
                diaActual = (int) diasPasados + 1;
            }

            listaAlertas.clear();

            // 🔹 Mensaje inicial “antiguo”
            listaAlertas.add(new Mensaje(
                    0,
                    "23/09/2025",
                    "🌧️ Alerta por lluvias en la provincia de Granada.",
                    "false",
                    "alerta"
            ));

            // 🔹 Añadir solo las alertas hasta el día actual
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int dia = obj.getInt("dia");
                if (dia <= diaActual) {

                    // 🟢 Calcular fecha simulada del mensaje (según día y fechaInicio)
                    String fecha = obtenerFechaSimulada(dia);

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

            // 🔽 ORDENAR DE MÁS RECIENTE A MÁS ANTIGUO
            listaAlertas.sort((m1, m2) -> {
                if (m1.getDia() == m2.getDia()) {
                    // Si tienen el mismo día, ordena por hora (si la hay)
                    return m2.getHora().compareTo(m1.getHora());
                }
                return Integer.compare(m2.getDia(), m1.getDia());
            });

            // 🔽 ACTUALIZAR ADAPTADOR
            recyclerView.setAdapter(new AdaptadorMensajes(listaAlertas, this));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarHistorial() {
        cargarAlertas(); // ya lo tienes hecho
    }


}
